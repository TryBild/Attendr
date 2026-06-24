import { Company, Subscription } from "../models/index.js";
import { createSubscription, cancelSubscription, fetchSubscription } from "../services/razorpay.js";
import { err } from "../utils/response.js";

const PRO_PLAN_ID = process.env.RAZORPAY_PRO_PLAN_ID || "";

// GET /api/billing/status
export async function getBillingStatus(req, res) {
  try {
    const sub = await Subscription.findOne({ company: req.auth.companyId }).sort({ createdAt: -1 });
    if (!sub) {
      return res.json({
        ok: true,
        plan: "free",
        status: "none",
        trialDaysLeft: 0,
        trialEndsAt: null,
        renewsAt: null,
      });
    }

    let trialDaysLeft = 0;
    if (sub.status === "trialing" && sub.trialEndsAt) {
      trialDaysLeft = Math.max(0, Math.ceil((sub.trialEndsAt - Date.now()) / 86400000));
      if (trialDaysLeft === 0 && sub.status === "trialing") {
        sub.status = "expired";
        await sub.save();
      }
    }

    return res.json({
      ok: true,
      plan: sub.plan,
      status: sub.status,
      trialDaysLeft,
      trialEndsAt: sub.trialEndsAt,
      renewsAt: sub.renewsAt,
      razorpaySubscriptionId: sub.razorpaySubscriptionId || null,
    });
  } catch (e) {
    console.error(e);
    err(res, e.message);
  }
}

// POST /api/billing/create-subscription
export async function createBillingSubscription(req, res) {
  try {
    if (!PRO_PLAN_ID) return err(res, "Billing not configured yet", 503);

    const company = await Company.findById(req.auth.companyId);
    if (!company) return err(res, "Company not found", 404);

    const existing = await Subscription.findOne({
      company: req.auth.companyId,
      status: "active",
    });
    if (existing) return err(res, "You already have an active subscription", 409);

    const rzpSub = await createSubscription(PRO_PLAN_ID, 12, company.adminEmail, company.phone);

    let sub = await Subscription.findOne({ company: req.auth.companyId }).sort({ createdAt: -1 });
    if (sub) {
      sub.razorpaySubscriptionId = rzpSub.id;
      sub.razorpayPlanId = PRO_PLAN_ID;
      sub.plan = "pro";
      await sub.save();
    } else {
      sub = await Subscription.create({
        company: req.auth.companyId,
        plan: "pro",
        status: "trialing",
        razorpaySubscriptionId: rzpSub.id,
        razorpayPlanId: PRO_PLAN_ID,
      });
    }

    return res.json({
      ok: true,
      subscriptionId: rzpSub.id,
      shortUrl: rzpSub.short_url,
      razorpayKeyId: process.env.RAZORPAY_KEY_ID,
    });
  } catch (e) {
    console.error(e);
    err(res, e.message);
  }
}

// POST /api/billing/cancel
export async function cancelBillingSubscription(req, res) {
  try {
    const sub = await Subscription.findOne({
      company: req.auth.companyId,
      status: { $in: ["active", "trialing"] },
    }).sort({ createdAt: -1 });
    if (!sub) return err(res, "No active subscription to cancel", 404);

    if (sub.razorpaySubscriptionId) {
      await cancelSubscription(sub.razorpaySubscriptionId, true);
    }

    sub.status = "cancelled";
    sub.cancelledAt = new Date();
    await sub.save();

    return res.json({ ok: true, message: "Subscription cancelled. Access continues until period end." });
  } catch (e) {
    console.error(e);
    err(res, e.message);
  }
}

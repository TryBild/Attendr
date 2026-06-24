import { Subscription } from "../models/index.js";

export async function requireActiveSubscription(req, res, next) {
  try {
    const companyId = req.auth?.companyId;
    if (!companyId) return next();

    const sub = await Subscription.findOne({ company: companyId }).sort({ createdAt: -1 });
    if (!sub) {
      return res.status(402).json({ ok: false, error: "No subscription found. Please subscribe.", code: "NO_SUBSCRIPTION" });
    }

    if (sub.status === "active") return next();

    if (sub.status === "trialing") {
      if (sub.trialEndsAt && sub.trialEndsAt > new Date()) return next();
      sub.status = "expired";
      await sub.save();
    }

    if (sub.status === "expired" || sub.status === "cancelled") {
      return res.status(402).json({ ok: false, error: "Subscription expired. Please renew.", code: "SUBSCRIPTION_EXPIRED" });
    }

    next();
  } catch (e) {
    console.error("[subscriptionCheck]", e.message);
    next();
  }
}

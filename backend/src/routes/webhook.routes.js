import { Router } from "express";
import express from "express";
import { Subscription } from "../models/index.js";
import { verifyWebhookSignature } from "../services/razorpay.js";

const router = Router();

router.post("/razorpay", express.raw({ type: "application/json" }), async (req, res) => {
  try {
    const signature = req.headers["x-razorpay-signature"];
    const rawBody = req.body;

    if (signature && process.env.RAZORPAY_WEBHOOK_SECRET) {
      const bodyStr = typeof rawBody === "string" ? rawBody : rawBody.toString("utf8");
      if (!verifyWebhookSignature(bodyStr, signature)) {
        console.warn("[Webhook] Invalid signature");
        return res.status(400).json({ ok: false });
      }
    }

    const payload = typeof rawBody === "string" ? JSON.parse(rawBody) : JSON.parse(rawBody.toString("utf8"));
    const event = payload.event;
    const entity = payload.payload?.subscription?.entity || payload.payload?.payment?.entity;

    console.log(`[Webhook] ${event}`, entity?.id);

    if (!entity) return res.json({ ok: true });

    const rzpSubId = entity.subscription_id || entity.id;
    const sub = rzpSubId ? await Subscription.findOne({ razorpaySubscriptionId: rzpSubId }) : null;

    switch (event) {
      case "subscription.activated": {
        if (sub) {
          sub.status = "active";
          sub.renewsAt = entity.current_end ? new Date(entity.current_end * 1000) : null;
          await sub.save();
        }
        break;
      }
      case "subscription.charged": {
        if (sub) {
          sub.status = "active";
          sub.renewsAt = entity.current_end ? new Date(entity.current_end * 1000) : null;
          await sub.save();
        }
        break;
      }
      case "subscription.cancelled": {
        if (sub) {
          sub.status = "cancelled";
          sub.cancelledAt = new Date();
          await sub.save();
        }
        break;
      }
      case "subscription.expired": {
        if (sub) {
          sub.status = "expired";
          await sub.save();
        }
        break;
      }
      case "payment.failed": {
        console.error(`[Webhook] Payment failed for subscription ${rzpSubId}`);
        break;
      }
      default:
        break;
    }

    return res.json({ ok: true });
  } catch (e) {
    console.error("[Webhook] Error:", e.message);
    return res.status(500).json({ ok: false });
  }
});

export default router;

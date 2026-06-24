import { Subscription } from "../models/index.js";

async function expireTrials() {
  const result = await Subscription.updateMany(
    { status: "trialing", trialEndsAt: { $lt: new Date() } },
    { $set: { status: "expired" } },
  );
  if (result.modifiedCount > 0) {
    console.log(`[SubscriptionJob] Expired ${result.modifiedCount} trial(s)`);
  }
}

async function expireLapsed() {
  const result = await Subscription.updateMany(
    { status: "active", renewsAt: { $lt: new Date() } },
    { $set: { status: "expired" } },
  );
  if (result.modifiedCount > 0) {
    console.log(`[SubscriptionJob] Expired ${result.modifiedCount} lapsed subscription(s)`);
  }
}

export async function runSubscriptionCheck() {
  try {
    await expireTrials();
    await expireLapsed();
  } catch (e) {
    console.error("[SubscriptionJob] Error:", e.message);
  }
}

export function startSubscriptionJob() {
  const INTERVAL_MS = 6 * 60 * 60 * 1000;
  setInterval(runSubscriptionCheck, INTERVAL_MS);
  console.log("✓ Subscription expiry check scheduled (every 6h)");
}

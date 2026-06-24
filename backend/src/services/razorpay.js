import crypto from "crypto";

const BASE_URL = "https://api.razorpay.com/v1";

function authHeader() {
  const key = process.env.RAZORPAY_KEY_ID;
  const secret = process.env.RAZORPAY_KEY_SECRET;
  if (!key || !secret) return null;
  return "Basic " + Buffer.from(`${key}:${secret}`).toString("base64");
}

async function rp(method, path, body) {
  const auth = authHeader();
  if (!auth) throw new Error("Razorpay credentials not configured");

  const opts = {
    method,
    headers: { Authorization: auth, "Content-Type": "application/json" },
  };
  if (body) opts.body = JSON.stringify(body);

  const res = await fetch(`${BASE_URL}${path}`, opts);
  const data = await res.json();
  if (!res.ok) throw new Error(data.error?.description || `Razorpay ${method} ${path} failed (${res.status})`);
  return data;
}

export async function createPlan(name, amountPaise, interval = "monthly") {
  return rp("POST", "/plans", {
    period: interval,
    interval: 1,
    item: { name, amount: amountPaise, currency: "INR" },
  });
}

export async function createSubscription(planId, totalCount = 12, notifyEmail, notifyPhone) {
  const body = { plan_id: planId, total_count: totalCount, quantity: 1 };
  if (notifyEmail) body.customer_notify = 1;
  if (notifyPhone) body.notify_info = { notify_phone: notifyPhone };
  return rp("POST", "/subscriptions", body);
}

export async function cancelSubscription(subscriptionId, cancelAtEnd = true) {
  return rp("POST", `/subscriptions/${subscriptionId}/cancel`, {
    cancel_at_cycle_end: cancelAtEnd ? 1 : 0,
  });
}

export async function fetchSubscription(subscriptionId) {
  return rp("GET", `/subscriptions/${subscriptionId}`);
}

export function verifyWebhookSignature(rawBody, signature) {
  const secret = process.env.RAZORPAY_WEBHOOK_SECRET;
  if (!secret) return false;
  const expected = crypto.createHmac("sha256", secret).update(rawBody).digest("hex");
  return crypto.timingSafeEqual(Buffer.from(expected), Buffer.from(signature));
}

import mongoose from "mongoose";

const subscriptionSchema = new mongoose.Schema({
  company:                { type: mongoose.Schema.Types.ObjectId, ref: "Company", required: true, index: true },
  plan:                   { type: String, enum: ["free", "pro", "enterprise"], default: "free" },
  status:                 { type: String, enum: ["trialing", "active", "expired", "cancelled"], default: "trialing" },
  trialEndsAt:            { type: Date },
  renewsAt:               { type: Date },
  razorpaySubscriptionId: { type: String },
  razorpayPlanId:         { type: String },
  razorpayCustomerId:     { type: String },
  amountPaise:            { type: Number },
  cancelledAt:            { type: Date },
}, { timestamps: true });

export default mongoose.model("Subscription", subscriptionSchema);

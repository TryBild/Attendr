import mongoose from "mongoose";

const subscriptionSchema = new mongoose.Schema({
  companyId: { type: mongoose.Types.ObjectId, ref: "Company", required: true, index: true },
  plan:      { type: String, enum: ["free", "pro"], default: "free" },
  status:    { type: String, enum: ["trialing", "active", "expired"], default: "trialing" },
  renewsAt:  { type: Date },
  razorpaySubscriptionId: { type: String },
}, { timestamps: true });

export default mongoose.model("Subscription", subscriptionSchema);

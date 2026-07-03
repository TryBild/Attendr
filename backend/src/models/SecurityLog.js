import mongoose from "mongoose";

const securityLogSchema = new mongoose.Schema({
  companyId: { type: mongoose.Schema.Types.ObjectId, ref: "Company", default: null },
  userId:    { type: mongoose.Schema.Types.ObjectId, default: null },
  userKind:  { type: String, enum: ["admin", "employee"] },
  event: {
    type: String,
    enum: [
      "login_success", "login_failed", "account_locked",
      "password_changed", "password_reset_requested", "otp_brute_force",
    ],
    required: true,
  },
  ip:        { type: String },
  userAgent: { type: String },
  at:        { type: Date, default: Date.now },
});

securityLogSchema.index({ companyId: 1, at: -1 });
// Auto-expire after 90 days
securityLogSchema.index({ at: 1 }, { expireAfterSeconds: 7776000 });

export default mongoose.model("SecurityLog", securityLogSchema);

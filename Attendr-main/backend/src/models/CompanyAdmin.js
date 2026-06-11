import mongoose from "mongoose";

const companyAdminSchema = new mongoose.Schema({
  companyId: { type: mongoose.Types.ObjectId, ref: "Company", required: true, index: true },
  name:      { type: String, trim: true },
  phone:     { type: String, required: true },
  role:      { type: String, enum: ["owner", "hr"], default: "owner" },
  active:    { type: Boolean, default: true },
}, { timestamps: true });

companyAdminSchema.index({ companyId: 1, phone: 1 }, { unique: true });

export default mongoose.model("CompanyAdmin", companyAdminSchema);

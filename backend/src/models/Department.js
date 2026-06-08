import mongoose from "mongoose";

const departmentSchema = new mongoose.Schema({
  companyId: { type: mongoose.Types.ObjectId, ref: "Company", required: true, index: true },
  name:      { type: String, required: true, trim: true },
  active:    { type: Boolean, default: true },
}, { timestamps: true });

departmentSchema.index({ companyId: 1, name: 1 }, { unique: true });

export default mongoose.model("Department", departmentSchema);

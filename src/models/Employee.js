import mongoose from "mongoose";

const employeeSchema = new mongoose.Schema({
  companyId:    { type: mongoose.Types.ObjectId, ref: "Company",    required: true, index: true },
  departmentId: { type: mongoose.Types.ObjectId, ref: "Department" },
  name:         { type: String, required: true, trim: true },
  phone:        { type: String, required: true },
  deviceId:     { type: String, default: null },
  active:       { type: Boolean, default: true },
  consentAt:    { type: Date, default: null },
}, { timestamps: true });

employeeSchema.index({ companyId: 1, phone: 1 }, { unique: true });

export default mongoose.model("Employee", employeeSchema);

import mongoose from "mongoose";

const attendanceLogSchema = new mongoose.Schema({
  companyId:    { type: mongoose.Types.ObjectId, ref: "Company",  required: true, index: true },
  employeeId:   { type: mongoose.Types.ObjectId, ref: "Employee", required: true, index: true },
  type:         { type: String, enum: ["in", "out"], required: true },
  at:           { type: Date, default: Date.now },
  lat:          { type: Number },
  lng:          { type: Number },
  mockDetected: { type: Boolean, default: false },
}, { timestamps: true });

attendanceLogSchema.index({ companyId: 1, employeeId: 1, at: -1 });
attendanceLogSchema.index({ companyId: 1, at: -1 });

export default mongoose.model("AttendanceLog", attendanceLogSchema);

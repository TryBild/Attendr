import mongoose from "mongoose";

const employeeSchema = new mongoose.Schema({
  company:       { type: mongoose.Schema.Types.ObjectId, ref: "Company", required: true },
  department:    { type: mongoose.Schema.Types.ObjectId, ref: "Department" },
  fullName:      { type: String, required: true, trim: true },
  mobile:        { type: String, required: true, trim: true },
  employeeCode:  { type: String },
  designation:   { type: String },
  isActive:      { type: Boolean, default: true },
  isVerified:    { type: Boolean, default: false },
  joinedAt:      { type: Date, default: Date.now },
  lastLogin:     { type: Date },
  otp:           { type: String },
  otpExpiry:     { type: Date },
  otpAttempts:   { type: Number, default: 0 },
  otpLockedUntil:{ type: Date },
}, { timestamps: true });

employeeSchema.index({ mobile: 1, company: 1 }, { unique: true });

export default mongoose.model("Employee", employeeSchema);

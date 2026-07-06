import mongoose from "mongoose";

const companySchema = new mongoose.Schema({
  name:          { type: String, required: true, trim: true },
  teamId:        { type: String, required: true, unique: true, uppercase: true, trim: true },
  adminEmail:    { type: String, required: true, unique: true, lowercase: true, trim: true },
  adminPassword: { type: String, required: true },
  adminName:     { type: String, trim: true },
  loginAttempts: { type: Number, default: 0 },   // admin login failures
  lockedUntil:   { type: Date, default: null },  // admin account lock
  phone:         { type: String },
  address:       { type: String },
  city:          { type: String, required: true },
  state:         { type: String },
  orgSize:       { type: String, enum: ["1-10", "11-50", "51-200", "200+"] },
  isActive:      { type: Boolean, default: true },
  plan:          { type: String, enum: ["free", "monthly", "yearly"], default: "free" },
  maxEmployees:  { type: Number, default: 25 },
  setupComplete: { type: Boolean, default: false },
  industry:      { type: String },
  workDays:      { type: [String], default: [] },
  workStartTime: { type: String },
  workEndTime:   { type: String },
  timezone:      { type: String, default: "Asia/Kolkata" },
  referralSource:{ type: String },
  whatsappAdminNumbers: { type: [String], default: [] },
  holidays:      { type: [String], default: [] },
  photoUrl:      { type: String, default: null },
}, { timestamps: true });

export default mongoose.model("Company", companySchema);

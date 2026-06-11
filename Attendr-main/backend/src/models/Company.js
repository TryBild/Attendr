import mongoose from "mongoose";

const companySchema = new mongoose.Schema({
  name:          { type: String, required: true, trim: true },
  teamId:        { type: String, required: true, unique: true, uppercase: true, trim: true },
  adminEmail:    { type: String, required: true, unique: true, lowercase: true, trim: true },
  adminPassword: { type: String, required: true },
  address:       { type: String },
  city:          { type: String },
  state:         { type: String },
  isActive:      { type: Boolean, default: true },
  plan:          { type: String, enum: ["free", "starter", "pro"], default: "free" },
  maxEmployees:  { type: Number, default: 25 },
}, { timestamps: true });

export default mongoose.model("Company", companySchema);

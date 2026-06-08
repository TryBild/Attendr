import mongoose from "mongoose";

const departmentSchema = new mongoose.Schema({
  company:     { type: mongoose.Schema.Types.ObjectId, ref: "Company", required: true },
  name:        { type: String, required: true, trim: true },
  description: { type: String },
  isActive:    { type: Boolean, default: true },
}, { timestamps: true });

export default mongoose.model("Department", departmentSchema);

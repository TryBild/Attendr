import mongoose from "mongoose";

const geofenceSchema = new mongoose.Schema({
  label:   { type: String, default: "Office" },
  lat:     { type: Number, required: true },
  lng:     { type: Number, required: true },
  radiusM: { type: Number, default: 80 },
}, { _id: false });

const companySchema = new mongoose.Schema({
  name:                 { type: String, required: true, trim: true },
  plan:                 { type: String, enum: ["free", "pro"], default: "free" },
  geofences:            [geofenceSchema],
  requireGeofence:      { type: Boolean, default: true },
  whatsappAdminNumbers: [{ type: String }],
  trialEndsAt:          { type: Date },
  active:               { type: Boolean, default: true },
}, { timestamps: true });

export default mongoose.model("Company", companySchema);

import mongoose from "mongoose";

const geofenceSchema = new mongoose.Schema({
  company:      { type: mongoose.Schema.Types.ObjectId, ref: "Company", required: true },
  name:         { type: String, required: true },
  latitude:     { type: Number, required: true },
  longitude:    { type: Number, required: true },
  radiusMeters: { type: Number, required: true, default: 100 },
  isActive:     { type: Boolean, default: true },
  address:      { type: String },
}, { timestamps: true });

export default mongoose.model("Geofence", geofenceSchema);

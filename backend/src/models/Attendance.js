import mongoose from "mongoose";
import { encrypt, decrypt } from "../utils/encryption.js";

const attendanceSchema = new mongoose.Schema({
  company:  { type: mongoose.Schema.Types.ObjectId, ref: "Company", required: true },
  employee: { type: mongoose.Schema.Types.ObjectId, ref: "Employee", required: true },
  geofence: { type: mongoose.Schema.Types.ObjectId, ref: "Geofence" },
  date:     { type: String, required: true }, // YYYY-MM-DD
  checkInTime:  { type: Date },
  checkOutTime: { type: Date },
  // Mixed: legacy records hold plain Numbers; new writes hold AES-256-GCM strings ("iv:authTag:hex")
  checkInLocation:  { latitude: mongoose.Schema.Types.Mixed, longitude: mongoose.Schema.Types.Mixed },
  checkOutLocation: { latitude: mongoose.Schema.Types.Mixed, longitude: mongoose.Schema.Types.Mixed },
  status: {
    type: String,
    enum: ["present", "absent", "late", "half-day", "leave", "holiday"],
    default: "present",
  },
  isManualEntry: { type: Boolean, default: false },
  mockDetected:  { type: Boolean, default: false },
  notes:         { type: String },
  workingHours:  { type: Number },
}, { timestamps: true });

attendanceSchema.index({ employee: 1, date: 1, company: 1 }, { unique: true });

function encryptCoord(v) {
  if (v == null) return v;
  if (typeof v === "string" && v.includes(":")) return v; // already encrypted
  return encrypt(String(v));
}

function decryptCoord(v) {
  if (typeof v !== "string") return v; // legacy plain Number
  const plain = v.includes(":") ? decrypt(v) : v;
  const n = Number(plain);
  return Number.isNaN(n) ? plain : n;
}

attendanceSchema.pre("save", function (next) {
  for (const field of ["checkInLocation", "checkOutLocation"]) {
    if (this[field] && this.isModified(field)) {
      this[field].latitude  = encryptCoord(this[field].latitude);
      this[field].longitude = encryptCoord(this[field].longitude);
    }
  }
  next();
});

// Decrypt when documents are hydrated from the DB (covers find/findOne; .lean() bypasses)
attendanceSchema.post("init", function (doc) {
  for (const field of ["checkInLocation", "checkOutLocation"]) {
    if (doc[field]) {
      doc[field].latitude  = decryptCoord(doc[field].latitude);
      doc[field].longitude = decryptCoord(doc[field].longitude);
    }
  }
});

export default mongoose.model("Attendance", attendanceSchema);

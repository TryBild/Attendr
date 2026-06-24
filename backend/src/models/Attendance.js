import mongoose from "mongoose";

const attendanceSchema = new mongoose.Schema({
  company:  { type: mongoose.Schema.Types.ObjectId, ref: "Company", required: true },
  employee: { type: mongoose.Schema.Types.ObjectId, ref: "Employee", required: true },
  geofence: { type: mongoose.Schema.Types.ObjectId, ref: "Geofence" },
  date:     { type: String, required: true }, // YYYY-MM-DD
  checkInTime:  { type: Date },
  checkOutTime: { type: Date },
  checkInLocation:  { latitude: Number, longitude: Number },
  checkOutLocation: { latitude: Number, longitude: Number },
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

export default mongoose.model("Attendance", attendanceSchema);

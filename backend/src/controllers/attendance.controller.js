import { Attendance, Employee, Geofence } from "../models/index.js";
import { findMatchingGeofence, haversineDistance } from "../utils/geo.js";
import { err } from "../utils/response.js";

function todayIST() {
  return new Date().toLocaleDateString("en-CA", { timeZone: "Asia/Kolkata" });
}

function formatTime(date) {
  if (!date) return null;
  return new Date(date).toLocaleTimeString("en-IN", {
    timeZone: "Asia/Kolkata",
    hour: "2-digit",
    minute: "2-digit",
    hour12: true,
  });
}

// POST /api/attendance/mark
export async function markAttendance(req, res) {
  try {
    const { latitude, longitude, action, mockDetected, deviceId } = req.body;
    if (!["checkin", "checkout"].includes(action))
      return err(res, "action must be checkin or checkout", 400);
    if (typeof latitude !== "number" || typeof longitude !== "number")
      return err(res, "latitude and longitude are required", 400);

    if (deviceId) {
      const emp = await Employee.findById(req.auth.id, "deviceId");
      if (emp?.deviceId && emp.deviceId !== deviceId) {
        return err(res, "This device is not registered for your account. Contact your admin.", 403);
      }
    }

    const geofences = await Geofence.find({ company: req.auth.companyId, isActive: true });
    const match = findMatchingGeofence(latitude, longitude, geofences);

    if (!match) {
      // Find nearest to give helpful distance info
      let nearestDist = null;
      for (const gf of geofences) {
        const d = Math.round(haversineDistance(latitude, longitude, gf.latitude, gf.longitude));
        if (nearestDist === null || d < nearestDist) nearestDist = d;
      }
      const distMsg = nearestDist !== null ? ` (${nearestDist}m from nearest office)` : "";
      return err(res, `You are not within the office premises${distMsg}. Move closer and try again.`, 403);
    }

    const date = todayIST();

    if (action === "checkin") {
      const existing = await Attendance.findOne({ employee: req.auth.id, date, company: req.auth.companyId });
      if (existing) return err(res, "You have already checked in today.", 409);

      const now = new Date();
      const record = await Attendance.create({
        company:  req.auth.companyId,
        employee: req.auth.id,
        geofence: match.geofence._id,
        date,
        checkInTime:     now,
        checkInLocation: { latitude, longitude },
        status: "present",
        mockDetected: mockDetected === true,
      });

      return res.json({
        ok: true,
        action: "checkin",
        time:   formatTime(now),
        status: record.status,
        geofence: match.geofence.name,
        distance: match.distance,
      });
    }

    // checkout
    const record = await Attendance.findOne({ employee: req.auth.id, date, company: req.auth.companyId });
    if (!record || !record.checkInTime) return err(res, "Please check in first.", 400);
    if (record.checkOutTime) return err(res, "You have already checked out today.", 409);

    const now = new Date();
    const diffMs = now - record.checkInTime;
    const workingHours = Math.round((diffMs / 3600000) * 100) / 100;

    record.checkOutTime = now;
    record.checkOutLocation = { latitude, longitude };
    record.workingHours = workingHours;
    if (mockDetected === true) record.mockDetected = true;
    await record.save();

    return res.json({
      ok: true,
      action:       "checkout",
      time:         formatTime(now),
      status:       record.status,
      workingHours,
    });
  } catch (e) {
    console.error(e);
    err(res, e.message);
  }
}

// GET /api/attendance/today
export async function getTodayAttendance(req, res) {
  try {
    const date = todayIST();
    const record = await Attendance.findOne({
      employee: req.auth.id,
      date,
      company: req.auth.companyId,
    }).populate("geofence", "name");

    if (!record) return res.json({ ok: true, status: "not_marked", date });

    return res.json({
      ok: true,
      date,
      status:       record.status,
      checkInTime:  record.checkInTime,
      checkOutTime: record.checkOutTime,
      workingHours: record.workingHours,
      geofence:     record.geofence?.name || null,
      mockDetected: record.mockDetected || false,
    });
  } catch (e) {
    console.error(e);
    err(res, e.message);
  }
}

// GET /api/attendance/my?month=2026-06
export async function getMyAttendance(req, res) {
  try {
    const monthParam = req.query.month || todayIST().slice(0, 7);
    const [year, month] = monthParam.split("-").map(Number);

    const startDate = `${year}-${String(month).padStart(2, "0")}-01`;
    const lastDay = new Date(year, month, 0).getDate();
    const endDate = `${year}-${String(month).padStart(2, "0")}-${lastDay}`;

    const today = todayIST();
    const [todayY, todayM, todayD] = today.split("-").map(Number);
    const isCurrentMonth = todayY === year && todayM === month;
    const effectiveLastDay = isCurrentMonth ? Math.min(todayD, lastDay) : lastDay;

    const records = await Attendance.find({
      employee: req.auth.id,
      company:  req.auth.companyId,
      date:     { $gte: startDate, $lte: endDate },
    }).sort({ date: 1 });

    const present  = records.filter((r) => r.status === "present").length;
    const late     = records.filter((r) => r.status === "late").length;
    const leaves   = records.filter((r) => r.status === "leave").length;
    const totalMarked = records.length;
    const workingDays = effectiveLastDay - getWeekends(year, month, effectiveLastDay);
    const absent   = Math.max(0, workingDays - present - late - leaves);
    const attendancePercent = workingDays > 0 ? Math.round(((present + late) / workingDays) * 100) : 0;

    return res.json({
      ok: true,
      month:  monthParam,
      records: records.map((r) => ({
        date:         r.date,
        status:       r.status,
        checkInTime:  r.checkInTime,
        checkOutTime: r.checkOutTime,
        workingHours: r.workingHours,
        mockDetected: r.mockDetected || false,
      })),
      summary: { totalMarked, present, absent, late, leaves, workingDays, attendancePercent },
    });
  } catch (e) {
    console.error(e);
    err(res, e.message);
  }
}

// GET /api/attendance/geofences
export async function getGeofences(req, res) {
  try {
    const geofences = await Geofence.find({ company: req.auth.companyId, isActive: true })
      .select("name latitude longitude radiusMeters");
    return res.json({ ok: true, geofences });
  } catch (e) {
    err(res, e.message);
  }
}

function getWeekends(year, month, maxDay) {
  let count = 0;
  const days = maxDay || new Date(year, month, 0).getDate();
  for (let d = 1; d <= days; d++) {
    const day = new Date(year, month - 1, d).getDay();
    if (day === 0 || day === 6) count++;
  }
  return count;
}

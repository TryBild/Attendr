import { Company, AttendanceLog } from "../models/index.js";
import { isInsideGeofence } from "../utils/geo.js";
import { dayStart, dayEnd } from "../utils/dateUtils.js";
import { ok, err } from "../utils/response.js";

export async function markAttendance(req, res) {
  try {
    const { type, lat, lng, mock } = req.body;
    if (!["in", "out"].includes(type)) return err(res, "type must be 'in' or 'out'", 400);

    const company = await Company.findById(req.auth.companyId);
    if (!company) return err(res, "Company not found", 404);

    if (company.requireGeofence) {
      if (typeof lat !== "number" || typeof lng !== "number")
        return err(res, "Location required (lat, lng)", 400);
      if (!isInsideGeofence(lat, lng, company.geofences))
        return err(res, "You are outside the office area", 403);
    }

    const log = await AttendanceLog.create({
      companyId:    req.auth.companyId,
      employeeId:   req.auth.id,
      type,
      lat,
      lng,
      mockDetected: !!mock,
    });

    return res.json({ ok: true, at: log.at, flagged: log.mockDetected });
  } catch (e) { err(res, e.message); }
}

export async function getTodayLogs(req, res) {
  try {
    const logs = await AttendanceLog.find({
      companyId:  req.auth.companyId,
      employeeId: req.auth.id,
      at: { $gte: dayStart(), $lte: dayEnd() },
    }).sort({ at: 1 });
    return res.json({ ok: true, logs });
  } catch (e) { err(res, e.message); }
}

import { Router } from "express";
import { requireAuth } from "../middleware/auth.js";
import { markAttendance, getTodayAttendance, getMyAttendance, getGeofences, notifyAdminGeofence } from "../controllers/attendance.controller.js";

const router = Router();
router.use(requireAuth(["employee"]));
// NOTE: subscription status must NEVER gate an employee marking attendance — that is an
// admin/HR billing concern. requireActiveSubscription is intentionally not applied here.

router.post("/mark",                  markAttendance);
router.get("/today",                  getTodayAttendance);
router.get("/my",                     getMyAttendance);
router.get("/geofences",              getGeofences);
router.post("/notify-admin-geofence", notifyAdminGeofence);

export default router;

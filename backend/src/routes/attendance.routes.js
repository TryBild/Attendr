import { Router } from "express";
import { requireAuth } from "../middleware/auth.js";
import { markAttendance, getTodayAttendance, getMyAttendance, getGeofences } from "../controllers/attendance.controller.js";

const router = Router();
router.use(requireAuth(["employee"]));

router.post("/mark",      markAttendance);
router.get("/today",      getTodayAttendance);
router.get("/my",         getMyAttendance);
router.get("/geofences",  getGeofences);

export default router;

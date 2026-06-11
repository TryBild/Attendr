import { Router } from "express";
import { requireAuth } from "../middleware/auth.js";
import { markAttendance, getTodayAttendance, getMyAttendance } from "../controllers/attendance.controller.js";

const router = Router();
router.use(requireAuth(["employee"]));

router.post("/mark",  markAttendance);
router.get("/today",  getTodayAttendance);
router.get("/my",     getMyAttendance);

export default router;

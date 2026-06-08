import { Router } from "express";
import { requireAuth } from "../middleware/auth.js";
import { tenantScope } from "../middleware/tenantScope.js";
import { markAttendance, getTodayLogs } from "../controllers/attendance.controller.js";

const router = Router();
router.use(requireAuth(["employee", "admin"]), tenantScope);

router.post("/mark",  markAttendance);
router.get ("/today", getTodayLogs);

export default router;

import { Router } from "express";
import { requireAuth } from "../middleware/auth.js";
import { tenantScope } from "../middleware/tenantScope.js";
import { getDayRegister, getMonthCsv } from "../controllers/reports.controller.js";

const router = Router();
router.use(requireAuth(["admin"]), tenantScope);

router.get("/register/day",       getDayRegister);
router.get("/register/month.csv", getMonthCsv);

export default router;

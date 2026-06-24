import { Router } from "express";
import { requireAuth } from "../middleware/auth.js";
import { getMonthCsv, getMonthJson, getMusterRollCsv } from "../controllers/reports.controller.js";

const router = Router();
router.use(requireAuth(["admin"]));

router.get("/month.csv", getMonthCsv);
router.get("/month",     getMonthJson);
router.get("/register/month.csv", getMusterRollCsv);

export default router;

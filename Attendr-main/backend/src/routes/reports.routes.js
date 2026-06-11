import { Router } from "express";
import { requireAuth } from "../middleware/auth.js";
import { getMonthCsv, getMonthJson } from "../controllers/reports.controller.js";

const router = Router();
router.use(requireAuth(["admin"]));

router.get("/month.csv", getMonthCsv);
router.get("/month",     getMonthJson);

export default router;

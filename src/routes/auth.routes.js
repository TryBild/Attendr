import { Router } from "express";
import { requestOtp, verifyAdmin, verifyEmployee } from "../controllers/auth.controller.js";
import { otpRateLimiter } from "../middleware/rateLimiter.js";

const router = Router();

router.post("/otp/request",      otpRateLimiter, requestOtp);
router.post("/admin/verify",     verifyAdmin);
router.post("/employee/verify",  verifyEmployee);

export default router;

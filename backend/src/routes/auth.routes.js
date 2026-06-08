import { Router } from "express";
import { requestOTP, verifyOTP, adminLogin, adminRegister } from "../controllers/auth.controller.js";
import { otpRateLimiter } from "../middleware/rateLimiter.js";

const router = Router();

router.post("/otp/request",    otpRateLimiter, requestOTP);
router.post("/otp/verify",     verifyOTP);
router.post("/admin/login",    adminLogin);
router.post("/admin/register", adminRegister);

export default router;

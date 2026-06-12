import { Router } from "express";
import { requestOTP, verifyOTP, adminLogin, adminRegister, adminSetup, adminProfile } from "../controllers/auth.controller.js";
import { otpRateLimiter } from "../middleware/rateLimiter.js";
import { requireAuth } from "../middleware/auth.js";

const router = Router();

router.post("/otp/request",    otpRateLimiter, requestOTP);
router.post("/otp/verify",     verifyOTP);
router.post("/admin/login",    adminLogin);
router.post("/admin/register", adminRegister);
router.patch("/admin/setup",   requireAuth(["admin"]), adminSetup);
router.get("/admin/profile",   requireAuth(["admin"]), adminProfile);

export default router;

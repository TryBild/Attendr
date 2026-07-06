import { Router } from "express";
import { requestOTP, verifyOTP, employeeLogin, employeeSetPassword, adminLogin, adminRegister, adminSetup, adminProfile } from "../controllers/auth.controller.js";
import { uploadProfilePhoto } from "../controllers/profile.controller.js";
import { otpRateLimiter, otpVerifyLimiter, photoUploadLimiter } from "../middleware/rateLimiter.js";
import { uploadProfilePhotoImage } from "../middleware/upload.js";
import { requireAuth } from "../middleware/auth.js";

const router = Router();

router.post("/otp/request",            otpRateLimiter, requestOTP);
router.post("/otp/verify",             otpVerifyLimiter, verifyOTP);
router.post("/employee/login",         employeeLogin);
router.post("/employee/set-password",  employeeSetPassword);
router.post("/admin/login",            adminLogin);
router.post("/admin/register",         adminRegister);
router.patch("/admin/setup",           requireAuth(["admin"]), adminSetup);
router.get("/admin/profile",           requireAuth(["admin"]), adminProfile);
router.post(
  "/profile/photo",
  requireAuth(["admin", "employee"]),
  photoUploadLimiter,
  uploadProfilePhotoImage,
  uploadProfilePhoto
);

export default router;

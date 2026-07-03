import { z } from "zod";

// teamId formats in production: legacy "ABC123" and current "ATT-XXXX-XXXX"
const teamId = z
  .string({ error: "teamId is required" })
  .trim()
  .toUpperCase()
  .regex(/^[A-Z0-9-]{4,20}$/, "Invalid organisation ID");

const mobile = z
  .string({ error: "mobile is required" })
  .trim()
  .regex(/^[6-9]\d{9}$/, "Enter a valid 10-digit Indian mobile number");

const fullName = z
  .string()
  .trim()
  .min(2, "Full name must be at least 2 characters")
  .max(60, "Full name must be at most 60 characters")
  .regex(/^[A-Za-z][A-Za-z\s.'-]*$/, "Full name can only contain letters and spaces");

const strongPassword = z
  .string({ error: "password is required" })
  .min(8, "Password must be at least 8 characters")
  .max(72, "Password is too long")
  .regex(/[A-Z]/, "Password must contain at least 1 uppercase letter")
  .regex(/\d/, "Password must contain at least 1 number")
  .regex(/[!@#$%^&*]/, "Password must contain at least 1 special character (!@#$%^&*)");

const deviceId = z.string().trim().max(200).optional();

export const otpRequestSchema = z
  .object({
    mobile,
    teamId,
    // Existing clients don't send purpose — default keeps the old behaviour
    purpose:  z.enum(["register", "forgot"]).default("register"),
    fullName: fullName.optional(),
  })
  .refine((d) => d.purpose !== "register" || !!d.fullName, {
    message: "fullName is required for registration",
    path: ["fullName"],
  });

export const otpVerifySchema = z.object({
  mobile,
  teamId,
  otp: z.string({ error: "otp is required" }).trim().regex(/^\d{6}$/, "OTP must be exactly 6 digits"),
});

export const setPasswordSchema = z
  .object({
    pendingToken:    z.string({ error: "pendingToken is required" }).min(20, "Invalid session token"),
    password:        strongPassword,
    confirmPassword: z.string({ error: "confirmPassword is required" }),
    deviceId,
  })
  .refine((d) => d.password === d.confirmPassword, {
    message: "Passwords do not match",
    path: ["confirmPassword"],
  });

export const employeeLoginSchema = z.object({
  mobile,
  teamId,
  // min 6, not 8: passwords created before the strength policy must still log in
  password: z.string({ error: "password is required" }).min(6, "Invalid credentials."),
  deviceId,
});

export const adminLoginSchema = z.object({
  email:    z.email("Invalid email format").trim().toLowerCase(),
  password: z.string({ error: "password is required" }).min(8, "Invalid email or password."),
});

// Accepts both field-name conventions the controller supports
// (companyName/orgName, adminEmail/email) plus optional profile fields.
export const adminRegisterSchema = z
  .object({
    companyName: z.string().trim().min(2, "Organization name must be at least 2 characters").max(100).optional(),
    orgName:     z.string().trim().min(2, "Organization name must be at least 2 characters").max(100).optional(),
    adminEmail:  z.email("Invalid email format").trim().toLowerCase().optional(),
    email:       z.email("Invalid email format").trim().toLowerCase().optional(),
    password:    strongPassword,
    adminName:   z.string().trim().max(60).optional(),
    phone:       z.string().trim().max(20).optional(),
    city:        z.string().trim().max(60).optional(),
    state:       z.string().trim().max(60).optional(),
    orgSize:     z.enum(["1-10", "11-50", "51-200", "200+"]).optional(),
  })
  .refine((d) => !!(d.companyName || d.orgName), {
    message: "companyName is required",
    path: ["companyName"],
  })
  .refine((d) => !!(d.adminEmail || d.email), {
    message: "email is required",
    path: ["email"],
  });

import bcrypt from "bcryptjs";
import { Company, Department, Employee } from "../models/index.js";
import { sendOTP } from "../services/otpService.js";
import { signToken } from "../middleware/auth.js";
import { err } from "../utils/response.js";

function generateOTP() {
  return String(Math.floor(100000 + Math.random() * 900000));
}

function generateTeamId(companyName) {
  const letters = companyName.replace(/[^a-zA-Z]/g, "").toUpperCase().slice(0, 3).padEnd(3, "X");
  const digits = String(Math.floor(100 + Math.random() * 900));
  return letters + digits;
}

function maskMobile(mobile) {
  return "+91 " + mobile.slice(0, 2) + "XXXXXX" + mobile.slice(-2);
}

function todayIST() {
  return new Date().toLocaleDateString("en-CA", { timeZone: "Asia/Kolkata" });
}

// POST /api/auth/otp/request
export async function requestOTP(req, res) {
  try {
    const { fullName, mobile, teamId } = req.body;
    if (!fullName || !mobile || !teamId)
      return err(res, "fullName, mobile, and teamId are required", 400);
    if (!/^[6-9]\d{9}$/.test(mobile))
      return err(res, "Enter a valid 10-digit Indian mobile number", 400);

    const company = await Company.findOne({ teamId: teamId.toUpperCase() });
    if (!company) return err(res, "Organization not found. Please contact your administrator.", 404);
    if (!company.isActive) return err(res, "Organization account is inactive.", 403);

    let employee = await Employee.findOne({ mobile, company: company._id });

    if (employee) {
      if (!employee.isActive) return err(res, "Your account has been deactivated. Contact HR.", 403);
      if (employee.fullName !== fullName.trim()) {
        employee.fullName = fullName.trim();
      }
    } else {
      employee = new Employee({
        company: company._id,
        fullName: fullName.trim(),
        mobile,
        isVerified: false,
      });
    }

    // OTP lock check
    if (employee.otpLockedUntil && employee.otpLockedUntil > new Date()) {
      const remainingMs = employee.otpLockedUntil - new Date();
      const mins = Math.ceil(remainingMs / 60000);
      return err(res, `Too many attempts. Try again in ${mins} minute(s).`, 429);
    }

    const otp = generateOTP();
    const hashed = await bcrypt.hash(otp, 10);
    const expiryMins = Number(process.env.OTP_EXPIRY_MINUTES) || 10;

    employee.otp = hashed;
    employee.otpExpiry = new Date(Date.now() + expiryMins * 60 * 1000);
    employee.otpAttempts = (employee.otpAttempts || 0) + 1;

    if (employee.otpAttempts > 5) {
      employee.otpLockedUntil = new Date(Date.now() + 30 * 60 * 1000);
      await employee.save();
      return err(res, "Too many OTP requests. Account locked for 30 minutes.", 429);
    }

    await employee.save();
    await sendOTP(mobile, otp);

    return res.json({
      ok: true,
      message: `OTP sent to ${maskMobile(mobile)}`,
      expiresInMinutes: expiryMins,
    });
  } catch (e) {
    console.error(e);
    err(res, e.message);
  }
}

// POST /api/auth/otp/verify
export async function verifyOTP(req, res) {
  try {
    const { mobile, teamId, otp } = req.body;
    if (!mobile || !teamId || !otp)
      return err(res, "mobile, teamId, and otp are required", 400);

    const company = await Company.findOne({ teamId: teamId.toUpperCase() });
    if (!company) return err(res, "Organization not found.", 404);

    const employee = await Employee.findOne({ mobile, company: company._id })
      .populate("company", "name teamId")
      .populate("department", "name");

    if (!employee) return err(res, "Employee not found.", 404);
    if (!employee.otp || !employee.otpExpiry)
      return err(res, "No OTP requested. Please request a new OTP.", 400);
    if (employee.otpExpiry < new Date())
      return err(res, "OTP has expired. Please request a new one.", 400);

    const valid = await bcrypt.compare(String(otp), employee.otp);
    if (!valid) {
      employee.otpAttempts = (employee.otpAttempts || 0) + 1;
      if (employee.otpAttempts >= 3) {
        employee.otpLockedUntil = new Date(Date.now() + 15 * 60 * 1000);
      }
      await employee.save();
      const remaining = Math.max(0, 3 - employee.otpAttempts);
      return err(res, `Invalid OTP. ${remaining} attempt(s) remaining.`, 401);
    }

    employee.otp = undefined;
    employee.otpExpiry = undefined;
    employee.otpAttempts = 0;
    employee.otpLockedUntil = undefined;
    employee.isVerified = true;
    employee.lastLogin = new Date();
    await employee.save();

    const token = signToken({
      id:        employee._id,
      companyId: company._id,
      teamId:    company.teamId,
      kind:      "employee",
    });

    return res.json({
      ok: true,
      token,
      employee: {
        id:           employee._id,
        fullName:     employee.fullName,
        mobile:       employee.mobile,
        employeeCode: employee.employeeCode,
        designation:  employee.designation,
        department:   employee.department?.name || null,
        joinedAt:     employee.joinedAt,
        company: {
          name:   company.name,
          teamId: company.teamId,
        },
      },
    });
  } catch (e) {
    console.error(e);
    err(res, e.message);
  }
}

// POST /api/auth/admin/login
export async function adminLogin(req, res) {
  try {
    const { email, password } = req.body;
    if (!email || !password) return err(res, "email and password are required", 400);

    const company = await Company.findOne({ adminEmail: email.toLowerCase() });
    if (!company) return err(res, "Invalid email or password.", 401);
    if (!company.isActive) return err(res, "Account is inactive. Contact support.", 403);

    const valid = await bcrypt.compare(password, company.adminPassword);
    if (!valid) return err(res, "Invalid email or password.", 401);

    const token = signToken({
      id:        company._id,
      companyId: company._id,
      teamId:    company.teamId,
      kind:      "admin",
    });

    return res.json({
      ok: true,
      token,
      company: {
        id:           company._id,
        name:         company.name,
        teamId:       company.teamId,
        plan:         company.plan,
        city:         company.city,
        state:        company.state,
        setupComplete: company.setupComplete ?? false,
      },
    });
  } catch (e) {
    console.error(e);
    err(res, e.message);
  }
}

function generateOrgId() {
  const chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  const part = (n) => Array.from({ length: n }, () => chars[Math.floor(Math.random() * chars.length)]).join("");
  return `ATT-${part(4)}-${part(4)}`;
}

// POST /api/auth/admin/register
export async function adminRegister(req, res) {
  try {
    const { orgName, adminName, email, phone, city, orgSize, password } = req.body;

    if (!orgName || !adminName || !email || !phone || !city || !orgSize || !password)
      return err(res, "All fields are required", 400);
    if (orgName.trim().length < 2)
      return err(res, "Organization name must be at least 2 characters", 400);
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email))
      return err(res, "Invalid email format", 400);
    if (!/^\d{10}$/.test(phone))
      return err(res, "Phone must be exactly 10 digits", 400);
    if (password.length < 8)
      return err(res, "Password must be at least 8 characters", 400);

    const existing = await Company.findOne({ adminEmail: email.toLowerCase() });
    if (existing) return err(res, "An account with this email already exists.", 409);

    const hashed = await bcrypt.hash(password, 12);

    let orgId;
    for (let i = 0; i < 10; i++) {
      const candidate = generateOrgId();
      const clash = await Company.findOne({ teamId: candidate });
      if (!clash) { orgId = candidate; break; }
    }
    if (!orgId) return err(res, "Could not generate Org ID. Try again.", 500);

    const company = await Company.create({
      name:          orgName.trim(),
      teamId:        orgId,
      adminEmail:    email.toLowerCase(),
      adminPassword: hashed,
      adminName:     adminName.trim(),
      phone,
      city:          city.trim(),
      orgSize,
      setupComplete: false,
    });

    await Department.create({ company: company._id, name: "General" });

    const token = signToken({
      id:        company._id,
      companyId: company._id,
      teamId:    company.teamId,
      kind:      "admin",
    });

    return res.status(201).json({ ok: true, orgId, token });
  } catch (e) {
    console.error(e);
    if (e.code === 11000) { console.error("11000 key:", JSON.stringify(e.keyValue)); return err(res, "Account already exists: " + JSON.stringify(e.keyValue), 409); }
    err(res, e.message);
  }
}

// PATCH /api/auth/admin/setup
export async function adminSetup(req, res) {
  try {
    const { industry, workDays, workStartTime, workEndTime, timezone, referralSource } = req.body;

    if (!industry || !workDays || !workStartTime || !workEndTime || !timezone || !referralSource)
      return err(res, "All setup fields are required", 400);
    if (!Array.isArray(workDays) || workDays.length === 0)
      return err(res, "At least one work day must be selected", 400);
    if (workEndTime <= workStartTime)
      return err(res, "Work end time must be after start time", 400);

    await Company.findByIdAndUpdate(req.auth.companyId, {
      industry,
      workDays,
      workStartTime,
      workEndTime,
      timezone,
      referralSource,
      setupComplete: true,
    });

    return res.json({ ok: true, success: true });
  } catch (e) {
    console.error(e);
    err(res, e.message);
  }
}

// GET /api/auth/admin/profile
export async function adminProfile(req, res) {
  try {
    const company = await Company.findById(req.auth.companyId)
      .select("name teamId adminName setupComplete");
    if (!company) return err(res, "Company not found", 404);

    return res.json({
      ok:           true,
      setupComplete: company.setupComplete ?? false,
      orgId:        company.teamId,
      orgName:      company.name,
      adminName:    company.adminName,
    });
  } catch (e) {
    console.error(e);
    err(res, e.message);
  }
}

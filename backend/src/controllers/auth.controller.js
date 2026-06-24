import jwt from "jsonwebtoken";
import bcrypt from "bcryptjs";
import { Company, Department, Employee, Geofence } from "../models/index.js";
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
    const { fullName, mobile, teamId, purpose = "register" } = req.body;
    if (!mobile || !teamId)
      return err(res, "mobile and teamId are required", 400);
    if (purpose === "register" && !fullName)
      return err(res, "fullName is required for registration", 400);
    if (!/^[6-9]\d{9}$/.test(mobile))
      return err(res, "Enter a valid 10-digit Indian mobile number", 400);

    const company = await Company.findOne({ teamId: teamId.toUpperCase() });
    if (!company) return err(res, "Organization not found. Please contact your administrator.", 404);
    if (!company.isActive) return err(res, "Organization account is inactive.", 403);

    let employee = await Employee.findOne({ mobile, company: company._id });

    if (employee) {
      if (!employee.isActive) return err(res, "Your account has been deactivated. Contact HR.", 403);
      if (purpose === "register" && employee.passwordHash)
        return err(res, "Account already exists. Please log in.", 409);
      if (purpose === "forgot" && !employee.passwordHash)
        return err(res, "No account found. Please register first.", 404);
      if (purpose === "register" && fullName && employee.fullName !== fullName.trim()) {
        employee.fullName = fullName.trim();
      }
    } else {
      if (purpose === "forgot")
        return err(res, "No account found. Please register first.", 404);
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
    // A duplicate-key here means a stale/orphan index (e.g. companyId_1_phone_1)
    // exists on the employees collection — surface a clear 409 instead of a raw 500.
    // Fix: run scripts/fix-employee-indexes.js against the affected database.
    if (e.code === 11000) {
      console.error("11000 key:", JSON.stringify(e.keyValue));
      return err(res, "Could not register due to a database conflict. Please try again or contact support.", 409);
    }
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

    const employee = await Employee.findOne({ mobile, company: company._id });

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
    await employee.save();

    const pendingToken = signToken({
      id:        employee._id,
      companyId: company._id,
      teamId:    company.teamId,
      kind:      "pending",
    }, "5m");

    return res.json({
      ok: true,
      pendingToken,
      fullName: employee.fullName,
    });
  } catch (e) {
    console.error(e);
    err(res, e.message);
  }
}

// POST /api/auth/employee/set-password
export async function employeeSetPassword(req, res) {
  try {
    const { pendingToken, password, confirmPassword, deviceId } = req.body;
    if (!pendingToken || !password || !confirmPassword)
      return err(res, "pendingToken, password, and confirmPassword are required", 400);
    if (password !== confirmPassword)
      return err(res, "Passwords do not match", 400);
    if (password.length < 6)
      return err(res, "Password must be at least 6 characters", 400);

    let payload;
    try {
      payload = jwt.verify(pendingToken, process.env.JWT_SECRET);
    } catch {
      return err(res, "Session expired. Please verify OTP again.", 401);
    }
    if (payload.kind !== "pending")
      return err(res, "Invalid token", 401);

    const employee = await Employee.findById(payload.id)
      .populate("company", "name teamId")
      .populate("department", "name");
    if (!employee) return err(res, "Employee not found.", 404);

    employee.passwordHash = await bcrypt.hash(password, 12);
    employee.isVerified = true;
    employee.lastLogin = new Date();
    if (deviceId) employee.deviceId = deviceId;
    await employee.save();

    const token = signToken({
      id:        employee._id,
      companyId: payload.companyId,
      teamId:    payload.teamId,
      kind:      "employee",
    });

    return res.json({
      ok: true,
      token,
      employee: buildEmployeeResponse(employee),
    });
  } catch (e) {
    console.error(e);
    err(res, e.message);
  }
}

// POST /api/auth/employee/login
export async function employeeLogin(req, res) {
  try {
    const { mobile, teamId, password, deviceId } = req.body;
    if (!mobile || !teamId || !password)
      return err(res, "mobile, teamId, and password are required", 400);

    const company = await Company.findOne({ teamId: teamId.toUpperCase() });
    if (!company) return err(res, "Invalid credentials.", 401);
    if (!company.isActive) return err(res, "Organization account is inactive.", 403);

    const employee = await Employee.findOne({ mobile, company: company._id })
      .populate("company", "name teamId")
      .populate("department", "name");
    if (!employee || !employee.passwordHash)
      return err(res, "Invalid credentials.", 401);
    if (!employee.isActive)
      return err(res, "Your account has been deactivated. Contact HR.", 403);

    const valid = await bcrypt.compare(password, employee.passwordHash);
    if (!valid) return err(res, "Invalid credentials.", 401);

    if (deviceId) {
      if (!employee.deviceId) {
        employee.deviceId = deviceId;
      } else if (employee.deviceId !== deviceId) {
        return err(res, "This account is bound to another device. Contact your admin to reset.", 403);
      }
    }

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
      employee: buildEmployeeResponse(employee),
    });
  } catch (e) {
    console.error(e);
    err(res, e.message);
  }
}

function buildEmployeeResponse(employee) {
  return {
    id:           employee._id,
    fullName:     employee.fullName,
    mobile:       employee.mobile,
    employeeCode: employee.employeeCode,
    designation:  employee.designation,
    department:   employee.department?.name || null,
    joinedAt:     employee.joinedAt,
    company: {
      name:   employee.company.name,
      teamId: employee.company.teamId,
    },
  };
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
    // Accept both field-name conventions
    const companyName = (req.body.companyName || req.body.orgName || "").trim();
    const email       = (req.body.adminEmail  || req.body.email   || "").trim();
    const { password, city, state, adminName, phone, orgSize } = req.body;

    if (!companyName || !email || !password)
      return err(res, "companyName, email, and password are required", 400);
    if (companyName.length < 2)
      return err(res, "Organization name must be at least 2 characters", 400);
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email))
      return err(res, "Invalid email format", 400);
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
      name:          companyName,
      teamId:        orgId,
      adminEmail:    email.toLowerCase(),
      adminPassword: hashed,
      adminName:     adminName?.trim(),
      phone:         phone,
      city:          city?.trim() || "N/A",
      state:         state,
      orgSize:       orgSize,
      setupComplete: false,
    });

    await Department.create({ company: company._id, name: "General" });

    const token = signToken({
      id:        company._id,
      companyId: company._id,
      teamId:    company.teamId,
      kind:      "admin",
    });

    return res.status(201).json({
      ok: true,
      token,
      company: { id: company._id, name: company.name, teamId: company.teamId },
    });
  } catch (e) {
    console.error(e);
    if (e.code === 11000) { console.error("11000 key:", JSON.stringify(e.keyValue)); return err(res, "Account already exists: " + JSON.stringify(e.keyValue), 409); }
    err(res, e.message);
  }
}

// PATCH /api/auth/admin/setup
export async function adminSetup(req, res) {
  try {
    const { workDays, workStartTime, workEndTime, geofence, industry, timezone, referralSource, adminName } = req.body;

    if (!Array.isArray(workDays) || workDays.length === 0)
      return err(res, "At least one work day must be selected", 400);
    if (!workStartTime || !workEndTime)
      return err(res, "workStartTime and workEndTime are required", 400);
    if (workEndTime <= workStartTime)
      return err(res, "Work end time must be after start time", 400);

    await Company.findByIdAndUpdate(req.auth.companyId, {
      workDays,
      workStartTime,
      workEndTime,
      ...(industry      && { industry }),
      ...(timezone      && { timezone }),
      ...(referralSource && { referralSource }),
      ...(adminName && adminName.trim() && { adminName: adminName.trim() }),
      setupComplete: true,
    });

    if (geofence && geofence.latitude != null && geofence.longitude != null) {
      await Geofence.create({
        company:      req.auth.companyId,
        name:         (geofence.name || "Main Office").trim(),
        latitude:     Number(geofence.latitude),
        longitude:    Number(geofence.longitude),
        radiusMeters: Number(geofence.radiusMeters) || 100,
        address:      geofence.address?.trim(),
      });
    }

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
      .select("name teamId adminName phone setupComplete workDays workStartTime workEndTime");
    if (!company) return err(res, "Company not found", 404);

    return res.json({
      ok:            true,
      setupComplete: company.setupComplete ?? false,
      orgId:         company.teamId,
      orgName:       company.name,
      adminName:     company.adminName,
      phone:         company.phone,
      workDays:      company.workDays ?? [],
      workStartTime: company.workStartTime,
      workEndTime:   company.workEndTime,
    });
  } catch (e) {
    console.error(e);
    err(res, e.message);
  }
}

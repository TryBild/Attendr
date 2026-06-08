import { Company, CompanyAdmin, Employee, Subscription } from "../models/index.js";
import { createAndSendOtp, verifyOtp } from "../services/otp.js";
import { signToken } from "../middleware/auth.js";
import { ok, err } from "../utils/response.js";
import { TRIAL_DAYS } from "../config/constants.js";

export async function requestOtp(req, res) {
  try {
    const { phone } = req.body;
    if (!phone) return err(res, "phone required", 400);
    await createAndSendOtp(phone);
    return res.json({ ok: true, message: "OTP sent" });
  } catch (e) { err(res, e.message); }
}

export async function verifyAdmin(req, res) {
  try {
    const { phone, code, companyName } = req.body;
    if (!phone || !code) return err(res, "phone and code required", 400);

    const valid = await verifyOtp(phone, code);
    if (!valid) return err(res, "Invalid or expired OTP", 400);

    let admin = await CompanyAdmin.findOne({ phone });

    if (!admin) {
      if (!companyName) return err(res, "companyName required for first login", 400);
      const trialEndsAt = new Date(Date.now() + TRIAL_DAYS * 24 * 60 * 60 * 1000);
      const company = await Company.create({
        name: companyName,
        trialEndsAt,
        whatsappAdminNumbers: [phone],
      });
      await Subscription.create({ companyId: company._id, status: "trialing" });
      admin = await CompanyAdmin.create({ companyId: company._id, phone, role: "owner" });
    }

    const token = signToken({ id: admin._id, companyId: admin.companyId, kind: "admin" });
    return res.json({ ok: true, token, companyId: admin.companyId });
  } catch (e) { err(res, e.message); }
}

export async function verifyEmployee(req, res) {
  try {
    const { phone, code, deviceId } = req.body;
    if (!phone || !code) return err(res, "phone and code required", 400);

    const valid = await verifyOtp(phone, code);
    if (!valid) return err(res, "Invalid or expired OTP", 400);

    const emp = await Employee.findOne({ phone, active: true });
    if (!emp) return err(res, "Not invited to any company. Ask your HR.", 404);

    if (emp.deviceId && deviceId && emp.deviceId !== deviceId)
      return err(res, "Device mismatch. Contact HR to re-bind your device.", 403);

    if (!emp.deviceId && deviceId) emp.deviceId = deviceId;
    if (!emp.consentAt) emp.consentAt = new Date();
    await emp.save();

    const token = signToken({ id: emp._id, companyId: emp.companyId, kind: "employee" });
    return res.json({ ok: true, token, name: emp.name, companyId: emp.companyId });
  } catch (e) { err(res, e.message); }
}

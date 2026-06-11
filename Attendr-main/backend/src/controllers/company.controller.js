import { Company, Department, Employee } from "../models/index.js";
import { ok, err } from "../utils/response.js";

export async function updateGeofences(req, res) {
  try {
    const { geofences, requireGeofence } = req.body;
    const update = {};
    if (Array.isArray(geofences))        update.geofences       = geofences;
    if (typeof requireGeofence === "boolean") update.requireGeofence = requireGeofence;
    const company = await Company.findByIdAndUpdate(req.auth.companyId, update, { new: true });
    return res.json({ ok: true, company });
  } catch (e) { err(res, e.message); }
}

export async function getCompany(req, res) {
  try {
    const company = await Company.findById(req.auth.companyId);
    return res.json({ ok: true, company });
  } catch (e) { err(res, e.message); }
}

export async function createDepartment(req, res) {
  try {
    const { name } = req.body;
    if (!name) return err(res, "name required", 400);
    const dept = await Department.create({ companyId: req.auth.companyId, name });
    return res.status(201).json({ ok: true, dept });
  } catch (e) {
    if (e.code === 11000) return err(res, "Department already exists", 409);
    err(res, e.message);
  }
}

export async function getDepartments(req, res) {
  try {
    const depts = await Department.find({ companyId: req.auth.companyId, active: true });
    return res.json({ ok: true, depts });
  } catch (e) { err(res, e.message); }
}

export async function inviteEmployee(req, res) {
  try {
    const { name, phone, departmentId } = req.body;
    if (!name || !phone) return err(res, "name and phone required", 400);
    const emp = await Employee.create({ companyId: req.auth.companyId, name, phone, departmentId });
    return res.status(201).json({ ok: true, emp });
  } catch (e) {
    if (e.code === 11000) return err(res, "Employee with this phone already exists", 409);
    err(res, e.message);
  }
}

export async function getEmployees(req, res) {
  try {
    const emps = await Employee.find({ companyId: req.auth.companyId })
      .populate("departmentId", "name")
      .sort({ name: 1 });
    return res.json({ ok: true, emps });
  } catch (e) { err(res, e.message); }
}

export async function toggleEmployee(req, res) {
  try {
    const emp = await Employee.findOne({ _id: req.params.id, companyId: req.auth.companyId });
    if (!emp) return err(res, "Employee not found", 404);
    emp.active = !emp.active;
    await emp.save();
    return res.json({ ok: true, active: emp.active });
  } catch (e) { err(res, e.message); }
}

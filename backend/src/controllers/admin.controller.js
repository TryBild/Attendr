import { Company, Department, Employee, Geofence, Attendance } from "../models/index.js";
import { err } from "../utils/response.js";

function todayIST() {
  return new Date().toLocaleDateString("en-CA", { timeZone: "Asia/Kolkata" });
}

function formatTime12(date) {
  if (!date) return null;
  return new Date(date).toLocaleTimeString("en-IN", {
    timeZone: "Asia/Kolkata",
    hour: "2-digit",
    minute: "2-digit",
    hour12: true,
  });
}

// ─── DASHBOARD ──────────────────────────────────────────────────────────────

function checkinStatus(checkInTime, startHour, startMinute) {
  if (!checkInTime || startHour === null) return null;
  const timeStr = new Date(checkInTime).toLocaleTimeString("en-US", {
    timeZone: "Asia/Kolkata",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });
  const [h, m] = timeStr.split(":").map(Number);
  return h * 60 + m > startHour * 60 + startMinute ? "late" : "on_time";
}

// GET /api/admin/dashboard
export async function getDashboard(req, res) {
  try {
    const companyId = req.auth.companyId;
    const date = todayIST();

    // Fetch workStartTime once — format is "HH:mm" (24-hour)
    const company = await Company.findById(companyId, "workStartTime");
    let startHour = null, startMinute = null;
    if (company?.workStartTime) {
      const parts = company.workStartTime.split(":").map(Number);
      startHour   = parts[0] ?? null;
      startMinute = parts[1] ?? 0;
    }

    const totalEmployees = await Employee.countDocuments({ company: companyId, isActive: true });

    const todayRecords = await Attendance.find({ company: companyId, date })
      .populate({
        path: "employee",
        select: "fullName department",
        populate: { path: "department", select: "name" },
      })
      .sort({ checkInTime: -1 });

    const present = todayRecords.filter((r) => r.status === "present" || r.status === "late").length;
    const late    = todayRecords.filter((r) => r.status === "late").length;
    const absent  = totalEmployees - present;
    const attendancePercent = totalEmployees > 0 ? Math.round((present / totalEmployees) * 100) : 0;

    const recentActivity = todayRecords.slice(0, 10).map((r) => {
      const action = r.checkOutTime ? "checkout" : "checkin";
      return {
        employeeName: r.employee?.fullName || "Unknown",
        department:   r.employee?.department?.name || null,
        action,
        status: action === "checkin" ? checkinStatus(r.checkInTime, startHour, startMinute) : null,
        time:   formatTime12(r.checkOutTime || r.checkInTime),
      };
    });

    // This month avg
    const now = new Date();
    const monthStart = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-01`;
    const monthRecords = await Attendance.find({
      company: companyId,
      date:    { $gte: monthStart, $lte: date },
      status:  { $in: ["present", "late"] },
    });

    const daysSoFar = Number(date.split("-")[2]);
    const avgAttendance = daysSoFar > 0
      ? Math.round(monthRecords.length / (daysSoFar * Math.max(totalEmployees, 1)) * 100)
      : 0;

    return res.json({
      ok: true,
      today: { date, totalEmployees, present, absent, late, attendancePercent },
      thisMonth: { avgAttendance, totalWorkingDays: daysSoFar },
      recentActivity,
    });
  } catch (e) {
    console.error(e);
    err(res, e.message);
  }
}

// ─── DAY REGISTER ────────────────────────────────────────────────────────────

// GET /api/admin/attendance/day?date=2026-06-08
export async function getDayRegister(req, res) {
  try {
    const companyId = req.auth.companyId;
    const date = req.query.date || todayIST();

    const employees = await Employee.find({ company: companyId, isActive: true })
      .populate("department", "name")
      .sort({ fullName: 1 });

    const records = await Attendance.find({ company: companyId, date });
    const recordMap = {};
    for (const r of records) recordMap[String(r.employee)] = r;

    const rows = employees.map((emp, i) => {
      const r = recordMap[String(emp._id)];
      return {
        index:        i + 1,
        employeeId:   emp._id,
        employeeCode: emp.employeeCode || "-",
        fullName:     emp.fullName,
        department:   emp.department?.name || "-",
        status:       r?.status || "absent",
        checkInTime:  formatTime12(r?.checkInTime),
        checkOutTime: formatTime12(r?.checkOutTime),
        workingHours: r?.workingHours || null,
        late:         r?.status === "late",
      };
    });

    const present = rows.filter((r) => ["present", "late"].includes(r.status)).length;
    return res.json({ ok: true, date, rows, present, total: rows.length });
  } catch (e) {
    console.error(e);
    err(res, e.message);
  }
}

// ─── EMPLOYEES ───────────────────────────────────────────────────────────────

// GET /api/admin/employees
export async function getEmployees(req, res) {
  try {
    const employees = await Employee.find({ company: req.auth.companyId })
      .populate("department", "name")
      .sort({ fullName: 1 });
    return res.json({ ok: true, employees });
  } catch (e) {
    err(res, e.message);
  }
}

// POST /api/admin/employees
export async function addEmployee(req, res) {
  try {
    const { fullName, mobile, departmentId, employeeCode, designation } = req.body;
    if (!fullName || !mobile) return err(res, "fullName and mobile are required", 400);
    if (!/^[6-9]\d{9}$/.test(mobile))
      return err(res, "Enter a valid 10-digit Indian mobile number", 400);

    const emp = await Employee.create({
      company: req.auth.companyId,
      fullName: fullName.trim(),
      mobile,
      department:   departmentId || undefined,
      employeeCode: employeeCode?.trim() || undefined,
      designation:  designation?.trim() || undefined,
    });
    const populated = await emp.populate("department", "name");
    return res.status(201).json({ ok: true, employee: populated });
  } catch (e) {
    if (e.code === 11000) return err(res, "An employee with this mobile already exists in this company.", 409);
    err(res, e.message);
  }
}

// PUT /api/admin/employees/:id
export async function updateEmployee(req, res) {
  try {
    const { fullName, departmentId, employeeCode, designation, isActive } = req.body;
    const emp = await Employee.findOne({ _id: req.params.id, company: req.auth.companyId });
    if (!emp) return err(res, "Employee not found", 404);

    if (fullName)       emp.fullName     = fullName.trim();
    if (departmentId !== undefined) emp.department = departmentId || null;
    if (employeeCode !== undefined) emp.employeeCode = employeeCode?.trim();
    if (designation !== undefined)  emp.designation  = designation?.trim();
    if (typeof isActive === "boolean") emp.isActive = isActive;

    await emp.save();
    const populated = await emp.populate("department", "name");
    return res.json({ ok: true, employee: populated });
  } catch (e) {
    err(res, e.message);
  }
}

// DELETE /api/admin/employees/:id
export async function deactivateEmployee(req, res) {
  try {
    const emp = await Employee.findOne({ _id: req.params.id, company: req.auth.companyId });
    if (!emp) return err(res, "Employee not found", 404);
    emp.isActive = false;
    await emp.save();
    return res.json({ ok: true, message: "Employee deactivated." });
  } catch (e) {
    err(res, e.message);
  }
}

// ─── DEPARTMENTS ─────────────────────────────────────────────────────────────

// GET /api/admin/departments
export async function getDepartments(req, res) {
  try {
    const depts = await Department.find({ company: req.auth.companyId, isActive: true });
    const empCounts = await Employee.aggregate([
      { $match: { company: req.auth.companyId, isActive: true } },
      { $group: { _id: "$department", count: { $sum: 1 } } },
    ]);
    const countMap = {};
    for (const e of empCounts) if (e._id) countMap[String(e._id)] = e.count;

    return res.json({
      ok: true,
      departments: depts.map((d) => ({
        ...d.toObject(),
        employeeCount: countMap[String(d._id)] || 0,
      })),
    });
  } catch (e) {
    err(res, e.message);
  }
}

// POST /api/admin/departments
export async function addDepartment(req, res) {
  try {
    const { name, description } = req.body;
    if (!name) return err(res, "name is required", 400);
    const dept = await Department.create({ company: req.auth.companyId, name: name.trim(), description });
    return res.status(201).json({ ok: true, department: dept });
  } catch (e) {
    err(res, e.message);
  }
}

// PUT /api/admin/departments/:id
export async function updateDepartment(req, res) {
  try {
    const { name, description } = req.body;
    const dept = await Department.findOne({ _id: req.params.id, company: req.auth.companyId });
    if (!dept) return err(res, "Department not found", 404);
    if (name) dept.name = name.trim();
    if (description !== undefined) dept.description = description;
    await dept.save();
    return res.json({ ok: true, department: dept });
  } catch (e) {
    err(res, e.message);
  }
}

// DELETE /api/admin/departments/:id
export async function deleteDepartment(req, res) {
  try {
    const count = await Employee.countDocuments({ department: req.params.id, isActive: true });
    if (count > 0) return err(res, `Cannot delete: ${count} active employee(s) are in this department.`, 400);
    await Department.findOneAndUpdate(
      { _id: req.params.id, company: req.auth.companyId },
      { isActive: false }
    );
    return res.json({ ok: true });
  } catch (e) {
    err(res, e.message);
  }
}

// ─── GEOFENCES ───────────────────────────────────────────────────────────────

// GET /api/admin/geofences
export async function getGeofences(req, res) {
  try {
    const geofences = await Geofence.find({ company: req.auth.companyId });
    return res.json({ ok: true, geofences });
  } catch (e) {
    err(res, e.message);
  }
}

// POST /api/admin/geofences
export async function addGeofence(req, res) {
  try {
    const { name, latitude, longitude, radiusMeters, address } = req.body;
    if (!name || latitude === undefined || longitude === undefined)
      return err(res, "name, latitude, and longitude are required", 400);
    const gf = await Geofence.create({
      company: req.auth.companyId,
      name: name.trim(),
      latitude:     Number(latitude),
      longitude:    Number(longitude),
      radiusMeters: Number(radiusMeters) || 100,
      address:      address?.trim(),
    });
    return res.status(201).json({ ok: true, geofence: gf });
  } catch (e) {
    err(res, e.message);
  }
}

// PUT /api/admin/geofences/:id
export async function updateGeofence(req, res) {
  try {
    const { name, latitude, longitude, radiusMeters, address, isActive } = req.body;
    const gf = await Geofence.findOne({ _id: req.params.id, company: req.auth.companyId });
    if (!gf) return err(res, "Geofence not found", 404);
    if (name !== undefined)         gf.name         = name.trim();
    if (latitude !== undefined)     gf.latitude     = Number(latitude);
    if (longitude !== undefined)    gf.longitude    = Number(longitude);
    if (radiusMeters !== undefined) gf.radiusMeters = Number(radiusMeters);
    if (address !== undefined)      gf.address      = address?.trim();
    if (typeof isActive === "boolean") gf.isActive  = isActive;
    await gf.save();
    return res.json({ ok: true, geofence: gf });
  } catch (e) {
    err(res, e.message);
  }
}

// DELETE /api/admin/geofences/:id
export async function deleteGeofence(req, res) {
  try {
    await Geofence.findOneAndDelete({ _id: req.params.id, company: req.auth.companyId });
    return res.json({ ok: true });
  } catch (e) {
    err(res, e.message);
  }
}

// ─── MANUAL ATTENDANCE ───────────────────────────────────────────────────────

// POST /api/admin/attendance/manual
export async function manualAttendance(req, res) {
  try {
    const { employeeId, date, status, checkInTime, checkOutTime, notes } = req.body;
    if (!employeeId || !date) return err(res, "employeeId and date are required", 400);

    const emp = await Employee.findOne({ _id: employeeId, company: req.auth.companyId });
    if (!emp) return err(res, "Employee not found", 404);

    const checkIn = checkInTime
      ? new Date(`${date}T${checkInTime}:00+05:30`)
      : undefined;
    const checkOut = checkOutTime
      ? new Date(`${date}T${checkOutTime}:00+05:30`)
      : undefined;
    const workingHours = checkIn && checkOut
      ? Math.round(((checkOut - checkIn) / 3600000) * 100) / 100
      : undefined;

    const record = await Attendance.findOneAndUpdate(
      { employee: employeeId, date, company: req.auth.companyId },
      {
        $set: {
          company:       req.auth.companyId,
          employee:      employeeId,
          date,
          status:        status || "present",
          checkInTime:   checkIn,
          checkOutTime:  checkOut,
          workingHours,
          isManualEntry: true,
          notes,
        },
      },
      { upsert: true, new: true }
    );

    return res.json({ ok: true, record });
  } catch (e) {
    err(res, e.message);
  }
}

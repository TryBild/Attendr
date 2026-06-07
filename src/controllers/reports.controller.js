import { Employee, AttendanceLog } from "../models/index.js";
import { dayStart, dayEnd, monthRange } from "../utils/dateUtils.js";
import { buildMusterRollCsv } from "../utils/csvBuilder.js";
import { ok, err } from "../utils/response.js";

export async function getDayRegister(req, res) {
  try {
    const date = req.query.date ? new Date(req.query.date) : new Date();
    const employees = await Employee.find({ companyId: req.auth.companyId, active: true })
      .populate("departmentId", "name").sort({ name: 1 });

    const logs = await AttendanceLog.find({
      companyId: req.auth.companyId,
      at: { $gte: dayStart(date), $lte: dayEnd(date) },
    }).sort({ at: 1 });

    const byEmp = {};
    for (const l of logs) {
      const k = String(l.employeeId);
      byEmp[k] = byEmp[k] || { in: null, out: null, flagged: false };
      if (l.type === "in"  && !byEmp[k].in)  byEmp[k].in  = l.at;
      if (l.type === "out")                   byEmp[k].out = l.at;
      if (l.mockDetected)                     byEmp[k].flagged = true;
    }

    const rows = employees.map((e) => {
      const r = byEmp[String(e._id)];
      return {
        id:         e._id,
        name:       e.name,
        department: e.departmentId?.name || "-",
        status:     r ? "present" : "absent",
        checkIn:    r?.in  || null,
        checkOut:   r?.out || null,
        flagged:    r?.flagged || false,
      };
    });

    return res.json({
      ok: true,
      date,
      present: rows.filter((r) => r.status === "present").length,
      total:   rows.length,
      rows,
    });
  } catch (e) { err(res, e.message); }
}

export async function getMonthCsv(req, res) {
  try {
    const year  = Number(req.query.year)  || new Date().getFullYear();
    const month = Number(req.query.month) || new Date().getMonth() + 1;
    const { start, end } = monthRange(year, month);

    const employees = await Employee.find({ companyId: req.auth.companyId, active: true })
      .populate("departmentId", "name").sort({ name: 1 });

    const logs = await AttendanceLog.find({
      companyId: req.auth.companyId,
      type: "in",
      at: { $gte: start, $lt: end },
    });

    const presentMap = {};
    for (const l of logs) {
      const k = String(l.employeeId);
      (presentMap[k] = presentMap[k] || new Set()).add(new Date(l.at).getDate());
    }

    const csv = buildMusterRollCsv(employees, presentMap, year, month);
    res.setHeader("Content-Type", "text/csv");
    res.setHeader("Content-Disposition", `attachment; filename="muster-roll-${year}-${month}.csv"`);
    return res.send(csv);
  } catch (e) { err(res, e.message); }
}

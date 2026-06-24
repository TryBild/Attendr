import { Company, Employee, Attendance } from "../models/index.js";
import { buildMusterRollGrid } from "../utils/csvBuilder.js";
import { err } from "../utils/response.js";

function daysInMonth(year, month) {
  return new Date(year, month, 0).getDate();
}

function monthName(month) {
  return ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"][month - 1];
}

function isWeekend(year, month, day) {
  const d = new Date(year, month - 1, day).getDay();
  return d === 0 || d === 6;
}

// GET /api/reports/month.csv?month=2026-06
export async function getMonthCsv(req, res) {
  try {
    const companyId = req.auth.companyId;
    const monthParam = req.query.month || new Date().toLocaleDateString("en-CA", { timeZone: "Asia/Kolkata" }).slice(0, 7);
    const [year, month] = monthParam.split("-").map(Number);
    const days = daysInMonth(year, month);

    const employees = await Employee.find({ company: companyId, isActive: true })
      .populate("department", "name")
      .sort({ fullName: 1 });

    const padMonth = String(month).padStart(2, "0");
    const startDate = `${year}-${padMonth}-01`;
    const endDate   = `${year}-${padMonth}-${days}`;

    const records = await Attendance.find({
      company: companyId,
      date:    { $gte: startDate, $lte: endDate },
    });

    const recordMap = {};
    for (const r of records) {
      const key = `${r.employee}_${r.date}`;
      recordMap[key] = r.status;
    }

    // Build CSV headers
    const dayHeaders = Array.from({ length: days }, (_, i) => {
      const d = i + 1;
      return `${d}-${monthName(month)}`;
    });

    const headers = [
      "Employee Code", "Full Name", "Department",
      ...dayHeaders,
      "Present Days", "Absent Days", "Late Days", "Attendance %",
    ];

    const rows = employees.map((emp) => {
      let present = 0, absent = 0, late = 0, leaves = 0;
      const dayCells = [];

      for (let d = 1; d <= days; d++) {
        const dateStr = `${year}-${padMonth}-${String(d).padStart(2, "0")}`;
        const key = `${emp._id}_${dateStr}`;
        const status = recordMap[key];
        const weekend = isWeekend(year, month, d);

        if (weekend) {
          dayCells.push("—");
        } else if (!status) {
          dayCells.push("A");
          absent++;
        } else if (status === "present") {
          dayCells.push("P");
          present++;
        } else if (status === "late") {
          dayCells.push("L");
          late++;
          present++; // count late as present for %
        } else if (status === "leave") {
          dayCells.push("LV");
          leaves++;
        } else if (status === "holiday") {
          dayCells.push("H");
        } else if (status === "absent") {
          dayCells.push("A");
          absent++;
        } else {
          dayCells.push(status.slice(0, 2).toUpperCase());
        }
      }

      const workingDays = days - Array.from({ length: days }, (_, i) => isWeekend(year, month, i + 1)).filter(Boolean).length;
      const pct = workingDays > 0 ? Math.round(((present) / workingDays) * 100) : 0;

      return [
        emp.employeeCode || "-",
        `"${emp.fullName}"`,
        `"${emp.department?.name || "General"}"`,
        ...dayCells,
        present,
        absent,
        late,
        `${pct}%`,
      ];
    });

    const mn = monthName(month);
    const csvLines = [headers.join(","), ...rows.map((r) => r.join(","))];
    const csv = csvLines.join("\n");

    res.setHeader("Content-Type", "text/csv; charset=utf-8");
    res.setHeader("Content-Disposition", `attachment; filename="attendance-${mn.toLowerCase()}-${year}.csv"`);
    return res.send(csv);
  } catch (e) {
    console.error(e);
    err(res, e.message);
  }
}

// GET /api/reports/month?month=2026-06 (JSON for frontend table)
export async function getMonthJson(req, res) {
  try {
    const companyId = req.auth.companyId;
    const monthParam = req.query.month || new Date().toLocaleDateString("en-CA", { timeZone: "Asia/Kolkata" }).slice(0, 7);
    const [year, month] = monthParam.split("-").map(Number);
    const days = daysInMonth(year, month);

    const employees = await Employee.find({ company: companyId, isActive: true })
      .populate("department", "name")
      .sort({ fullName: 1 });

    const padMonth = String(month).padStart(2, "0");
    const startDate = `${year}-${padMonth}-01`;
    const endDate   = `${year}-${padMonth}-${days}`;

    const records = await Attendance.find({
      company: companyId,
      date:    { $gte: startDate, $lte: endDate },
    });

    const recordMap = {};
    for (const r of records) {
      const key = `${r.employee}_${r.date}`;
      recordMap[key] = r.status;
    }

    const rows = employees.map((emp) => {
      let present = 0, absent = 0, late = 0;
      const days_ = [];

      for (let d = 1; d <= days; d++) {
        const dateStr = `${year}-${padMonth}-${String(d).padStart(2, "0")}`;
        const key = `${emp._id}_${dateStr}`;
        const status = recordMap[key];
        const weekend = isWeekend(year, month, d);
        let cell;

        if (weekend) cell = "WE";
        else if (!status) { cell = "A"; absent++; }
        else if (status === "present") { cell = "P"; present++; }
        else if (status === "late") { cell = "L"; late++; present++; }
        else if (status === "leave") { cell = "LV"; }
        else if (status === "holiday") { cell = "H"; }
        else { cell = "A"; absent++; }

        days_.push(cell);
      }

      const workingDays = days - Array.from({ length: days }, (_, i) => isWeekend(year, month, i + 1)).filter(Boolean).length;
      const pct = workingDays > 0 ? Math.round((present / workingDays) * 100) : 0;

      return {
        employeeId:   emp._id,
        employeeCode: emp.employeeCode || "-",
        fullName:     emp.fullName,
        department:   emp.department?.name || "General",
        days:         days_,
        present,
        absent,
        late,
        attendancePct: pct,
      };
    });

    return res.json({ ok: true, month: monthParam, days, rows });
  } catch (e) {
    console.error(e);
    err(res, e.message);
  }
}

// GET /api/reports/register/month.csv?month=2026-06
export async function getMusterRollCsv(req, res) {
  try {
    const companyId = req.auth.companyId;
    const monthParam = req.query.month || new Date().toLocaleDateString("en-CA", { timeZone: "Asia/Kolkata" }).slice(0, 7);
    const [year, month] = monthParam.split("-").map(Number);
    const days = daysInMonth(year, month);

    const [company, employees, records] = await Promise.all([
      Company.findById(companyId).lean(),
      Employee.find({ company: companyId, isActive: true })
        .populate("department", "name")
        .sort({ fullName: 1 }),
      Attendance.find({
        company: companyId,
        date: {
          $gte: `${year}-${String(month).padStart(2, "0")}-01`,
          $lte: `${year}-${String(month).padStart(2, "0")}-${days}`,
        },
      }),
    ]);

    if (!company) return err(res, "Company not found", 404);

    const csv = buildMusterRollGrid(employees, records, year, month, company);
    const mn = monthName(month);

    res.setHeader("Content-Type", "text/csv; charset=utf-8");
    res.setHeader("Content-Disposition", `attachment; filename="muster_roll_${year}_${String(month).padStart(2, "0")}.csv"`);
    return res.send(csv);
  } catch (e) {
    console.error(e);
    err(res, e.message);
  }
}

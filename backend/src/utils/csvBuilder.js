import { daysInMonth } from "./dateUtils.js";

const MONTH_NAMES = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"];

const DAY_INDEX = { Sun: 0, Mon: 1, Tue: 2, Wed: 3, Thu: 4, Fri: 5, Sat: 6 };

function escapeCsv(val) {
  const s = String(val ?? "");
  return s.includes(",") || s.includes('"') || s.includes("\n") ? `"${s.replace(/"/g, '""')}"` : s;
}

export function buildMusterRollGrid(employees, attendanceRecords, year, month, company) {
  const days = daysInMonth(year, month);
  const padMonth = String(month).padStart(2, "0");
  const mn = MONTH_NAMES[month - 1];

  const workDayNums = new Set(
    (company.workDays || []).map((d) => DAY_INDEX[d]).filter((n) => n !== undefined),
  );
  const hasWorkDays = workDayNums.size > 0;

  const holidaySet = new Set(company.holidays || []);

  const recordMap = {};
  for (const r of attendanceRecords) {
    const key = `${r.employee}_${r.date}`;
    recordMap[key] = r.status;
  }

  const dayHeaders = Array.from({ length: days }, (_, i) => `${i + 1}-${mn}`);
  const headers = [
    "S.No",
    "Employee Code",
    "Full Name",
    "Department",
    ...dayHeaders,
    "Total Present",
    "Total Absent",
    "Total Late",
    "Total Leave",
    "Attendance %",
  ];

  const rows = employees.map((emp, idx) => {
    let present = 0, absent = 0, late = 0, leaves = 0, workingDays = 0;
    const dayCells = [];

    for (let d = 1; d <= days; d++) {
      const dateStr = `${year}-${padMonth}-${String(d).padStart(2, "0")}`;
      const key = `${emp._id}_${dateStr}`;
      const status = recordMap[key];
      const jsDay = new Date(year, month - 1, d).getDay();
      const isNonWorkDay = hasWorkDays ? !workDayNums.has(jsDay) : (jsDay === 0 || jsDay === 6);
      const isHoliday = holidaySet.has(dateStr);

      if (isHoliday) {
        dayCells.push("H");
      } else if (isNonWorkDay) {
        dayCells.push("-");
      } else {
        workingDays++;
        if (!status || status === "absent") {
          dayCells.push("A");
          absent++;
        } else if (status === "present") {
          dayCells.push("P");
          present++;
        } else if (status === "late") {
          dayCells.push("L");
          late++;
          present++;
        } else if (status === "leave") {
          dayCells.push("LV");
          leaves++;
        } else if (status === "half-day") {
          dayCells.push("HD");
          present += 0.5;
        } else if (status === "holiday") {
          dayCells.push("H");
          workingDays--;
        } else {
          dayCells.push(status.slice(0, 2).toUpperCase());
        }
      }
    }

    const pct = workingDays > 0 ? Math.round((present / workingDays) * 100) : 0;

    return [
      idx + 1,
      escapeCsv(emp.employeeCode || "-"),
      escapeCsv(emp.fullName),
      escapeCsv(emp.department?.name || "General"),
      ...dayCells,
      Math.round(present),
      absent,
      late,
      leaves,
      `${pct}%`,
    ];
  });

  return [headers, ...rows].map((r) => r.join(",")).join("\n");
}

import { daysInMonth } from "./dateUtils.js";

// Builds monthly muster roll CSV string
// presentMap: { employeeId (string) -> Set<dayNumber> }
export function buildMusterRollCsv(employees, presentMap, year, month) {
  const days = daysInMonth(year, month);
  const headers = [
    "S.No", "Employee", "Department",
    ...Array.from({ length: days }, (_, i) => i + 1),
    "Total Present",
  ];

  const rows = employees.map((emp, i) => {
    const set = presentMap[String(emp._id)] || new Set();
    const marks = Array.from({ length: days }, (_, d) => (set.has(d + 1) ? "P" : "A"));
    return [i + 1, emp.name, emp.departmentId?.name || "-", ...marks, set.size];
  });

  return [headers, ...rows].map((r) => r.join(",")).join("\n");
}

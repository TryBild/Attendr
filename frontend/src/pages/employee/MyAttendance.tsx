import { useState } from "react";
import { Link } from "react-router-dom";
import { ChevronLeft, ChevronRight, Home, Calendar, User } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { getMyAttendance } from "../../api/attendance";
import { currentMonthIST, formatDateTime } from "../../lib/utils";

const STATUS_COLOR: Record<string, string> = {
  present:   "bg-green-100 text-green-700",
  late:      "bg-orange-100 text-orange-700",
  absent:    "bg-red-100 text-red-700",
  leave:     "bg-blue-100 text-blue-700",
  holiday:   "bg-gray-100 text-gray-600",
  not_marked:"bg-gray-50 text-gray-400",
};

const STATUS_DOT: Record<string, string> = {
  present: "bg-green-500",
  late:    "bg-orange-500",
  absent:  "bg-red-500",
  leave:   "bg-blue-500",
  holiday: "bg-gray-400",
};

export default function MyAttendance() {
  const [month, setMonth] = useState(currentMonthIST());

  const { data, isLoading } = useQuery({
    queryKey: ["my-attendance", month],
    queryFn:  () => getMyAttendance(month),
  });

  function prevMonth() {
    const [y, m] = month.split("-").map(Number);
    const d = new Date(y, m - 2);
    setMonth(d.toLocaleDateString("en-CA", {}).slice(0, 7));
  }

  function nextMonth() {
    const [y, m] = month.split("-").map(Number);
    const d = new Date(y, m);
    const next = d.toLocaleDateString("en-CA", {}).slice(0, 7);
    if (next > currentMonthIST()) return;
    setMonth(next);
  }

  const [year, mon] = month.split("-").map(Number);
  const monthLabel = new Date(year, mon - 1).toLocaleDateString("en-IN", { month: "long", year: "numeric" });

  const recordMap: Record<string, string> = {};
  for (const r of data?.records || []) {
    recordMap[r.date] = r.status;
  }

  const daysInMonth = new Date(year, mon, 0).getDate();
  const firstDayOfMonth = new Date(year, mon - 1, 1).getDay();
  // Adjust to Mon=0
  const startOffset = (firstDayOfMonth + 6) % 7;

  const summary = data?.summary;

  return (
    <div className="min-h-screen bg-gray-50 max-w-md mx-auto pb-24">
      {/* Header */}
      <div className="bg-white px-5 pt-12 pb-5 shadow-sm">
        <h1 className="text-xl font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>My Attendance</h1>

        {/* Month Picker */}
        <div className="flex items-center justify-between mt-4">
          <button onClick={prevMonth} className="p-2 hover:bg-gray-100 rounded-full">
            <ChevronLeft size={20} className="text-gray-600" />
          </button>
          <span className="font-semibold text-gray-800">{monthLabel}</span>
          <button onClick={nextMonth} disabled={month >= currentMonthIST()} className="p-2 hover:bg-gray-100 rounded-full disabled:opacity-30">
            <ChevronRight size={20} className="text-gray-600" />
          </button>
        </div>
      </div>

      {/* Summary chips */}
      {summary && (
        <div className="px-5 py-4 grid grid-cols-4 gap-2">
          {[
            { label: "Present", value: summary.present, color: "bg-green-50 text-green-700" },
            { label: "Absent",  value: summary.absent,  color: "bg-red-50 text-red-700" },
            { label: "Late",    value: summary.late,    color: "bg-orange-50 text-orange-700" },
            { label: "Attnd %", value: `${summary.attendancePercent}%`, color: "bg-blue-50 text-blue-700" },
          ].map(({ label, value, color }) => (
            <div key={label} className={`rounded-xl p-2 text-center ${color}`}>
              <p className="text-lg font-bold">{value}</p>
              <p className="text-xs font-medium opacity-80">{label}</p>
            </div>
          ))}
        </div>
      )}

      {/* Calendar */}
      <div className="mx-5 bg-white rounded-2xl shadow-sm p-4 mb-4">
        {/* Day headers */}
        <div className="grid grid-cols-7 mb-2">
          {["M","T","W","T","F","S","S"].map((d, i) => (
            <div key={i} className={`text-center text-xs font-semibold py-1 ${i >= 5 ? "text-gray-300" : "text-gray-400"}`}>{d}</div>
          ))}
        </div>
        {/* Day cells */}
        <div className="grid grid-cols-7 gap-1">
          {Array(startOffset).fill(null).map((_, i) => <div key={`e${i}`} />)}
          {Array.from({ length: daysInMonth }, (_, i) => {
            const day = i + 1;
            const dateStr = `${year}-${String(mon).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
            const today = new Date().toLocaleDateString("en-CA", { timeZone: "Asia/Kolkata" });
            const isToday = dateStr === today;
            const status = recordMap[dateStr];
            const dayOfWeek = new Date(year, mon - 1, day).getDay();
            const isWeekend = dayOfWeek === 0 || dayOfWeek === 6;

            return (
              <div key={day} className={`flex flex-col items-center py-2 rounded-xl ${isToday ? "bg-blue-900" : ""}`}>
                <span className={`text-xs font-medium ${isToday ? "text-white" : isWeekend ? "text-gray-300" : "text-gray-700"}`}>
                  {day}
                </span>
                {!isWeekend && (
                  <div className={`w-2 h-2 rounded-full mt-1 ${
                    status === "present" ? "bg-green-500" :
                    status === "late"    ? "bg-orange-500" :
                    status === "absent"  ? "bg-red-500" :
                    status === "leave"   ? "bg-blue-500" :
                    isToday             ? "bg-blue-300" :
                    "bg-gray-200"
                  }`} />
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Records list */}
      <div className="mx-5 space-y-2">
        {isLoading ? (
          <div className="flex justify-center py-8">
            <svg className="animate-spin w-6 h-6 border-2 border-blue-600 border-t-transparent rounded-full" viewBox="0 0 24 24" />
          </div>
        ) : data?.records.length === 0 ? (
          <div className="bg-white rounded-2xl p-6 text-center text-gray-400">
            No attendance records for this month.
          </div>
        ) : (
          [...(data?.records || [])].reverse().map((r) => (
            <div key={r.date} className="bg-white rounded-2xl p-4 flex items-center gap-3 shadow-sm">
              <div>
                <p className="text-sm font-semibold text-gray-800">
                  {new Date(r.date + "T00:00:00").toLocaleDateString("en-IN", { weekday: "short", day: "numeric", month: "short" })}
                </p>
                <div className="flex gap-2 mt-1 text-xs text-gray-500">
                  {r.checkInTime && <span>In: {formatDateTime(r.checkInTime)}</span>}
                  {r.checkOutTime && <span>· Out: {formatDateTime(r.checkOutTime)}</span>}
                  {r.workingHours && <span>· {r.workingHours.toFixed(1)}h</span>}
                </div>
              </div>
              <span className={`ml-auto text-xs font-semibold px-3 py-1 rounded-full ${STATUS_COLOR[r.status] || "bg-gray-100 text-gray-500"}`}>
                {r.status.charAt(0).toUpperCase() + r.status.slice(1)}
              </span>
            </div>
          ))
        )}
      </div>

      {/* Bottom Nav */}
      <nav className="fixed bottom-0 left-1/2 -translate-x-1/2 w-full max-w-md bg-white border-t border-gray-200 flex safe-bottom">
        <Link to="/employee/home" className="flex-1 flex flex-col items-center py-3 text-gray-400 text-xs gap-1">
          <Home size={22} />Home
        </Link>
        <Link to="/employee/attendance" className="flex-1 flex flex-col items-center py-3 text-blue-900 font-semibold text-xs gap-1">
          <Calendar size={22} />Attendance
        </Link>
        <Link to="/employee/profile" className="flex-1 flex flex-col items-center py-3 text-gray-400 text-xs gap-1">
          <User size={22} />Profile
        </Link>
      </nav>
    </div>
  );
}

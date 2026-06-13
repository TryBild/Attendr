import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import {
  format, startOfMonth, endOfMonth, eachDayOfInterval,
  getDay, addMonths, subMonths,
} from "date-fns";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { getDayRegister } from "../../api/admin";
import { todayIST, getInitials } from "../../lib/utils";
import { HOLIDAYS_2026 } from "../../data/holidays";
import { AdminLayout } from "../../components/AdminLayout";

const DAY_HEADERS = ["Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"];

const STATUS_CHIP: Record<string, string> = {
  present: "bg-green-100 text-green-700",
  late:    "bg-orange-100 text-orange-700",
  absent:  "bg-gray-100 text-gray-500",
};

export default function Calendar() {
  const todayStr = todayIST();
  const [currentMonth, setCurrentMonth] = useState(() => {
    const [y, m] = todayStr.split("-").map(Number);
    return new Date(y, m - 1, 1);
  });
  const [selectedDate, setSelectedDate] = useState<string | null>(null);

  const start = startOfMonth(currentMonth);
  const end   = endOfMonth(currentMonth);
  const days  = eachDayOfInterval({ start, end });

  // Mon-based offset: Sun=0 in JS → offset=(0+6)%7=6; Mon=1→0; Tue=2→1 etc.
  const leadingEmpty  = (getDay(start) + 6) % 7;
  const totalCells    = Math.ceil((leadingEmpty + days.length) / 7) * 7;
  const trailingEmpty = totalCells - leadingEmpty - days.length;

  const holidayMap = Object.fromEntries(HOLIDAYS_2026.map((h) => [h.date, h.name]));

  const selectedHoliday = selectedDate ? holidayMap[selectedDate] : null;

  const { data: dayData, isLoading: dayLoading } = useQuery({
    queryKey: ["day-register", selectedDate],
    queryFn:  () => getDayRegister(selectedDate!),
    enabled:  !!selectedDate,
  });

  function toggleDate(dateStr: string) {
    setSelectedDate((prev) => (prev === dateStr ? null : dateStr));
  }

  return (
    <AdminLayout>
      <div className="min-h-screen max-w-2xl mx-auto pb-24" style={{ backgroundColor: "#F5F7FB" }}>

        {/* Top bar */}
        <div className="bg-white px-5 pt-12 pb-4 shadow-sm">
          <div className="flex items-center justify-between">
            <button
              onClick={() => setCurrentMonth((m) => subMonths(m, 1))}
              className="p-2 hover:bg-gray-100 rounded-full transition"
            >
              <ChevronLeft size={20} className="text-gray-600" />
            </button>
            <h1 className="text-lg font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>
              {format(currentMonth, "MMMM yyyy")}
            </h1>
            <button
              onClick={() => setCurrentMonth((m) => addMonths(m, 1))}
              className="p-2 hover:bg-gray-100 rounded-full transition"
            >
              <ChevronRight size={20} className="text-gray-600" />
            </button>
          </div>
        </div>

        <div className="px-5 py-4 space-y-4">

          {/* Holiday legend */}
          <div className="flex items-center gap-3 text-xs text-gray-500">
            <span className="inline-flex items-center gap-1.5">
              <span className="w-3 h-3 rounded-sm bg-orange-100 border border-orange-300" />
              Public holiday
            </span>
            <span className="inline-flex items-center gap-1.5">
              <span className="w-3 h-3 rounded-full ring-2 ring-[#1B3A7B]" />
              Today
            </span>
          </div>

          {/* Calendar grid */}
          <div className="bg-white rounded-2xl shadow-sm p-4">
            {/* Headers */}
            <div className="grid grid-cols-7 mb-1">
              {DAY_HEADERS.map((h) => (
                <div key={h} className="text-center text-xs font-semibold text-gray-400 py-1">
                  {h}
                </div>
              ))}
            </div>

            {/* Day cells */}
            <div className="grid grid-cols-7 gap-1">
              {Array.from({ length: leadingEmpty }).map((_, i) => (
                <div key={`l${i}`} />
              ))}

              {days.map((day) => {
                const dateStr   = format(day, "yyyy-MM-dd");
                const isHoliday = !!holidayMap[dateStr];
                const isToday   = dateStr === todayStr;
                const isSelected = selectedDate === dateStr;
                const isFuture  = dateStr > todayStr;

                return (
                  <button
                    key={dateStr}
                    onClick={() => !isFuture && toggleDate(dateStr)}
                    disabled={isFuture}
                    className={[
                      "aspect-square flex items-center justify-center rounded-xl text-sm font-medium transition select-none",
                      isSelected ? "bg-[#1B3A7B] text-white shadow-sm" : "",
                      isHoliday && !isSelected ? "bg-orange-100 text-orange-700" : "",
                      isToday && !isSelected ? "ring-2 ring-[#1B3A7B] text-[#1B3A7B]" : "",
                      !isSelected && !isHoliday && !isToday ? "text-gray-700 hover:bg-gray-100" : "",
                      isFuture ? "opacity-35 cursor-default" : "cursor-pointer",
                    ].filter(Boolean).join(" ")}
                  >
                    {format(day, "d")}
                  </button>
                );
              })}

              {Array.from({ length: trailingEmpty }).map((_, i) => (
                <div key={`t${i}`} />
              ))}
            </div>
          </div>

          {/* Day detail panel */}
          {selectedDate && (
            <div className="bg-white rounded-2xl shadow-sm overflow-hidden">
              <div className="px-4 py-3 border-b border-gray-50">
                <p className="font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>
                  {format(new Date(selectedDate + "T00:00:00"), "EEEE, d MMMM yyyy")}
                </p>
                {selectedHoliday && (
                  <p className="text-xs text-orange-600 mt-0.5">
                    Public holiday · {selectedHoliday}
                  </p>
                )}
                {dayData && (
                  <p className="text-xs text-gray-400 mt-0.5">
                    {dayData.present} present · {dayData.total} total
                  </p>
                )}
              </div>

              {dayLoading ? (
                <div className="py-8 flex justify-center">
                  <svg className="animate-spin w-5 h-5 border-2 border-blue-600 border-t-transparent rounded-full" viewBox="0 0 24 24" />
                </div>
              ) : (dayData?.rows || []).length === 0 ? (
                <p className="text-center text-gray-400 text-sm py-8">
                  {selectedHoliday ? "Holiday — no attendance records" : "No attendance records for this date"}
                </p>
              ) : (
                (dayData?.rows || []).slice(0, 10).map((r: any, i: number) => (
                  <div key={r.employeeId} className={`px-4 py-3 flex items-center gap-3 ${i > 0 ? "border-t border-gray-50" : ""}`}>
                    <div className="w-9 h-9 bg-blue-50 rounded-full flex items-center justify-center flex-shrink-0">
                      <span className="text-xs font-bold text-blue-700">{getInitials(r.fullName)}</span>
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-semibold text-gray-800 truncate">{r.fullName}</p>
                      <p className="text-xs text-gray-400">
                        {r.department}
                        {r.checkInTime ? ` · In: ${r.checkInTime}` : ""}
                      </p>
                    </div>
                    <span className={`text-xs font-semibold px-2 py-0.5 rounded-full flex-shrink-0 ${STATUS_CHIP[r.status] || "bg-gray-100 text-gray-500"}`}>
                      {r.status.charAt(0).toUpperCase() + r.status.slice(1)}
                    </span>
                  </div>
                ))
              )}

              {(dayData?.rows || []).length > 10 && (
                <p className="text-center text-xs text-gray-400 py-3 border-t border-gray-50">
                  +{dayData!.rows.length - 10} more · <a href="/admin/day-register" className="text-blue-700">View full register</a>
                </p>
              )}
            </div>
          )}
        </div>
      </div>
    </AdminLayout>
  );
}

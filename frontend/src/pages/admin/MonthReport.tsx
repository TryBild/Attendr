import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Download, ChevronLeft, ChevronRight } from "lucide-react";
import { getMonthReport, downloadMonthCsv } from "../../api/reports";
import { currentMonthIST } from "../../lib/utils";
import { AdminLayout } from "../../components/AdminLayout";
import toast from "react-hot-toast";

const CELL_COLOR: Record<string, string> = {
  P:  "bg-green-100 text-green-700",
  L:  "bg-orange-100 text-orange-700",
  A:  "bg-red-100 text-red-600",
  LV: "bg-blue-100 text-blue-700",
  H:  "bg-gray-100 text-gray-500",
  WE: "bg-gray-50 text-gray-300",
};

export default function MonthReport() {
  const [month, setMonth] = useState(currentMonthIST());

  const { data, isLoading } = useQuery({
    queryKey: ["month-report", month],
    queryFn: () => getMonthReport(month),
  });

  const [year, mon] = month.split("-").map(Number);
  const monthLabel = new Date(year, mon - 1).toLocaleDateString("en-IN", { month: "long", year: "numeric" });

  function prevMonth() {
    const d = new Date(year, mon - 2);
    setMonth(d.toLocaleDateString("en-CA", {}).slice(0, 7));
  }
  function nextMonth() {
    const d = new Date(year, mon);
    const next = d.toLocaleDateString("en-CA", {}).slice(0, 7);
    if (next > currentMonthIST()) return;
    setMonth(next);
  }

  async function handleDownload() {
    try { await downloadMonthCsv(month); toast.success("CSV downloaded!"); }
    catch { toast.error("Download failed"); }
  }

  const days = data?.days || 0;
  const dayNums = Array.from({ length: days }, (_, i) => i + 1);

  return (
    <AdminLayout>
    <div className="min-h-screen bg-gray-50 max-w-5xl mx-auto pb-24">
      <div className="bg-white px-5 pt-12 pb-4 shadow-sm">
        <div className="flex items-center gap-3 mb-4">
          <h1 className="text-xl font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>Month Report</h1>
          <button onClick={handleDownload} className="ml-auto flex items-center gap-1 bg-blue-900 text-white px-4 py-2 rounded-full text-sm font-semibold">
            <Download size={15} />Download CSV
          </button>
        </div>
        <div className="flex items-center justify-between">
          <button onClick={prevMonth} className="p-2 hover:bg-gray-100 rounded-full"><ChevronLeft size={20} className="text-gray-600" /></button>
          <span className="font-semibold text-gray-800">{monthLabel}</span>
          <button onClick={nextMonth} disabled={month >= currentMonthIST()} className="p-2 hover:bg-gray-100 rounded-full disabled:opacity-30"><ChevronRight size={20} className="text-gray-600" /></button>
        </div>
      </div>

      {/* Legend */}
      <div className="px-5 py-3 flex gap-3 flex-wrap text-xs">
        {[["P","Present","bg-green-100 text-green-700"],["L","Late","bg-orange-100 text-orange-700"],["A","Absent","bg-red-100 text-red-600"],["LV","Leave","bg-blue-100 text-blue-700"],["H","Holiday","bg-gray-100 text-gray-500"],["—","Weekend","bg-gray-50 text-gray-300"]].map(([code, label, cls]) => (
          <span key={code} className={`px-2 py-0.5 rounded font-semibold ${cls}`}>{code} = {label}</span>
        ))}
      </div>

      {/* Table */}
      <div className="mx-5 bg-white rounded-2xl shadow-sm overflow-x-auto">
        {isLoading ? (
          <div className="py-12 flex justify-center"><svg className="animate-spin w-6 h-6 border-2 border-blue-600 border-t-transparent rounded-full" viewBox="0 0 24 24" /></div>
        ) : (
          <table className="w-full text-xs min-w-max">
            <thead>
              <tr className="border-b border-gray-100">
                <th className="px-3 py-3 text-left font-semibold text-gray-600 sticky left-0 bg-white min-w-32">Employee</th>
                <th className="px-2 py-3 text-left font-semibold text-gray-600 min-w-20 hidden sm:table-cell">Dept</th>
                {dayNums.map((d) => (
                  <th key={d} className="px-1 py-3 text-center font-semibold text-gray-500 min-w-8">{d}</th>
                ))}
                <th className="px-2 py-3 text-center font-semibold text-gray-600">P</th>
                <th className="px-2 py-3 text-center font-semibold text-gray-600">A</th>
                <th className="px-2 py-3 text-center font-semibold text-gray-600">%</th>
              </tr>
            </thead>
            <tbody>
              {(data?.rows || []).map((row: any) => (
                <tr key={row.employeeId} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="px-3 py-2 sticky left-0 bg-white font-medium text-gray-800 truncate max-w-32">{row.fullName}</td>
                  <td className="px-2 py-2 text-gray-500 hidden sm:table-cell truncate max-w-20">{row.department}</td>
                  {(row.days || []).map((cell: string, i: number) => (
                    <td key={i} className="px-1 py-2 text-center">
                      <span className={`inline-block w-6 h-5 text-center rounded text-xs font-semibold leading-5 ${CELL_COLOR[cell] || "text-gray-300"}`}>
                        {cell === "WE" ? "—" : cell}
                      </span>
                    </td>
                  ))}
                  <td className="px-2 py-2 text-center font-bold text-green-700">{row.present}</td>
                  <td className="px-2 py-2 text-center font-bold text-red-600">{row.absent}</td>
                  <td className="px-2 py-2 text-center font-semibold text-blue-700">{row.attendancePct}%</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
    </AdminLayout>
  );
}

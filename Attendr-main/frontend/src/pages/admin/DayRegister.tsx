import { useState } from "react";
import { Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { ArrowLeft, Download } from "lucide-react";
import { getDayRegister } from "../../api/admin";
import { downloadMonthCsv } from "../../api/reports";
import { todayIST, currentMonthIST } from "../../lib/utils";
import toast from "react-hot-toast";

const STATUS_CHIP: Record<string, string> = {
  present: "bg-green-100 text-green-700",
  late:    "bg-orange-100 text-orange-700",
  absent:  "bg-red-100 text-red-600",
  leave:   "bg-blue-100 text-blue-700",
  holiday: "bg-gray-100 text-gray-600",
};

export default function DayRegister() {
  const [date, setDate]           = useState(todayIST());
  const [search, setSearch]       = useState("");
  const [deptFilter, setDeptFilter]   = useState("all");
  const [statusFilter, setStatusFilter] = useState("all");

  const { data, isLoading } = useQuery({
    queryKey: ["day-register", date],
    queryFn:  () => getDayRegister(date),
  });

  const departments = [...new Set((data?.rows || []).map((r: any) => r.department).filter(Boolean))] as string[];

  const filtered = (data?.rows || []).filter((r: any) => {
    const matchSearch = !search || r.fullName.toLowerCase().includes(search.toLowerCase());
    const matchDept   = deptFilter === "all" || r.department === deptFilter;
    const matchStatus = statusFilter === "all" || r.status === statusFilter;
    return matchSearch && matchDept && matchStatus;
  });

  async function handleDownload() {
    try { await downloadMonthCsv(currentMonthIST()); toast.success("Downloading..."); }
    catch { toast.error("Download failed"); }
  }

  return (
    <div className="min-h-screen bg-gray-50 max-w-2xl mx-auto pb-8">
      <div className="bg-white px-5 pt-12 pb-4 shadow-sm">
        <div className="flex items-center gap-3 mb-4">
          <Link to="/admin/dashboard" className="text-gray-500 hover:text-gray-700"><ArrowLeft size={20} /></Link>
          <h1 className="text-xl font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>Day Register</h1>
          <button onClick={handleDownload} className="ml-auto flex items-center gap-1 text-sm text-blue-700 font-medium">
            <Download size={16} />Export
          </button>
        </div>
        <input type="date" value={date} onChange={(e) => setDate(e.target.value)} max={todayIST()}
          className="w-full border border-gray-200 rounded-xl px-4 py-3 text-gray-800 bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-600" />
      </div>
      <div className="px-5 py-3 flex gap-2 overflow-x-auto">
        <input placeholder="Search name..." value={search} onChange={(e) => setSearch(e.target.value)}
          className="flex-shrink-0 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-800 bg-white focus:outline-none w-36" />
        <select value={deptFilter} onChange={(e) => setDeptFilter(e.target.value)}
          className="flex-shrink-0 border border-gray-200 rounded-lg px-3 py-2 text-sm bg-white text-gray-700">
          <option value="all">All Depts</option>
          {departments.map((d) => <option key={d} value={d}>{d}</option>)}
        </select>
        {["all","present","absent","late"].map((s) => (
          <button key={s} onClick={() => setStatusFilter(s)}
            className={`flex-shrink-0 px-3 py-2 rounded-lg text-xs font-semibold border transition ${statusFilter === s ? "bg-blue-900 text-white border-blue-900" : "bg-white text-gray-600 border-gray-200"}`}>
            {s.charAt(0).toUpperCase() + s.slice(1)}
          </button>
        ))}
      </div>
      {data && <p className="px-5 mb-2 text-sm text-gray-500"><span className="font-semibold text-green-600">{data.present} present</span> · {data.total} total</p>}
      <div className="mx-5 bg-white rounded-2xl shadow-sm overflow-hidden">
        {isLoading ? (
          <div className="py-12 flex justify-center"><svg className="animate-spin w-6 h-6 border-2 border-blue-600 border-t-transparent rounded-full" viewBox="0 0 24 24" /></div>
        ) : filtered.length === 0 ? (
          <p className="text-center text-gray-400 py-10 text-sm">No records found</p>
        ) : filtered.map((r: any, i: number) => (
          <div key={r.employeeId} className={`px-4 py-3 flex items-center gap-3 ${i > 0 ? "border-t border-gray-50" : ""} ${r.status === "absent" ? "opacity-60" : ""}`}>
            <div className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center flex-shrink-0">
              <span className="text-xs font-bold text-gray-600">{r.fullName.split(" ").slice(0,2).map((n: string) => n[0]).join("").toUpperCase()}</span>
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-semibold text-gray-800 truncate">{r.fullName}</p>
              <p className="text-xs text-gray-400">{r.department} {r.employeeCode !== "-" ? `· ${r.employeeCode}` : ""}</p>
            </div>
            <div className="text-right text-xs text-gray-500 mr-2">
              {r.checkInTime && <p>In: {r.checkInTime}</p>}
              {r.workingHours && <p>{r.workingHours.toFixed(1)}h</p>}
            </div>
            <span className={`text-xs font-semibold px-2 py-1 rounded-full flex-shrink-0 ${STATUS_CHIP[r.status] || "bg-gray-100 text-gray-500"}`}>
              {r.status.charAt(0).toUpperCase() + r.status.slice(1)}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}

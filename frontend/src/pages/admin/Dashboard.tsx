import { Link, useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { PieChart, Pie, Cell } from "recharts";
import { UserPlus, List, MapPin, Building2, CheckCircle2, Circle, Sparkles, Link2 } from "lucide-react";
import { getDashboard, getGeofences } from "../../api/admin";
import { useAuth } from "../../hooks/useAuth";
import { getInitials } from "../../lib/utils";
import { AdminLayout } from "../../components/AdminLayout";

const DONUT_COLORS = {
  onTime: "#22c55e",
  late:   "#f97316",
  absent: "#E5E7EB",
};

export default function Dashboard() {
  const { admin } = useAuth();
  const navigate = useNavigate();

  const { data, isLoading } = useQuery({
    queryKey: ["admin-dashboard"],
    queryFn:  getDashboard,
    refetchInterval: 60_000,
  });

  const { data: gfData } = useQuery({
    queryKey: ["geofences"],
    queryFn:  getGeofences,
  });

  const t = data?.today;
  const present = t?.present ?? 0;
  const late    = t?.late    ?? 0;
  const absent  = t?.absent  ?? 0;
  const total   = t?.totalEmployees ?? 0;
  const pct     = t?.attendancePercent ?? 0;

  const hasEmployees = total > 0;
  const hasGeofence  = (gfData?.geofences?.length ?? 0) > 0;

  const checklist = [
    { label: "Organization created",    done: true },
    { label: "Geofence configured",     done: hasGeofence },
    { label: "Employees added",         done: hasEmployees },
    { label: "Work hours configured",   done: admin?.setupComplete === true },
  ];
  const allDone = checklist.every((c) => c.done);

  const donutData = total === 0
    ? [{ name: "Empty", value: 1, color: "#E5E7EB" }]
    : [
        { name: "On time", value: Math.max(0, present - late), color: DONUT_COLORS.onTime },
        { name: "Late",    value: late,                         color: DONUT_COLORS.late   },
        { name: "Absent",  value: absent,                       color: DONUT_COLORS.absent },
      ];

  const today = new Date().toLocaleDateString("en-IN", {
    weekday: "long", day: "numeric", month: "long",
  });

  return (
    <AdminLayout>
      <div className="min-h-screen max-w-2xl mx-auto pb-24" style={{ backgroundColor: "#F5F7FB" }}>

        {/* Top bar */}
        <div className="bg-white px-5 pt-12 pb-4 shadow-sm">
          <div className="flex items-center justify-between">
            <div>
              <div className="flex items-center gap-2">
                <img src="/favicon.png" alt="Attendr" className="w-6 h-6 object-contain rounded-lg" />
                <span className="text-lg font-bold text-[#1B3A7B]" style={{ fontFamily: "Nunito, sans-serif" }}>
                  Attendr
                </span>
              </div>
              <p className="text-xs text-gray-400 mt-0.5">{today}</p>
            </div>
            <button
              onClick={() => navigate("/admin/profile")}
              className="w-10 h-10 rounded-full bg-[#1B3A7B] flex items-center justify-center flex-shrink-0 hover:bg-blue-800 transition"
            >
              <span className="text-sm font-bold text-white">
                {getInitials(admin?.name || "A")}
              </span>
            </button>
          </div>
        </div>

        <div className="px-5 py-5 space-y-4">

          {/* ── Onboarding card (no employees yet) ── */}
          {!isLoading && !hasEmployees && (
            <div className="bg-white rounded-2xl shadow-sm p-5">
              <div className="flex items-center gap-3 mb-4">
                <div className="w-10 h-10 bg-blue-50 rounded-2xl flex items-center justify-center flex-shrink-0">
                  <Sparkles size={20} className="text-blue-700" />
                </div>
                <div>
                  <h2 className="font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>
                    Welcome to Attendr!
                  </h2>
                  <p className="text-xs text-gray-500">Get started by adding your team</p>
                </div>
              </div>
              <div className="space-y-2.5">
                <Link
                  to="/admin/employees"
                  className="w-full flex items-center gap-3 bg-[#1B3A7B] text-white px-4 py-3 rounded-xl font-semibold text-sm hover:bg-blue-800 transition"
                >
                  <UserPlus size={18} />
                  Add Employees Manually
                </Link>
                <Link
                  to="/admin/employees"
                  className="w-full flex items-center gap-3 border border-blue-200 text-blue-700 px-4 py-3 rounded-xl font-semibold text-sm hover:bg-blue-50 transition"
                >
                  <Link2 size={18} />
                  Generate Department Link
                </Link>
              </div>
            </div>
          )}

          {/* ── Attendance Overview (donut) ── */}
          <div className="bg-white rounded-2xl shadow-sm p-5">
            <h2 className="font-bold text-gray-900 mb-4" style={{ fontFamily: "Nunito, sans-serif" }}>
              Today's Attendance
            </h2>
            {isLoading ? (
              <div className="flex justify-center py-8">
                <svg className="animate-spin w-6 h-6 border-2 border-blue-600 border-t-transparent rounded-full" viewBox="0 0 24 24" />
              </div>
            ) : (
              <div className="flex items-center gap-4">
                {/* Donut */}
                <div className="relative flex-shrink-0 w-36 h-36">
                  <PieChart width={144} height={144}>
                    <Pie
                      data={donutData}
                      cx={72}
                      cy={72}
                      innerRadius={48}
                      outerRadius={66}
                      startAngle={90}
                      endAngle={-270}
                      dataKey="value"
                      strokeWidth={2}
                      stroke="white"
                    >
                      {donutData.map((entry, i) => (
                        <Cell key={i} fill={entry.color} />
                      ))}
                    </Pie>
                  </PieChart>
                  <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
                    <p className="text-2xl font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>
                      {pct}%
                    </p>
                    <p className="text-[11px] text-gray-500">Present</p>
                  </div>
                </div>

                {/* Legend */}
                <div className="flex-1 space-y-2.5">
                  <div className="flex items-center gap-2">
                    <span className="w-2.5 h-2.5 rounded-full bg-green-500 flex-shrink-0" />
                    <span className="text-xs text-gray-600 flex-1">Present</span>
                    <span className="text-sm font-bold text-gray-900">{present}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="w-2.5 h-2.5 rounded-full bg-orange-400 flex-shrink-0" />
                    <span className="text-xs text-gray-600 flex-1">Late</span>
                    <span className="text-sm font-bold text-gray-900">{late}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="w-2.5 h-2.5 rounded-full bg-gray-200 flex-shrink-0" />
                    <span className="text-xs text-gray-600 flex-1">Absent</span>
                    <span className="text-sm font-bold text-gray-900">{absent}</span>
                  </div>
                  <div className="pt-2 border-t border-gray-100 flex items-center gap-2">
                    <span className="text-xs text-gray-400 flex-1">Total employees</span>
                    <span className="text-sm font-bold text-gray-700">{total}</span>
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* ── Setup checklist ── */}
          {!allDone && (
            <div className="bg-white rounded-2xl shadow-sm p-5">
              <h2 className="font-bold text-gray-900 mb-1" style={{ fontFamily: "Nunito, sans-serif" }}>
                Setup Checklist
              </h2>
              <p className="text-xs text-gray-400 mb-4">
                {checklist.filter((c) => c.done).length} / {checklist.length} complete
              </p>
              <div className="space-y-3">
                {checklist.map(({ label, done }) => (
                  <div key={label} className="flex items-center gap-3">
                    {done
                      ? <CheckCircle2 size={18} className="text-green-500 flex-shrink-0" />
                      : <Circle       size={18} className="text-gray-300 flex-shrink-0" />}
                    <span className={`text-sm ${done ? "text-gray-400 line-through" : "text-gray-700 font-medium"}`}>
                      {label}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* ── Recent Activity ── */}
          {data?.recentActivity && data.recentActivity.length > 0 && (
            <div className="bg-white rounded-2xl shadow-sm overflow-hidden">
              <div className="px-4 pt-4 pb-2 flex items-center justify-between">
                <h2 className="font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>
                  Recent Activity
                </h2>
                <Link to="/admin/day-register" className="text-xs text-blue-700 font-semibold">
                  View all →
                </Link>
              </div>
              {data.recentActivity.slice(0, 8).map((a: any, i: number) => (
                <div key={i} className="px-4 py-3 flex items-center gap-3 border-t border-gray-50">
                  <div className="w-9 h-9 bg-blue-50 rounded-full flex items-center justify-center flex-shrink-0">
                    <span className="text-xs font-bold text-blue-700">{getInitials(a.employeeName)}</span>
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-semibold text-gray-800 truncate">{a.employeeName}</p>
                    <p className="text-xs text-gray-400">
                      {a.action === "checkin" ? "Checked in" : "Checked out"} · {a.time}
                    </p>
                  </div>
                  <span className={`text-xs font-semibold px-2 py-0.5 rounded-full flex-shrink-0 ${
                    a.action === "checkin" ? "bg-green-100 text-green-700" : "bg-gray-100 text-gray-500"
                  }`}>
                    {a.action === "checkin" ? "In" : "Out"}
                  </span>
                </div>
              ))}
            </div>
          )}

          {/* ── Quick Links (sub-pages not in bottom nav) ── */}
          <div className="grid grid-cols-3 gap-3">
            {[
              { label: "Day Register",  to: "/admin/day-register", icon: List      },
              { label: "Geofences",     to: "/admin/geofences",    icon: MapPin    },
              { label: "Departments",   to: "/admin/departments",   icon: Building2 },
            ].map(({ label, to, icon: Icon }) => (
              <Link
                key={label}
                to={to}
                className="bg-white rounded-2xl p-4 shadow-sm flex flex-col items-center gap-2 hover:bg-blue-50 transition text-center"
              >
                <Icon size={20} className="text-blue-700" />
                <span className="text-xs font-semibold text-gray-700">{label}</span>
              </Link>
            ))}
          </div>

        </div>
      </div>
    </AdminLayout>
  );
}

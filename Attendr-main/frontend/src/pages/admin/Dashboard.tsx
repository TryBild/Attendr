import { useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from "recharts";
import { Users, CheckCircle, Clock, TrendingUp, LogOut, FileText, List, UserPlus } from "lucide-react";
import { getDashboard } from "../../api/admin";
import { useAuth } from "../../hooks/useAuth";
import { getInitials } from "../../lib/utils";

export default function Dashboard() {
  const { admin, logout } = useAuth();
  const navigate = useNavigate();

  const { data, isLoading, refetch } = useQuery({
    queryKey: ["admin-dashboard"],
    queryFn:  getDashboard,
    refetchInterval: 60_000,
  });

  function handleLogout() {
    logout();
    navigate("/", { replace: true });
  }

  const t = data?.today;
  const stats = [
    { label: "Present",     value: t?.present || 0,    icon: CheckCircle, color: "bg-green-50 text-green-600 border-green-100" },
    { label: "Absent",      value: t?.absent || 0,     icon: Users,       color: "bg-red-50 text-red-600 border-red-100" },
    { label: "Late",        value: t?.late || 0,       icon: Clock,       color: "bg-orange-50 text-orange-600 border-orange-100" },
    { label: "Attendance",  value: `${t?.attendancePercent || 0}%`, icon: TrendingUp, color: "bg-blue-50 text-blue-600 border-blue-100" },
  ];

  return (
    <div className="min-h-screen bg-gray-50 max-w-2xl mx-auto">
      {/* Header */}
      <div className="bg-white px-5 pt-12 pb-5 shadow-sm">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-xl font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>
              {admin?.name || "Dashboard"}
            </h1>
            <div className="flex items-center gap-2 mt-1">
              <span className="bg-blue-100 text-blue-700 text-xs font-semibold px-2 py-0.5 rounded-full">Admin</span>
              {admin?.teamId && <span className="text-xs text-gray-400">Team: {admin.teamId}</span>}
            </div>
          </div>
          <button onClick={handleLogout} className="p-2 text-gray-400 hover:text-red-500 transition">
            <LogOut size={20} />
          </button>
        </div>
        <p className="text-sm text-gray-400 mt-2">
          {new Date().toLocaleDateString("en-IN", { weekday: "long", day: "numeric", month: "long", year: "numeric" })}
        </p>
      </div>

      <div className="px-5 py-5 space-y-5">
        {/* Stats Grid */}
        <div className="grid grid-cols-2 gap-3">
          {stats.map(({ label, value, icon: Icon, color }) => (
            <div key={label} className={`bg-white rounded-2xl p-4 shadow-sm border ${color.split(" ").find(c => c.startsWith("border")) || "border-gray-100"}`}>
              <div className={`w-10 h-10 rounded-xl flex items-center justify-center mb-3 ${color.split(" ").filter(c => !c.startsWith("border")).join(" ")}`}>
                <Icon size={20} />
              </div>
              <p className="text-2xl font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>{isLoading ? "—" : value}</p>
              <p className="text-xs text-gray-500 mt-0.5">{label} Today</p>
            </div>
          ))}
        </div>

        {/* This Month */}
        {data?.thisMonth && (
          <div className="bg-white rounded-2xl p-4 shadow-sm flex items-center justify-between">
            <div>
              <p className="text-xs text-gray-400">This Month Average</p>
              <p className="text-2xl font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>
                {data.thisMonth.avgAttendance}%
              </p>
            </div>
            <div className="text-right">
              <p className="text-xs text-gray-400">Working Days</p>
              <p className="text-xl font-bold text-gray-700">{data.thisMonth.totalWorkingDays}</p>
            </div>
          </div>
        )}

        {/* Quick Actions */}
        <div className="grid grid-cols-3 gap-3">
          {[
            { label: "Day Register", to: "/admin/day-register", icon: List },
            { label: "Month Report", to: "/admin/reports",      icon: FileText },
            { label: "Add Employee", to: "/admin/employees",    icon: UserPlus },
          ].map(({ label, to, icon: Icon }) => (
            <Link key={label} to={to} className="bg-white rounded-2xl p-4 shadow-sm flex flex-col items-center gap-2 hover:bg-blue-50 transition text-center">
              <Icon size={22} className="text-blue-700" />
              <span className="text-xs font-semibold text-gray-700">{label}</span>
            </Link>
          ))}
        </div>

        {/* Recent Activity */}
        {data?.recentActivity && data.recentActivity.length > 0 && (
          <div className="bg-white rounded-2xl shadow-sm overflow-hidden">
            <div className="px-4 pt-4 pb-2 flex items-center justify-between">
              <h2 className="font-bold text-gray-800" style={{ fontFamily: "Nunito, sans-serif" }}>Recent Activity</h2>
              <Link to="/admin/day-register" className="text-xs text-blue-700 font-medium">View all →</Link>
            </div>
            {data.recentActivity.map((a: any, i: number) => (
              <div key={i} className="px-4 py-3 flex items-center gap-3 border-t border-gray-50">
                <div className="w-9 h-9 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0">
                  <span className="text-xs font-bold text-blue-700">{getInitials(a.employeeName)}</span>
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-semibold text-gray-800 truncate">{a.employeeName}</p>
                  <p className="text-xs text-gray-400">{a.action === "checkin" ? "Checked in" : "Checked out"} · {a.time}</p>
                </div>
                <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${a.action === "checkin" ? "bg-green-100 text-green-700" : "bg-gray-100 text-gray-600"}`}>
                  {a.action === "checkin" ? "In" : "Out"}
                </span>
              </div>
            ))}
          </div>
        )}

        {/* Navigation */}
        <nav className="bg-white rounded-2xl shadow-sm overflow-hidden">
          {[
            { label: "Employees",   to: "/admin/employees" },
            { label: "Departments", to: "/admin/departments" },
            { label: "Geofences",   to: "/admin/geofences" },
          ].map(({ label, to }) => (
            <Link key={label} to={to} className="flex items-center justify-between px-4 py-4 border-b border-gray-50 hover:bg-gray-50 transition text-sm font-medium text-gray-700 last:border-0">
              {label}
              <span className="text-gray-300">›</span>
            </Link>
          ))}
        </nav>
      </div>
    </div>
  );
}

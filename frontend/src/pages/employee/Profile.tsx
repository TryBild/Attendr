import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import { Home, Calendar, User, LogOut, Camera, Smartphone, Info } from "lucide-react";
import toast from "react-hot-toast";
import { useAuth } from "../../hooks/useAuth";
import { getInitials, formatDateIndia } from "../../lib/utils";

export default function Profile() {
  const navigate = useNavigate();
  const { employee, logout } = useAuth();

  function handleLogout() {
    logout();
    navigate("/", { replace: true });
  }

  function handleAvatarTap() {
    toast("Photo upload isn't available yet", { icon: "📷" });
  }

  if (!employee) return null;

  const fields = [
    { label: "Full Name",     value: employee.fullName },
    { label: "Mobile",        value: `+91 ${employee.mobile}` },
    { label: "Employee Code", value: employee.employeeCode || "Not assigned yet" },
    { label: "Designation",   value: employee.designation || "—" },
    { label: "Department",    value: employee.department || "Not assigned" },
    { label: "Company",       value: employee.company.name },
    { label: "Team ID",       value: employee.company.teamId },
    { label: "Joined On",     value: employee.joinedAt ? formatDateIndia(employee.joinedAt.slice(0, 10)) : "—" },
  ];

  return (
    <div className="min-h-screen bg-gray-50 max-w-md mx-auto pb-24">
      {/* Header */}
      <div className="bg-blue-900 px-5 pt-12 pb-10 text-white">
        <h1 className="text-xl font-bold mb-6" style={{ fontFamily: "Nunito, sans-serif" }}>My Profile</h1>
        <div className="flex items-center gap-4">
          <button onClick={handleAvatarTap} className="relative w-16 h-16 group">
            <div className="w-16 h-16 bg-white rounded-full flex items-center justify-center">
              <span className="text-2xl font-bold text-blue-900" style={{ fontFamily: "Nunito, sans-serif" }}>
                {getInitials(employee.fullName)}
              </span>
            </div>
            <div className="absolute bottom-0 right-0 w-6 h-6 bg-blue-600 rounded-full flex items-center justify-center border-2 border-blue-900">
              <Camera size={12} className="text-white" />
            </div>
          </button>
          <div>
            <p className="text-lg font-bold" style={{ fontFamily: "Nunito, sans-serif" }}>{employee.fullName}</p>
            <p className="text-blue-200 text-sm">{employee.designation || "Employee"}</p>
          </div>
        </div>
      </div>

      <div className="px-5 -mt-4">
        <div className="bg-white rounded-2xl shadow-sm overflow-hidden">
          {fields.map(({ label, value }, i) => (
            <div key={label} className={`px-4 py-4 ${i < fields.length - 1 ? "border-b border-gray-50" : ""}`}>
              <p className="text-xs text-gray-400 mb-0.5">{label}</p>
              <p className={`text-sm font-semibold ${value === "Not assigned yet" || value === "Not assigned" ? "text-gray-400 italic" : "text-gray-800"}`}>{value}</p>
            </div>
          ))}
        </div>

        {/* Device & App Info */}
        <div className="mt-4 bg-white rounded-2xl shadow-sm overflow-hidden">
          <div className="px-4 py-4 border-b border-gray-50 flex items-center gap-3">
            <Smartphone size={16} className="text-gray-400" />
            <div>
              <p className="text-xs text-gray-400 mb-0.5">Device</p>
              <p className="text-sm font-semibold text-gray-400 italic">Not registered</p>
            </div>
          </div>
          <div className="px-4 py-4 flex items-center gap-3">
            <Info size={16} className="text-gray-400" />
            <div>
              <p className="text-xs text-gray-400 mb-0.5">App Version</p>
              <p className="text-sm font-semibold text-gray-800">{__APP_VERSION__}</p>
            </div>
          </div>
        </div>

        <div className="mt-4">
          <Link to="/help" className="block bg-white rounded-2xl px-4 py-4 shadow-sm text-sm font-medium text-gray-700 hover:bg-gray-50 transition">
            Help Center
          </Link>
        </div>

        <button
          onClick={handleLogout}
          className="w-full mt-4 bg-red-50 text-red-600 py-4 rounded-2xl font-semibold text-sm flex items-center justify-center gap-2 hover:bg-red-100 transition"
        >
          <LogOut size={18} />
          Logout
        </button>
      </div>

      {/* Bottom Nav */}
      <nav className="fixed bottom-0 left-1/2 -translate-x-1/2 w-full max-w-md bg-white border-t border-gray-200 flex safe-bottom">
        <Link to="/employee/home" className="flex-1 flex flex-col items-center py-3 text-gray-400 text-xs gap-1">
          <Home size={22} />Home
        </Link>
        <Link to="/employee/attendance" className="flex-1 flex flex-col items-center py-3 text-gray-400 text-xs gap-1">
          <Calendar size={22} />Attendance
        </Link>
        <Link to="/employee/profile" className="flex-1 flex flex-col items-center py-3 text-blue-900 font-semibold text-xs gap-1">
          <User size={22} />Profile
        </Link>
      </nav>
    </div>
  );
}

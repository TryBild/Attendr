import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Briefcase, User } from "lucide-react";
import AttendrLogo from "../assets/attendr-logo.png";

type Role = "admin" | "employee" | null;

export default function RoleSelection() {
  const navigate = useNavigate();
  const [selected, setSelected] = useState<Role>(null);

  function handleSelect(role: Role) {
    setSelected(role);
    if (role === "admin") navigate("/admin/login");
    else navigate("/employee/login");
  }

  return (
    <div className="min-h-screen flex flex-col bg-white max-w-md mx-auto">
      <div className="flex-1 flex flex-col items-center justify-center px-6 pt-16 pb-8">
        <div className="flex items-center gap-3 mb-10">
          <img src={AttendrLogo} alt="Attendr" className="w-10 h-10 object-contain rounded-xl" />
          <span className="text-xl font-bold text-blue-900" style={{ fontFamily: "Nunito, sans-serif" }}>Attendr</span>
        </div>

        <h1 className="text-2xl font-bold text-gray-900 mb-2 text-center" style={{ fontFamily: "Nunito, sans-serif" }}>
          Who are you?
        </h1>
        <p className="text-gray-500 text-sm text-center mb-10">
          Choose your role to get started
        </p>

        <div className="w-full flex flex-col sm:flex-row gap-4">
          <button
            onClick={() => handleSelect("admin")}
            className={`flex-1 flex flex-col items-center gap-4 px-6 py-8 rounded-2xl border-2 shadow-sm transition-all active:scale-95 cursor-pointer
              ${selected === "admin"
                ? "border-blue-900 bg-blue-50"
                : "border-gray-200 bg-white hover:border-blue-300 hover:shadow-md"
              }`}
          >
            <div className="w-14 h-14 rounded-full bg-blue-100 flex items-center justify-center">
              <Briefcase size={28} className="text-blue-900" />
            </div>
            <div className="text-center">
              <p className="text-base font-bold text-gray-900 mb-1">Admin</p>
              <p className="text-sm text-gray-500">I manage a team or organisation</p>
            </div>
          </button>

          <button
            onClick={() => handleSelect("employee")}
            className={`flex-1 flex flex-col items-center gap-4 px-6 py-8 rounded-2xl border-2 shadow-sm transition-all active:scale-95 cursor-pointer
              ${selected === "employee"
                ? "border-blue-900 bg-blue-50"
                : "border-gray-200 bg-white hover:border-blue-300 hover:shadow-md"
              }`}
          >
            <div className="w-14 h-14 rounded-full bg-blue-100 flex items-center justify-center">
              <User size={28} className="text-blue-900" />
            </div>
            <div className="text-center">
              <p className="text-base font-bold text-gray-900 mb-1">Employee</p>
              <p className="text-sm text-gray-500">I mark my daily attendance</p>
            </div>
          </button>
        </div>
      </div>
    </div>
  );
}

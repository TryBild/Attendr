import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { ArrowLeft, Briefcase, User } from "lucide-react";
import { RoleCard } from "../components/RoleCard";

export default function RoleSelector() {
  const navigate = useNavigate();
  const [selectedRole, setSelectedRole] = useState<"admin" | "employee">("admin");

  function handleContinue() {
    if (selectedRole === "admin") {
      navigate("/admin/login");
    } else {
      navigate("/register");
    }
  }

  return (
    <div className="min-h-screen bg-white max-w-md mx-auto flex flex-col">
      <div className="flex-1 flex flex-col px-6 animate-fadeUp">
        {/* Back button */}
        <button
          onClick={() => navigate("/welcome")}
          aria-label="Go back"
          className="flex items-center gap-1 pt-4 text-[#64748B] text-sm self-start min-h-[44px]"
        >
          <ArrowLeft size={16} />
          <span>Back</span>
        </button>

        {/* Logo + Wordmark */}
        <div className="flex items-center gap-2 mt-6">
          <div className="w-8 h-8 bg-[#1E3A8A] rounded-lg flex items-center justify-center flex-shrink-0">
            <span
              className="text-white font-bold text-base leading-none"
              style={{ fontFamily: "Nunito, sans-serif" }}
            >
              A
            </span>
          </div>
          <span
            className="text-[#1E3A8A] text-xl font-semibold"
            style={{ fontFamily: "Nunito, sans-serif" }}
          >
            Attendr
          </span>
        </div>

        {/* Heading */}
        <h1 className="text-2xl font-bold text-[#0F172A] mt-7">Who are you?</h1>
        <p className="text-sm text-[#64748B] mt-1 mb-8">Choose your role to get started.</p>

        {/* Role cards */}
        <div role="radiogroup" aria-label="Select your role" className="flex flex-col gap-3">
          <RoleCard
            icon={<Briefcase size={24} />}
            title="Admin"
            description="I manage a team or organisation"
            selected={selectedRole === "admin"}
            onPress={() => setSelectedRole("admin")}
          />
          <RoleCard
            icon={<User size={24} />}
            title="Employee"
            description="I mark my daily attendance"
            selected={selectedRole === "employee"}
            onPress={() => setSelectedRole("employee")}
          />
        </div>

        {/* Continue button */}
        <button
          onClick={handleContinue}
          aria-label={`Continue as ${selectedRole === "admin" ? "Admin" : "Employee"}`}
          className="w-full bg-[#1E3A8A] text-white py-4 rounded-full font-semibold text-base mt-8 active:scale-[0.97] transition-transform duration-100"
        >
          Continue
        </button>

        {/* Login link */}
        <p className="text-center text-[13px] text-[#64748B] mt-5 mb-8">
          Already have an account?{" "}
          <button
            onClick={() => navigate("/login")}
            className="text-[#1E3A8A] font-semibold"
          >
            Log in
          </button>
        </p>
      </div>
    </div>
  );
}

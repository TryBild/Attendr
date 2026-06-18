import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { Eye, EyeOff, Lock } from "lucide-react";
import toast from "react-hot-toast";
import { employeeSetPassword } from "../../api/auth";
import { useAuth } from "../../hooks/useAuth";

interface SetPasswordState {
  pendingToken: string;
  fullName: string;
  purpose: "register" | "forgot";
}

export default function EmployeeSetPassword() {
  const navigate = useNavigate();
  const location = useLocation();
  const { setEmployee } = useAuth();
  const state = location.state as SetPasswordState | null;

  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  if (!state?.pendingToken) {
    navigate("/employee/login", { replace: true });
    return null;
  }

  const isReset = state.purpose === "forgot";

  function validate() {
    const e: Record<string, string> = {};
    if (password.length < 6) e.password = "Password must be at least 6 characters";
    if (password !== confirmPassword) e.confirmPassword = "Passwords do not match";
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!validate()) return;
    setLoading(true);
    try {
      const res = await employeeSetPassword(state!.pendingToken, password, confirmPassword);
      setEmployee(res.token, res.employee.company.teamId, res.employee);
      toast.success(isReset ? "Password updated!" : `Welcome, ${res.employee.fullName}!`);
      navigate("/employee/home", { replace: true });
    } catch (err: unknown) {
      const msg = (err as Error).message || "Something went wrong";
      if (msg.toLowerCase().includes("expired")) {
        toast.error("Session expired. Please try again.");
        navigate(isReset ? "/employee/forgot-password" : "/register", { replace: true });
      } else {
        toast.error(msg);
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex flex-col bg-white max-w-md mx-auto">
      <div className="px-6 pt-12 pb-4">
        <div className="flex items-center gap-3 mb-8">
          <img src="/favicon.png" alt="Attendr" className="w-10 h-10 object-contain rounded-xl" />
          <span className="text-xl font-bold text-blue-900" style={{ fontFamily: "Nunito, sans-serif" }}>Attendr</span>
        </div>

        <div className="w-14 h-14 bg-blue-100 rounded-full flex items-center justify-center mb-6">
          <Lock size={28} className="text-blue-900" />
        </div>

        <h1 className="text-2xl font-bold text-gray-900 mb-1" style={{ fontFamily: "Nunito, sans-serif" }}>
          {isReset ? "Reset your password" : "Set your password"}
        </h1>
        <p className="text-gray-500 text-sm mb-8">
          {isReset
            ? "Choose a new password for your account"
            : `Hi ${state.fullName}, create a password to secure your account`}
        </p>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
            <div className="relative">
              <input
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="At least 6 characters"
                className={`w-full px-4 py-3 pr-12 rounded-xl border text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600 ${errors.password ? "border-red-400 bg-red-50" : "border-gray-200 bg-gray-50"}`}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
              </button>
            </div>
            {errors.password && <p className="text-red-500 text-xs mt-1">{errors.password}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Confirm Password</label>
            <div className="relative">
              <input
                type={showConfirm ? "text" : "password"}
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="Re-enter your password"
                className={`w-full px-4 py-3 pr-12 rounded-xl border text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600 ${errors.confirmPassword ? "border-red-400 bg-red-50" : "border-gray-200 bg-gray-50"}`}
              />
              <button
                type="button"
                onClick={() => setShowConfirm(!showConfirm)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                {showConfirm ? <EyeOff size={20} /> : <Eye size={20} />}
              </button>
            </div>
            {errors.confirmPassword && <p className="text-red-500 text-xs mt-1">{errors.confirmPassword}</p>}
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-900 text-white py-4 rounded-full font-bold text-base mt-2 hover:bg-blue-800 active:scale-95 transition-all disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {loading ? (
              <svg className="animate-spin w-5 h-5 border-2 border-white border-t-transparent rounded-full" viewBox="0 0 24 24" />
            ) : isReset ? "Update Password" : "Create Account"}
          </button>
        </form>
      </div>
    </div>
  );
}

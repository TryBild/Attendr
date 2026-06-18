import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { ArrowLeft, Check } from "lucide-react";
import toast from "react-hot-toast";
import { requestOtp } from "../../api/auth";

export default function EmployeeForgotPassword() {
  const navigate = useNavigate();
  const [mobile, setMobile] = useState("");
  const [teamId, setTeamId] = useState("");
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const mobileValid = /^[6-9]\d{9}$/.test(mobile);

  function validate() {
    const e: Record<string, string> = {};
    if (!mobileValid) e.mobile = "Enter a valid 10-digit Indian mobile number";
    if (!teamId.trim()) e.teamId = "Team ID is required";
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!validate()) return;
    setLoading(true);
    try {
      const res = await requestOtp(mobile.trim(), teamId.trim().toUpperCase(), "forgot");
      toast.success(res.message);
      navigate("/verify-otp", { state: { mobile, teamId: teamId.toUpperCase(), fullName: "", purpose: "forgot" } });
    } catch (err: unknown) {
      const msg = (err as Error).message || "Something went wrong";
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex flex-col bg-white max-w-md mx-auto">
      <div className="px-6 pt-12 pb-4">
        <Link to="/employee/login" className="inline-flex items-center text-gray-500 hover:text-gray-700 mb-6">
          <ArrowLeft size={20} />
        </Link>

        <div className="flex items-center gap-3 mb-8">
          <img src="/favicon.png" alt="Attendr" className="w-10 h-10 object-contain rounded-xl" />
          <span className="text-xl font-bold text-blue-900" style={{ fontFamily: "Nunito, sans-serif" }}>Attendr</span>
        </div>

        <h1 className="text-2xl font-bold text-gray-900 mb-1" style={{ fontFamily: "Nunito, sans-serif" }}>Forgot Password</h1>
        <p className="text-gray-500 text-sm mb-8">Enter your details and we'll send an OTP to reset your password</p>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Team ID / Organization ID</label>
            <input
              type="text"
              value={teamId}
              onChange={(e) => setTeamId(e.target.value.toUpperCase())}
              placeholder="e.g., ABC001, TRYBILD101"
              className={`w-full px-4 py-3 rounded-xl border text-gray-900 font-mono focus:outline-none focus:ring-2 focus:ring-blue-600 ${errors.teamId ? "border-red-400 bg-red-50" : "border-gray-200 bg-gray-50"}`}
            />
            {errors.teamId && <p className="text-red-500 text-xs mt-1">{errors.teamId}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Mobile Number</label>
            <div className="relative flex items-center">
              <span className="absolute left-3 text-gray-500 text-sm font-medium">+91</span>
              <input
                type="tel"
                inputMode="numeric"
                maxLength={10}
                value={mobile}
                onChange={(e) => setMobile(e.target.value.replace(/\D/g, "").slice(0, 10))}
                placeholder="9876543210"
                className={`w-full pl-14 pr-10 py-3 rounded-xl border text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600 ${errors.mobile ? "border-red-400 bg-red-50" : "border-gray-200 bg-gray-50"}`}
              />
              {mobileValid && (
                <div className="absolute right-3 w-6 h-6 bg-green-500 rounded-full flex items-center justify-center">
                  <Check size={12} className="text-white" strokeWidth={3} />
                </div>
              )}
            </div>
            {errors.mobile && <p className="text-red-500 text-xs mt-1">{errors.mobile}</p>}
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-900 text-white py-4 rounded-full font-bold text-base mt-2 hover:bg-blue-800 active:scale-95 transition-all disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {loading ? (
              <svg className="animate-spin w-5 h-5 border-2 border-white border-t-transparent rounded-full" viewBox="0 0 24 24" />
            ) : "Send OTP"}
          </button>
        </form>

        <p className="text-center text-sm text-gray-400 mt-6">
          Remember your password?{" "}
          <Link to="/employee/login" className="text-blue-700 font-medium hover:underline">Log in</Link>
        </p>
      </div>
    </div>
  );
}

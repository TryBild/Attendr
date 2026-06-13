import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { Check, ArrowLeft } from "lucide-react";
import toast from "react-hot-toast";
import { requestOtp } from "../../api/auth";

export default function EmployeeRegister() {
  const navigate = useNavigate();
  const [fullName, setFullName] = useState("");
  const [mobile, setMobile]     = useState("");
  const [teamId, setTeamId]     = useState("");
  const [loading, setLoading]   = useState(false);
  const [errors, setErrors]     = useState<Record<string, string>>({});

  const mobileValid = /^[6-9]\d{9}$/.test(mobile);

  function validate() {
    const e: Record<string, string> = {};
    if (!fullName.trim() || fullName.trim().length < 2) e.fullName = "Enter your full name (at least 2 characters)";
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
      const res = await requestOtp(fullName.trim(), mobile.trim(), teamId.trim().toUpperCase());
      toast.success(res.message);
      navigate("/verify-otp", { state: { mobile, teamId: teamId.toUpperCase(), fullName } });
    } catch (err: any) {
      const msg = err.message || "Something went wrong";
      if (msg.toLowerCase().includes("organization not found") || msg.toLowerCase().includes("not found")) {
        setErrors((prev) => ({ ...prev, teamId: msg }));
      }
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex flex-col bg-white max-w-md mx-auto">
      <div className="px-6 pt-12 pb-4">
        <Link to="/" className="inline-flex items-center text-gray-500 hover:text-gray-700 mb-6">
          <ArrowLeft size={20} />
        </Link>

        {/* Logo */}
        <div className="flex items-center gap-3 mb-8">
          <img src="/favicon.png" alt="Attendr" className="w-10 h-10 object-contain rounded-xl" />
          <span className="text-xl font-bold text-blue-900" style={{ fontFamily: "Nunito, sans-serif" }}>Attendr</span>
        </div>

        <h1 className="text-2xl font-bold text-gray-900 mb-1" style={{ fontFamily: "Nunito, sans-serif" }}>Register</h1>
        <p className="text-gray-500 text-sm mb-8">Enter your details to get started</p>

        <form onSubmit={handleSubmit} className="space-y-5">
          {/* Full Name */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Full Name</label>
            <input
              type="text"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              placeholder="Rahul Sharma"
              className={`w-full px-4 py-3 rounded-xl border text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600 ${errors.fullName ? "border-red-400 bg-red-50" : "border-gray-200 bg-gray-50"}`}
            />
            {errors.fullName && <p className="text-red-500 text-xs mt-1">{errors.fullName}</p>}
          </div>

          {/* Mobile */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Mobile Number</label>
            <div className="relative flex items-center">
              <span className="absolute left-3 text-gray-500 text-sm font-medium">🇮🇳 +91</span>
              <input
                type="tel"
                inputMode="numeric"
                maxLength={10}
                value={mobile}
                onChange={(e) => setMobile(e.target.value.replace(/\D/g, "").slice(0, 10))}
                placeholder="9876543210"
                className={`w-full pl-20 pr-10 py-3 rounded-xl border text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600 ${errors.mobile ? "border-red-400 bg-red-50" : "border-gray-200 bg-gray-50"}`}
              />
              {mobileValid && (
                <div className="absolute right-3 w-6 h-6 bg-green-500 rounded-full flex items-center justify-center">
                  <Check size={12} className="text-white" strokeWidth={3} />
                </div>
              )}
            </div>
            {errors.mobile && <p className="text-red-500 text-xs mt-1">{errors.mobile}</p>}
          </div>

          {/* Team ID */}
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
            <p className="text-xs text-gray-400 mt-1">Ask your HR manager for your Team ID</p>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-900 text-white py-4 rounded-full font-bold text-base mt-2 hover:bg-blue-800 active:scale-95 transition-all disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {loading ? (
              <svg className="animate-spin w-5 h-5 border-2 border-white border-t-transparent rounded-full" viewBox="0 0 24 24" />
            ) : "Continue"}
          </button>
        </form>

        <p className="text-center text-sm text-gray-400 mt-6">
          Are you an admin?{" "}
          <Link to="/admin/login" className="text-blue-700 font-medium hover:underline">Admin Login</Link>
        </p>
      </div>
    </div>
  );
}

import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { Eye, EyeOff, Copy, Check } from "lucide-react";
import toast from "react-hot-toast";
import { adminRegister } from "../../api/auth";
import { useAuth } from "../../hooks/useAuth";

const INDIAN_STATES = [
  "Andhra Pradesh","Arunachal Pradesh","Assam","Bihar","Chhattisgarh","Goa","Gujarat",
  "Haryana","Himachal Pradesh","Jharkhand","Karnataka","Kerala","Madhya Pradesh",
  "Maharashtra","Manipur","Meghalaya","Mizoram","Nagaland","Odisha","Punjab",
  "Rajasthan","Sikkim","Tamil Nadu","Telangana","Tripura","Uttar Pradesh",
  "Uttarakhand","West Bengal",
  "Andaman and Nicobar Islands","Chandigarh","Dadra and Nagar Haveli and Daman and Diu",
  "Delhi","Jammu and Kashmir","Ladakh","Lakshadweep","Puducherry",
];

export default function AdminRegister() {
  const navigate = useNavigate();
  const { setAdmin } = useAuth();
  const [form, setForm] = useState({
    companyName: "", adminEmail: "", password: "", confirmPassword: "", city: "", state: "",
  });
  const [showPwd, setShowPwd] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [teamId, setTeamId] = useState("");
  const [copied, setCopied] = useState(false);

  function setField(key: string, value: string) {
    setForm((f) => ({ ...f, [key]: value }));
    setErrors((e) => ({ ...e, [key]: "" }));
  }

  function validate() {
    const e: Record<string, string> = {};
    if (!form.companyName.trim()) e.companyName = "Company name is required";
    if (!/\S+@\S+\.\S+/.test(form.adminEmail)) e.adminEmail = "Enter a valid email";
    if (form.password.length < 8) e.password = "Password must be at least 8 characters";
    if (form.password !== form.confirmPassword) e.confirmPassword = "Passwords do not match";
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!validate()) return;
    setLoading(true);
    try {
      const res = await adminRegister({
        companyName: form.companyName.trim(),
        adminEmail:  form.adminEmail.trim(),
        password:    form.password,
        city:        form.city.trim() || undefined,
        state:       form.state || undefined,
      });
      setTeamId(res.company.teamId);
      setAdmin(res.token, res.company.id, {
        id:     res.company.id,
        name:   res.company.name,
        teamId: res.company.teamId,
        plan:   "free",
      });
    } catch (err: any) {
      toast.error(err.message || "Registration failed");
    } finally {
      setLoading(false);
    }
  }

  function copyTeamId() {
    navigator.clipboard.writeText(teamId);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }

  // Success modal
  if (teamId) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 px-6">
        <div className="bg-white rounded-2xl shadow-xl p-8 max-w-sm w-full text-center">
          <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <Check size={32} className="text-green-600" />
          </div>
          <h2 className="text-xl font-bold text-gray-900 mb-2" style={{ fontFamily: "Nunito, sans-serif" }}>
            Company Registered!
          </h2>
          <p className="text-gray-500 text-sm mb-6">Share this Team ID with your employees so they can join.</p>

          <div className="bg-blue-50 border border-blue-200 rounded-xl p-4 mb-6">
            <p className="text-xs text-blue-600 font-medium mb-1">Your Team ID</p>
            <div className="flex items-center justify-center gap-3">
              <span className="text-3xl font-bold text-blue-900 tracking-widest" style={{ fontFamily: "Nunito, sans-serif" }}>
                {teamId}
              </span>
              <button onClick={copyTeamId} className="text-blue-600 hover:text-blue-800">
                {copied ? <Check size={18} /> : <Copy size={18} />}
              </button>
            </div>
          </div>

          <button
            onClick={() => navigate("/admin/dashboard", { replace: true })}
            className="w-full bg-blue-900 text-white py-3 rounded-full font-bold hover:bg-blue-800 transition"
          >
            Go to Dashboard
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex flex-col bg-white max-w-md mx-auto overflow-y-auto">
      <div className="px-6 pt-16 pb-8">
        <div className="flex items-center gap-3 mb-10">
          <div className="w-10 h-10 bg-blue-900 rounded-xl flex items-center justify-center">
            <span className="text-white font-bold text-lg" style={{ fontFamily: "Nunito, sans-serif" }}>A</span>
          </div>
          <span className="text-xl font-bold text-blue-900" style={{ fontFamily: "Nunito, sans-serif" }}>Attendr</span>
        </div>

        <h1 className="text-2xl font-bold text-gray-900 mb-1" style={{ fontFamily: "Nunito, sans-serif" }}>Register Your Company</h1>
        <p className="text-gray-500 text-sm mb-8">Set up your organization's attendance system</p>

        <form onSubmit={handleSubmit} className="space-y-4">
          {[
            { key: "companyName", label: "Company Name", type: "text", placeholder: "Sharma Textiles Pvt Ltd" },
            { key: "adminEmail",  label: "Admin Email",  type: "email", placeholder: "admin@company.com" },
          ].map(({ key, label, type, placeholder }) => (
            <div key={key}>
              <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
              <input
                type={type}
                value={form[key as keyof typeof form]}
                onChange={(e) => setField(key, e.target.value)}
                placeholder={placeholder}
                className={`w-full px-4 py-3 rounded-xl border text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600 ${errors[key] ? "border-red-400 bg-red-50" : "border-gray-200 bg-gray-50"}`}
              />
              {errors[key] && <p className="text-red-500 text-xs mt-1">{errors[key]}</p>}
            </div>
          ))}

          {/* Password */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
            <div className="relative">
              <input
                type={showPwd ? "text" : "password"}
                value={form.password}
                onChange={(e) => setField("password", e.target.value)}
                placeholder="Min 8 characters"
                className={`w-full px-4 py-3 pr-12 rounded-xl border text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600 ${errors.password ? "border-red-400 bg-red-50" : "border-gray-200 bg-gray-50"}`}
              />
              <button type="button" onClick={() => setShowPwd(!showPwd)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">
                {showPwd ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
            {errors.password && <p className="text-red-500 text-xs mt-1">{errors.password}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Confirm Password</label>
            <input
              type="password"
              value={form.confirmPassword}
              onChange={(e) => setField("confirmPassword", e.target.value)}
              placeholder="Re-enter password"
              className={`w-full px-4 py-3 rounded-xl border text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600 ${errors.confirmPassword ? "border-red-400 bg-red-50" : "border-gray-200 bg-gray-50"}`}
            />
            {errors.confirmPassword && <p className="text-red-500 text-xs mt-1">{errors.confirmPassword}</p>}
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">City</label>
              <input
                type="text"
                value={form.city}
                onChange={(e) => setField("city", e.target.value)}
                placeholder="Mumbai"
                className="w-full px-4 py-3 rounded-xl border border-gray-200 bg-gray-50 text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">State</label>
              <select
                value={form.state}
                onChange={(e) => setField("state", e.target.value)}
                className="w-full px-4 py-3 rounded-xl border border-gray-200 bg-gray-50 text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600"
              >
                <option value="">Select</option>
                {INDIAN_STATES.map((s) => <option key={s} value={s}>{s}</option>)}
              </select>
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-900 text-white py-4 rounded-full font-bold text-base mt-2 hover:bg-blue-800 active:scale-95 transition-all disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center"
          >
            {loading ? (
              <svg className="animate-spin w-5 h-5 border-2 border-white border-t-transparent rounded-full" viewBox="0 0 24 24" />
            ) : "Create Account"}
          </button>
        </form>

        <p className="text-center text-sm text-gray-400 mt-6">
          Already have an account?{" "}
          <Link to="/admin/login" className="text-blue-700 font-medium hover:underline">Admin Login</Link>
        </p>
      </div>
    </div>
  );
}

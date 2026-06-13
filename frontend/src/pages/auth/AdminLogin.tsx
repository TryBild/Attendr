import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { Eye, EyeOff } from "lucide-react";
import AttendrLogo from "../../assets/attendr-logo.png";
import toast from "react-hot-toast";
import { adminLogin } from "../../api/auth";
import { useAuth } from "../../hooks/useAuth";

export default function AdminLogin() {
  const navigate = useNavigate();
  const { setAdmin } = useAuth();
  const [email, setEmail]       = useState("");
  const [password, setPassword] = useState("");
  const [showPwd, setShowPwd]   = useState(false);
  const [loading, setLoading]   = useState(false);
  const [errors, setErrors]     = useState<Record<string, string>>({});

  function validate() {
    const e: Record<string, string> = {};
    if (!email.trim() || !/\S+@\S+\.\S+/.test(email)) e.email = "Enter a valid email address";
    if (!password) e.password = "Password is required";
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!validate()) return;
    setLoading(true);
    try {
      const res = await adminLogin(email.trim(), password);
      setAdmin(res.token, res.company.id, {
        id:            res.company.id,
        name:          res.company.name,
        teamId:        res.company.teamId,
        plan:          res.company.plan,
        city:          res.company.city,
        state:         res.company.state,
        setupComplete: res.company.setupComplete,
      });
      toast.success(`Welcome back, ${res.company.name}!`);
      navigate(res.company.setupComplete ? "/admin/dashboard" : "/admin/setup", { replace: true });
    } catch (err: any) {
      toast.error(err.message || "Login failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex flex-col bg-white max-w-md mx-auto">
      <div className="px-6 pt-16 pb-4">
        {/* Logo */}
        <div className="flex items-center gap-3 mb-10">
          <img src="/favicon.png" alt="Attendr" className="w-10 h-10 object-contain rounded-xl" />
          <span className="text-xl font-bold text-blue-900" style={{ fontFamily: "Nunito, sans-serif" }}>Attendr</span>
        </div>

        <h1 className="text-2xl font-bold text-gray-900 mb-1" style={{ fontFamily: "Nunito, sans-serif" }}>Admin Login</h1>
        <p className="text-gray-500 text-sm mb-8">Sign in to your company admin account</p>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email Address</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="admin@company.com"
              className={`w-full px-4 py-3 rounded-xl border text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600 ${errors.email ? "border-red-400 bg-red-50" : "border-gray-200 bg-gray-50"}`}
            />
            {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
            <div className="relative">
              <input
                type={showPwd ? "text" : "password"}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className={`w-full px-4 py-3 pr-12 rounded-xl border text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600 ${errors.password ? "border-red-400 bg-red-50" : "border-gray-200 bg-gray-50"}`}
              />
              <button
                type="button"
                onClick={() => setShowPwd(!showPwd)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                {showPwd ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
            {errors.password && <p className="text-red-500 text-xs mt-1">{errors.password}</p>}
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-900 text-white py-4 rounded-full font-bold text-base mt-2 hover:bg-blue-800 active:scale-95 transition-all disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center"
          >
            {loading ? (
              <svg className="animate-spin w-5 h-5 border-2 border-white border-t-transparent rounded-full" viewBox="0 0 24 24" />
            ) : "Login"}
          </button>
        </form>

        <p className="text-center text-sm text-gray-400 mt-6">
          Don't have an account?{" "}
          <Link to="/admin/register" className="text-blue-700 font-medium hover:underline">Register your company →</Link>
        </p>
        <p className="text-center text-sm text-gray-400 mt-2">
          Employee?{" "}
          <Link to="/register" className="text-blue-700 font-medium hover:underline">Employee Login</Link>
        </p>
      </div>
    </div>
  );
}

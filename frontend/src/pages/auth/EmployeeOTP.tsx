import { useState, useRef, useEffect } from "react";
import { useNavigate, useLocation, Link } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import toast from "react-hot-toast";
import { verifyOtp, requestOtp } from "../../api/auth";
import { useAuth } from "../../hooks/useAuth";
import { maskMobile } from "../../lib/utils";

export default function EmployeeOTP() {
  const navigate = useNavigate();
  const location = useLocation();
  const { setEmployee } = useAuth();
  const state = location.state as { mobile: string; teamId: string; fullName: string } | null;

  const [digits, setDigits] = useState(["", "", "", "", "", ""]);
  const [loading, setLoading] = useState(false);
  const [countdown, setCountdown] = useState(60);
  const [canResend, setCanResend] = useState(false);
  const [error, setError] = useState("");
  const inputs = useRef<(HTMLInputElement | null)[]>([]);

  // Redirect if no state
  useEffect(() => {
    if (!state?.mobile) navigate("/register", { replace: true });
  }, []);

  // Countdown timer
  useEffect(() => {
    if (countdown <= 0) { setCanResend(true); return; }
    const t = setTimeout(() => setCountdown((c) => c - 1), 1000);
    return () => clearTimeout(t);
  }, [countdown]);

  function handleDigit(idx: number, value: string) {
    const v = value.replace(/\D/g, "").slice(-1);
    const next = [...digits];
    next[idx] = v;
    setDigits(next);
    setError("");
    if (v && idx < 5) inputs.current[idx + 1]?.focus();
  }

  function handleKeyDown(idx: number, e: React.KeyboardEvent) {
    if (e.key === "Backspace" && !digits[idx] && idx > 0) {
      inputs.current[idx - 1]?.focus();
    }
  }

  function handlePaste(e: React.ClipboardEvent) {
    const pasted = e.clipboardData.getData("text").replace(/\D/g, "").slice(0, 6);
    if (pasted.length === 6) {
      setDigits(pasted.split(""));
      inputs.current[5]?.focus();
    }
  }

  async function handleVerify() {
    const otp = digits.join("");
    if (otp.length !== 6) { setError("Enter the complete 6-digit OTP"); return; }
    if (!state) return;
    setLoading(true);
    setError("");
    try {
      const res = await verifyOtp(state.mobile, state.teamId, otp);
      setEmployee(res.token, res.employee.company.teamId, res.employee);
      toast.success(`Welcome, ${res.employee.fullName}!`);
      navigate("/employee/home", { replace: true });
    } catch (err: any) {
      setError(err.message || "Invalid OTP");
    } finally {
      setLoading(false);
    }
  }

  async function handleResend() {
    if (!state || !canResend) return;
    try {
      await requestOtp(state.fullName, state.mobile, state.teamId);
      setCountdown(60);
      setCanResend(false);
      setDigits(["", "", "", "", "", ""]);
      toast.success("New OTP sent!");
      inputs.current[0]?.focus();
    } catch (err: any) {
      toast.error(err.message || "Failed to resend OTP");
    }
  }

  if (!state) return null;

  return (
    <div className="min-h-screen flex flex-col bg-white max-w-md mx-auto">
      <div className="px-6 pt-12">
        <Link to="/register" className="inline-flex items-center text-gray-500 hover:text-gray-700 mb-8">
          <ArrowLeft size={20} />
        </Link>

        <h1 className="text-2xl font-bold text-gray-900 mb-2" style={{ fontFamily: "Nunito, sans-serif" }}>
          Verify your number
        </h1>
        <p className="text-gray-500 text-sm mb-8">
          We sent a 6-digit OTP to{" "}
          <span className="font-semibold text-gray-700">{maskMobile(state.mobile)}</span>
        </p>

        {/* OTP inputs */}
        <div className="flex gap-3 justify-center mb-6" onPaste={handlePaste}>
          {digits.map((d, i) => (
            <input
              key={i}
              ref={(el) => { inputs.current[i] = el; }}
              type="tel"
              inputMode="numeric"
              maxLength={1}
              value={d}
              onChange={(e) => handleDigit(i, e.target.value)}
              onKeyDown={(e) => handleKeyDown(i, e)}
              autoFocus={i === 0}
              className={`w-12 h-14 text-center text-xl font-bold border-2 rounded-xl focus:outline-none focus:border-blue-600 transition ${
                d ? "border-blue-600 bg-blue-50" : "border-gray-200 bg-gray-50"
              } ${error ? "border-red-400" : ""}`}
            />
          ))}
        </div>

        {error && <p className="text-red-500 text-sm text-center mb-4">{error}</p>}

        {/* Resend */}
        <div className="text-center mb-8">
          {canResend ? (
            <button onClick={handleResend} className="text-blue-700 font-semibold text-sm hover:underline">
              Resend OTP
            </button>
          ) : (
            <p className="text-gray-400 text-sm">
              Resend OTP in{" "}
              <span className="font-semibold text-gray-600">
                {String(Math.floor(countdown / 60)).padStart(2, "0")}:{String(countdown % 60).padStart(2, "0")}
              </span>
            </p>
          )}
        </div>

        <button
          onClick={handleVerify}
          disabled={loading || digits.join("").length !== 6}
          className="w-full bg-blue-900 text-white py-4 rounded-full font-bold text-base hover:bg-blue-800 active:scale-95 transition-all disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center gap-2"
        >
          {loading ? (
            <svg className="animate-spin w-5 h-5 border-2 border-white border-t-transparent rounded-full" viewBox="0 0 24 24" />
          ) : "Verify & Continue"}
        </button>
      </div>
    </div>
  );
}

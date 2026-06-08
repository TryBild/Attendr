import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { ChevronDown, MoreVertical, Check } from "lucide-react";

const LANGUAGES = [
  { code: "en", label: "English" },
  { code: "hi", label: "हिन्दी" },
  { code: "mr", label: "मराठी" },
  { code: "gu", label: "ગુજરાતી" },
  { code: "bn", label: "বাংলা" },
  { code: "ta", label: "தமிழ்" },
  { code: "te", label: "తెలుగు" },
  { code: "kn", label: "ಕನ್ನಡ" },
  { code: "ml", label: "മലയാളം" },
  { code: "pa", label: "ਪੰਜਾਬੀ" },
  { code: "ur", label: "اردو" },
  { code: "or", label: "Odia" },
  { code: "as", label: "Assamese" },
  { code: "ne", label: "नेपाली" },
];

export default function Welcome() {
  const navigate = useNavigate();
  const [lang, setLang] = useState(localStorage.getItem("attendr_lang") || "en");
  const [showLang, setShowLang] = useState(false);
  const [showMenu, setShowMenu] = useState(false);

  function handleContinue() {
    localStorage.setItem("attendr_lang", lang);
    localStorage.setItem("agreedToTerms", "true");
    navigate("/register");
  }

  return (
    <div className="min-h-screen flex flex-col bg-white max-w-md mx-auto relative">
      {/* Top menu */}
      <div className="absolute top-4 right-4">
        <button
          onClick={() => setShowMenu(!showMenu)}
          className="p-2 rounded-full hover:bg-gray-100 transition"
        >
          <MoreVertical size={20} className="text-gray-500" />
        </button>
        {showMenu && (
          <div className="absolute right-0 top-10 bg-white border border-gray-200 rounded-xl shadow-lg z-50 min-w-44 py-1">
            <Link to="/help" className="block px-4 py-3 text-sm text-gray-700 hover:bg-gray-50">Help Center</Link>
            <a href="#" className="block px-4 py-3 text-sm text-gray-700 hover:bg-gray-50">Terms of Service</a>
            <a href="#" className="block px-4 py-3 text-sm text-gray-700 hover:bg-gray-50">Privacy Policy</a>
          </div>
        )}
      </div>

      <div className="flex-1 flex flex-col items-center justify-center px-6 pt-16 pb-4">
        {/* Logo */}
        <div className="w-20 h-20 bg-blue-900 rounded-2xl flex items-center justify-center mb-6 shadow-lg">
          <svg width="44" height="44" viewBox="0 0 44 44" fill="none">
            <text x="8" y="32" fontSize="28" fontWeight="bold" fill="white" fontFamily="Nunito, sans-serif">A</text>
            <circle cx="34" cy="12" r="8" fill="#22C55E" />
            <path d="M30 12l3 3 5-5" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        </div>

        <h1 className="text-3xl font-bold text-blue-900 mb-2 text-center" style={{ fontFamily: "Nunito, sans-serif" }}>
          Welcome to Attendr
        </h1>
        <p className="text-gray-500 text-center mb-8">Built for Everyday India</p>

        {/* Language Selector */}
        <div className="w-full mb-6 relative">
          <label className="block text-sm font-medium text-gray-600 mb-1">Select Language</label>
          <button
            onClick={() => setShowLang(!showLang)}
            className="w-full flex items-center justify-between px-4 py-3 border border-gray-200 rounded-xl bg-gray-50 text-gray-800 font-medium"
          >
            <span>{LANGUAGES.find((l) => l.code === lang)?.label || "English"}</span>
            <ChevronDown size={16} className={`transition-transform ${showLang ? "rotate-180" : ""}`} />
          </button>
          {showLang && (
            <div className="absolute top-full left-0 right-0 mt-1 bg-white border border-gray-200 rounded-xl shadow-xl z-50 max-h-60 overflow-y-auto">
              {LANGUAGES.map((l) => (
                <button
                  key={l.code}
                  onClick={() => { setLang(l.code); setShowLang(false); }}
                  className="w-full flex items-center justify-between px-4 py-3 hover:bg-blue-50 text-left text-gray-800"
                >
                  <span>{l.label}</span>
                  {lang === l.code && <Check size={16} className="text-blue-700" />}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Policy links */}
        <p className="text-xs text-gray-400 text-center mb-2">
          By continuing, you agree to our{" "}
          <a href="#" className="text-blue-700 underline">Privacy Policy</a>,{" "}
          <a href="#" className="text-blue-700 underline">Terms & Conditions</a>, and{" "}
          <a href="#" className="text-blue-700 underline">Data Usage Policy</a>.
        </p>
      </div>

      {/* CTA */}
      <div className="px-6 pb-8 safe-bottom">
        <button
          onClick={handleContinue}
          className="w-full bg-blue-900 text-white py-4 rounded-full font-bold text-lg shadow-md hover:bg-blue-800 active:scale-95 transition-all"
        >
          Agree &amp; Continue
        </button>
      </div>
    </div>
  );
}

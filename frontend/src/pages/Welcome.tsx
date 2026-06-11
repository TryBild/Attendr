import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { ChevronDown, Globe, MoreVertical, Check } from "lucide-react";
import AttendrLogo from "../assets/attendr-logo.png";

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
  { code: "kok", label: "Konkani" },
  { code: "mni", label: "Manipuri" },
  { code: "ne", label: "नेपाली" },
];

export default function Welcome() {
  const navigate = useNavigate();
  const [lang, setLang] = useState(localStorage.getItem("attendr_lang") || "en");
  const [showLang, setShowLang] = useState(false);
  const [showMenu, setShowMenu] = useState(false);

  function handleContinue() {
    localStorage.setItem("attendr_lang", lang);
    localStorage.setItem("agreedToTerms", JSON.stringify({ agreedToTerms: true }));
    navigate("/register");
  }

  return (
    <div className="min-h-screen flex flex-col bg-white max-w-md mx-auto relative">
      {/* Top-right menu */}
      <div className="absolute top-4 right-4">
        <button
          onClick={() => setShowMenu(!showMenu)}
          className="p-2 rounded-full hover:bg-gray-100 transition"
        >
          <MoreVertical size={20} className="text-gray-500" />
        </button>
        {showMenu && (
          <div className="absolute right-0 top-10 bg-white border border-gray-200 rounded-xl shadow-lg z-50 min-w-44 py-1">
            <a href="#" className="block px-4 py-3 text-sm text-gray-700 hover:bg-gray-50">Terms of Service</a>
            <a href="#" className="block px-4 py-3 text-sm text-gray-700 hover:bg-gray-50">Privacy Policy</a>
            <Link to="/help" className="block px-4 py-3 text-sm text-gray-700 hover:bg-gray-50">Help Center</Link>
          </div>
        )}
      </div>

      <div className="flex-1 flex flex-col items-center justify-center px-6 pt-16 pb-4">
        {/* Logo */}
        <img src={AttendrLogo} alt="Attendr" className="w-24 h-24 mx-auto mb-6 object-contain rounded-2xl" />

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
            <span className="flex items-center gap-2">
              <Globe size={16} className="text-gray-500" />
              {LANGUAGES.find((l) => l.code === lang)?.label || "English"}
            </span>
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
          Read our{" "}
          <a href="#" className="text-blue-700 underline">Privacy Policy</a>,{" "}
          <a href="#" className="text-blue-700 underline">Terms & Conditions</a>, and{" "}
          <a href="#" className="text-blue-700 underline">Data Usage Policy</a>{" "}
          before continuing.
        </p>
      </div>

      {/* CTA pinned to bottom */}
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

import { useState } from "react";
import { Link } from "react-router-dom";
import { Search, ChevronRight, MessageSquare, Building2, Users, Smartphone, MapPin, RefreshCw, ArrowLeft } from "lucide-react";
import { helpArticles } from "../../data/helpArticles";

const ICON_MAP: Record<string, any> = {
  MessageSquare, Building2, Users, Smartphone, MapPin, RefreshCw,
};

export default function HelpCenter() {
  const [search, setSearch] = useState("");

  const filtered = helpArticles.filter(
    (a) => !search || a.title.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="min-h-screen bg-gray-50 max-w-md mx-auto pb-8">
      <div className="bg-white px-5 pt-12 pb-5 shadow-sm">
        <div className="flex items-center gap-3 mb-5">
          <Link to="/" className="text-gray-500 hover:text-gray-700"><ArrowLeft size={20} /></Link>
          <h1 className="text-xl font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>Help Center</h1>
        </div>
        <div className="relative">
          <Search size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="search"
            placeholder="Search for help..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-3 rounded-xl border border-gray-200 bg-gray-50 text-sm text-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-600"
          />
        </div>
      </div>

      <div className="px-5 py-4">
        <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-3">Most Common Issues</p>
        <div className="bg-white rounded-2xl shadow-sm overflow-hidden">
          {filtered.map((article, i) => {
            const Icon = ICON_MAP[article.icon] || MessageSquare;
            return (
              <Link
                key={article.slug}
                to={`/help/${article.slug}`}
                className={`flex items-center gap-3 px-4 py-4 hover:bg-gray-50 transition ${i > 0 ? "border-t border-gray-50" : ""}`}
              >
                <div className="w-10 h-10 bg-blue-50 rounded-xl flex items-center justify-center flex-shrink-0">
                  <Icon size={18} className="text-blue-700" />
                </div>
                <p className="flex-1 text-sm font-medium text-gray-800">{article.title}</p>
                <ChevronRight size={16} className="text-gray-300" />
              </Link>
            );
          })}
          {filtered.length === 0 && (
            <p className="text-center text-gray-400 py-10 text-sm">No articles match your search.</p>
          )}
        </div>

        <div className="mt-6 bg-blue-50 rounded-2xl p-4 text-center">
          <p className="text-sm font-medium text-gray-700 mb-1">Didn't find what you're looking for?</p>
          <Link to="/help/contact" className="text-blue-700 font-semibold text-sm hover:underline">
            Contact Attendr Support →
          </Link>
        </div>
      </div>
    </div>
  );
}

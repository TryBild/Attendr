import { useState } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import { ArrowLeft, ChevronRight, ThumbsUp, ThumbsDown } from "lucide-react";
import { getArticle, getRelatedArticles } from "../../data/helpArticles";

export default function HelpArticle() {
  const { slug } = useParams<{ slug: string }>();
  const navigate = useNavigate();
  const [feedback, setFeedback] = useState<"yes" | "no" | null>(null);

  const article = getArticle(slug || "");
  if (!article) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center px-6">
          <p className="text-gray-500 mb-4">Article not found.</p>
          <Link to="/help" className="text-blue-700 font-semibold">← Back to Help Center</Link>
        </div>
      </div>
    );
  }

  const related = getRelatedArticles(article.related);

  function handleNo() {
    setFeedback("no");
    setTimeout(() => navigate("/help/contact", { state: { articleSlug: slug } }), 800);
  }

  return (
    <div className="min-h-screen bg-gray-50 max-w-md mx-auto pb-8">
      <div className="bg-white px-5 pt-12 pb-5 shadow-sm">
        <div className="flex items-center gap-3">
          <Link to="/help" className="text-gray-500 hover:text-gray-700"><ArrowLeft size={20} /></Link>
          <h1 className="text-lg font-bold text-gray-900 leading-tight" style={{ fontFamily: "Nunito, sans-serif" }}>
            {article.title}
          </h1>
        </div>
      </div>

      <div className="px-5 py-5 space-y-4">
        {/* Steps */}
        <div className="bg-white rounded-2xl shadow-sm p-5">
          <ol className="space-y-4">
            {article.steps.map((step, i) => (
              <li key={i} className="flex gap-3">
                <span className="flex-shrink-0 w-6 h-6 bg-blue-900 text-white rounded-full flex items-center justify-center text-xs font-bold">
                  {i + 1}
                </span>
                <p className="text-sm text-gray-700 leading-relaxed">{step}</p>
              </li>
            ))}
          </ol>
        </div>

        {/* Feedback */}
        <div className="bg-white rounded-2xl shadow-sm p-4 text-center">
          <p className="text-sm font-medium text-gray-700 mb-3">Did this solve your problem?</p>
          {feedback ? (
            <p className="text-sm text-gray-500">
              {feedback === "yes" ? "✅ Great! Glad that helped." : "↗ Redirecting you to support..."}
            </p>
          ) : (
            <div className="flex justify-center gap-4">
              <button
                onClick={() => setFeedback("yes")}
                className="flex items-center gap-2 px-5 py-2.5 bg-green-50 text-green-700 rounded-full text-sm font-semibold hover:bg-green-100 transition"
              >
                <ThumbsUp size={16} /> Yes
              </button>
              <button
                onClick={handleNo}
                className="flex items-center gap-2 px-5 py-2.5 bg-red-50 text-red-600 rounded-full text-sm font-semibold hover:bg-red-100 transition"
              >
                <ThumbsDown size={16} /> No
              </button>
            </div>
          )}
        </div>

        {/* Related Articles */}
        {related.length > 0 && (
          <div className="bg-white rounded-2xl shadow-sm overflow-hidden">
            <p className="px-4 pt-4 pb-2 text-xs font-semibold text-gray-400 uppercase tracking-wide">Related Articles</p>
            {related.map((r, i) => (
              <Link key={r.slug} to={`/help/${r.slug}`}
                className={`flex items-center px-4 py-3 hover:bg-gray-50 transition ${i > 0 ? "border-t border-gray-50" : ""}`}>
                <p className="flex-1 text-sm text-gray-700">{r.title}</p>
                <ChevronRight size={14} className="text-gray-300" />
              </Link>
            ))}
          </div>
        )}

        <Link to="/help/contact" className="block text-center text-sm text-blue-700 font-medium hover:underline py-2">
          Still having issues? Contact Support →
        </Link>
      </div>
    </div>
  );
}

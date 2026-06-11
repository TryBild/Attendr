import { useState, useRef } from "react";
import { Link, useLocation } from "react-router-dom";
import { ArrowLeft, Paperclip, CheckCircle, X } from "lucide-react";
import toast from "react-hot-toast";
import { submitSupportTicket } from "../../api/support";
import { useAuth } from "../../hooks/useAuth";

export default function ContactSupport() {
  const { employee, admin } = useAuth();
  const location = useLocation();
  const articleSlug = (location.state as any)?.articleSlug;

  const [description, setDescription] = useState("");
  const [mobile, setMobile] = useState(employee?.mobile || "");
  const [teamId, setTeamId] = useState(employee?.company.teamId || admin?.teamId || "");
  const [screenshots, setScreenshots] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const [ticketId, setTicketId] = useState("");
  const fileRef = useRef<HTMLInputElement>(null);

  function handleFiles(e: React.ChangeEvent<HTMLInputElement>) {
    const files = Array.from(e.target.files || []).slice(0, 3);
    files.forEach((file) => {
      if (file.size > 5 * 1024 * 1024) { toast.error(`${file.name} exceeds 5MB`); return; }
      const reader = new FileReader();
      reader.onload = (ev) => {
        setScreenshots((prev) => [...prev.slice(0, 2), ev.target?.result as string]);
      };
      reader.readAsDataURL(file);
    });
    e.target.value = "";
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!description.trim()) { toast.error("Please describe your issue"); return; }
    setLoading(true);
    try {
      const res = await submitSupportTicket({
        issueDescription: description.trim(),
        mobile: mobile.trim() || undefined,
        teamId: teamId.trim().toUpperCase() || undefined,
        articleSlug,
        screenshotBase64: screenshots.length > 0 ? screenshots : undefined,
        deviceInfo: {
          platform:   navigator.platform,
          userAgent:  navigator.userAgent,
          appVersion: "1.0.0",
        },
      });
      setTicketId(res.ticketId);
    } catch (err: any) {
      toast.error(err.message || "Failed to send support request");
    } finally {
      setLoading(false);
    }
  }

  if (ticketId) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center px-6">
        <div className="bg-white rounded-2xl shadow-xl p-8 max-w-sm w-full text-center">
          <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <CheckCircle size={32} className="text-green-600" />
          </div>
          <h2 className="text-xl font-bold text-gray-900 mb-2" style={{ fontFamily: "Nunito, sans-serif" }}>Request Received!</h2>
          <p className="text-gray-500 text-sm mb-4">We'll respond within 24 hours.</p>
          <div className="bg-blue-50 rounded-xl p-3 mb-6">
            <p className="text-xs text-blue-600 font-medium">Ticket ID</p>
            <p className="text-lg font-bold text-blue-900">{ticketId}</p>
          </div>
          <Link to="/help" className="block w-full bg-blue-900 text-white py-3 rounded-full font-bold text-sm hover:bg-blue-800 transition">
            Back to Help Center
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 max-w-md mx-auto pb-8">
      <div className="bg-white px-5 pt-12 pb-5 shadow-sm">
        <div className="flex items-center gap-3">
          <Link to="/help" className="text-gray-500 hover:text-gray-700"><ArrowLeft size={20} /></Link>
          <h1 className="text-xl font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>Contact Support</h1>
        </div>
      </div>

      <div className="px-5 py-5">
        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Describe your issue *</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Please describe what you're experiencing in detail..."
              rows={5}
              className="w-full px-4 py-3 rounded-xl border border-gray-200 bg-gray-50 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600 resize-none"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Mobile Number</label>
            <input
              type="tel"
              value={mobile}
              onChange={(e) => setMobile(e.target.value.replace(/\D/g, "").slice(0, 10))}
              placeholder="9876543210"
              className="w-full px-4 py-3 rounded-xl border border-gray-200 bg-gray-50 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Team ID (optional)</label>
            <input
              type="text"
              value={teamId}
              onChange={(e) => setTeamId(e.target.value.toUpperCase())}
              placeholder="ABC001"
              className="w-full px-4 py-3 rounded-xl border border-gray-200 bg-gray-50 text-sm text-gray-900 font-mono focus:outline-none focus:ring-2 focus:ring-blue-600"
            />
          </div>

          {/* Screenshots */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Add Screenshots (optional)</label>
            <button
              type="button"
              onClick={() => fileRef.current?.click()}
              disabled={screenshots.length >= 3}
              className="flex items-center gap-2 px-4 py-2.5 border border-dashed border-gray-300 rounded-xl text-sm text-gray-500 hover:border-blue-400 hover:text-blue-600 transition disabled:opacity-50"
            >
              <Paperclip size={16} />
              Add Screenshot {screenshots.length > 0 ? `(${screenshots.length}/3)` : ""}
            </button>
            <input ref={fileRef} type="file" accept="image/*" multiple onChange={handleFiles} className="hidden" />
            {screenshots.length > 0 && (
              <div className="flex gap-2 mt-3">
                {screenshots.map((src, i) => (
                  <div key={i} className="relative w-16 h-16">
                    <img src={src} alt={`Screenshot ${i+1}`} className="w-16 h-16 object-cover rounded-lg border border-gray-200" />
                    <button onClick={() => setScreenshots((p) => p.filter((_, j) => j !== i))}
                      className="absolute -top-1 -right-1 w-5 h-5 bg-red-500 text-white rounded-full flex items-center justify-center">
                      <X size={10} />
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          <p className="text-xs text-gray-400 bg-gray-50 rounded-xl p-3">
            ℹ️ Your mobile number, Team ID, device info, and app version will be included automatically with your request.
          </p>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-900 text-white py-4 rounded-full font-bold text-sm hover:bg-blue-800 active:scale-95 transition-all disabled:opacity-60 flex items-center justify-center gap-2"
          >
            {loading ? <svg className="animate-spin w-5 h-5 border-2 border-white border-t-transparent rounded-full" viewBox="0 0 24 24" /> : "Send to Attendr Support"}
          </button>
        </form>
      </div>
    </div>
  );
}

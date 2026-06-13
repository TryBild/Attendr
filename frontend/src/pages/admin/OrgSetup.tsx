import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { MapPin, Navigation, Calendar } from "lucide-react";
import toast from "react-hot-toast";
import { adminSetup } from "../../api/auth";
import { useAuth } from "../../hooks/useAuth";

const DAYS = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];

export default function OrgSetup() {
  const navigate = useNavigate();
  const { markSetupComplete } = useAuth();

  const [latitude, setLatitude]       = useState("");
  const [longitude, setLongitude]     = useState("");
  const [radius, setRadius]           = useState(100);
  const [officeName, setOfficeName]   = useState("");
  const [officeAddress, setOfficeAddress] = useState("");
  const [geoLoading, setGeoLoading]   = useState(false);

  const [workDays, setWorkDays]   = useState<string[]>(["Mon", "Tue", "Wed", "Thu", "Fri"]);
  const [startTime, setStartTime] = useState("09:00");
  const [endTime, setEndTime]     = useState("18:00");

  const [submitting, setSubmitting] = useState(false);

  function useMyLocation() {
    if (!navigator.geolocation) { toast.error("Geolocation not supported"); return; }
    setGeoLoading(true);
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setLatitude(String(pos.coords.latitude));
        setLongitude(String(pos.coords.longitude));
        setGeoLoading(false);
        toast.success("Location captured!");
      },
      () => { toast.error("Location access denied"); setGeoLoading(false); },
      { enableHighAccuracy: true, timeout: 10000 }
    );
  }

  function toggleDay(day: string) {
    setWorkDays((prev) =>
      prev.includes(day) ? prev.filter((d) => d !== day) : [...prev, day]
    );
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (workDays.length === 0) { toast.error("Select at least one work day"); return; }
    if (!startTime || !endTime)  { toast.error("Please set work hours"); return; }
    if (startTime >= endTime)    { toast.error("End time must be after start time"); return; }

    const hasLocation = latitude !== "" && longitude !== "";

    setSubmitting(true);
    try {
      await adminSetup({
        workDays,
        workStartTime: startTime,
        workEndTime:   endTime,
        ...(hasLocation && {
          geofence: {
            name:         officeName.trim() || "Main Office",
            latitude:     Number(latitude),
            longitude:    Number(longitude),
            radiusMeters: radius,
            address:      officeAddress.trim() || undefined,
          },
        }),
      });
      markSetupComplete();
      toast.success("Setup complete! Welcome to Attendr.");
      // TODO: Navigate to Step 2 (employee onboarding checklist) once that screen is built
      navigate("/admin/dashboard", { replace: true });
    } catch (err: any) {
      toast.error(err.message || "Setup failed");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-lg mx-auto px-5 pt-14 pb-12">

        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center gap-3 mb-6">
            <img src="/favicon.png" alt="Attendr" className="w-9 h-9 object-contain rounded-xl" />
            <span className="text-lg font-bold text-blue-900" style={{ fontFamily: "Nunito, sans-serif" }}>
              Attendr
            </span>
          </div>
          <h1 className="text-2xl font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>
            Let's set up your office
          </h1>
          <p className="text-gray-500 text-sm mt-1">
            Configure your location and schedule so employees can start clocking in.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5">

          {/* Location Card */}
          <div className="bg-white rounded-2xl shadow-sm p-5">
            <div className="flex items-center gap-2 mb-4">
              <div className="w-8 h-8 bg-blue-50 rounded-xl flex items-center justify-center flex-shrink-0">
                <MapPin size={16} className="text-blue-700" />
              </div>
              <h2 className="font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>
                Office Location
              </h2>
              <span className="ml-auto text-xs text-gray-400 bg-gray-100 px-2 py-0.5 rounded-full">
                Optional
              </span>
            </div>

            <button
              type="button"
              onClick={useMyLocation}
              disabled={geoLoading}
              className="w-full flex items-center justify-center gap-2 border border-blue-200 text-blue-700 py-3 rounded-xl text-sm font-semibold hover:bg-blue-50 transition disabled:opacity-50 mb-4"
            >
              <Navigation size={15} />
              {geoLoading ? "Detecting location…" : "Use My Location"}
            </button>

            <div className="grid grid-cols-2 gap-3 mb-3">
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Latitude</label>
                <input
                  type="number"
                  step="any"
                  value={latitude}
                  onChange={(e) => setLatitude(e.target.value)}
                  placeholder="19.0760"
                  className="w-full px-3 py-2.5 rounded-xl border border-gray-200 bg-gray-50 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600"
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Longitude</label>
                <input
                  type="number"
                  step="any"
                  value={longitude}
                  onChange={(e) => setLongitude(e.target.value)}
                  placeholder="72.8777"
                  className="w-full px-3 py-2.5 rounded-xl border border-gray-200 bg-gray-50 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600"
                />
              </div>
            </div>

            <div className="mb-4">
              <label className="block text-xs font-medium text-gray-600 mb-1">
                Geofence Radius: <span className="text-blue-700 font-semibold">{radius}m</span>
              </label>
              <input
                type="range"
                min="50"
                max="1000"
                step="10"
                value={radius}
                onChange={(e) => setRadius(Number(e.target.value))}
                className="w-full accent-blue-900"
              />
              <div className="flex justify-between text-xs text-gray-400 mt-0.5">
                <span>50m</span>
                <span>1000m</span>
              </div>
            </div>

            <div className="space-y-3">
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Office Name</label>
                <input
                  type="text"
                  value={officeName}
                  onChange={(e) => setOfficeName(e.target.value)}
                  placeholder="Main Office"
                  className="w-full px-3 py-2.5 rounded-xl border border-gray-200 bg-gray-50 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600"
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Address (optional)</label>
                <input
                  type="text"
                  value={officeAddress}
                  onChange={(e) => setOfficeAddress(e.target.value)}
                  placeholder="BKC, Mumbai"
                  className="w-full px-3 py-2.5 rounded-xl border border-gray-200 bg-gray-50 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600"
                />
              </div>
            </div>

            {latitude && longitude && (
              <p className="mt-3 text-xs text-green-600 bg-green-50 rounded-xl px-3 py-2">
                Location set: {Number(latitude).toFixed(4)}, {Number(longitude).toFixed(4)} · {radius}m radius
              </p>
            )}

            {!latitude && !longitude && (
              <p className="mt-3 text-xs text-blue-600 bg-blue-50 rounded-xl px-3 py-2">
                You can find your office coordinates on Google Maps by right-clicking and selecting "What's here?"
              </p>
            )}
          </div>

          {/* Schedule Card */}
          <div className="bg-white rounded-2xl shadow-sm p-5">
            <div className="flex items-center gap-2 mb-4">
              <div className="w-8 h-8 bg-blue-50 rounded-xl flex items-center justify-center flex-shrink-0">
                <Calendar size={16} className="text-blue-700" />
              </div>
              <h2 className="font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>
                Work Schedule
              </h2>
            </div>

            <div className="mb-4">
              <label className="block text-xs font-medium text-gray-600 mb-2">Work Days</label>
              <div className="flex flex-wrap gap-2">
                {DAYS.map((day) => (
                  <button
                    key={day}
                    type="button"
                    onClick={() => toggleDay(day)}
                    className={`px-3.5 py-1.5 rounded-full text-sm font-semibold transition ${
                      workDays.includes(day)
                        ? "bg-blue-900 text-white"
                        : "bg-gray-100 text-gray-600 hover:bg-gray-200"
                    }`}
                  >
                    {day}
                  </button>
                ))}
              </div>
              {workDays.length === 0 && (
                <p className="text-xs text-red-500 mt-1">Select at least one day</p>
              )}
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Start Time</label>
                <input
                  type="time"
                  value={startTime}
                  onChange={(e) => setStartTime(e.target.value)}
                  className="w-full px-3 py-2.5 rounded-xl border border-gray-200 bg-gray-50 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600"
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">End Time</label>
                <input
                  type="time"
                  value={endTime}
                  onChange={(e) => setEndTime(e.target.value)}
                  className="w-full px-3 py-2.5 rounded-xl border border-gray-200 bg-gray-50 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600"
                />
              </div>
            </div>
          </div>

          <button
            type="submit"
            disabled={submitting}
            className="w-full bg-blue-900 text-white py-4 rounded-full font-bold text-base hover:bg-blue-800 active:scale-95 transition-all disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center"
          >
            {submitting ? (
              <svg className="animate-spin w-5 h-5 border-2 border-white border-t-transparent rounded-full" viewBox="0 0 24 24" />
            ) : "Continue"}
          </button>

          <p className="text-center text-xs text-gray-400">
            You can update these settings anytime from the dashboard.
          </p>
        </form>
      </div>
    </div>
  );
}

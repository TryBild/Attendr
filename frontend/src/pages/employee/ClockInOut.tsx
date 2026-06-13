import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { MapPin, Clock, CheckCircle, Calendar, User, Home } from "lucide-react";
import toast from "react-hot-toast";
import { useAuth } from "../../hooks/useAuth";
import { useGeolocation } from "../../hooks/useGeolocation";
import { markAttendance, getTodayAttendance } from "../../api/attendance";
import { formatDateTime } from "../../lib/utils";

function greeting(name: string) {
  const h = new Date().getHours();
  const g = h < 12 ? "Good Morning" : h < 17 ? "Good Afternoon" : "Good Evening";
  return `${g}, ${name.split(" ")[0]} 👋`;
}

function formatDate() {
  return new Date().toLocaleDateString("en-IN", {
    weekday: "long", day: "numeric", month: "long", year: "numeric", timeZone: "Asia/Kolkata",
  });
}

export default function ClockInOut() {
  const { employee } = useAuth();
  const { geo, request: requestGeo } = useGeolocation();
  const [today, setToday] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(true);

  useEffect(() => {
    fetchToday();
    requestGeo();
  }, []);

  async function fetchToday() {
    setFetching(true);
    try {
      const data = await getTodayAttendance();
      setToday(data);
    } catch {
      setToday({ status: "not_marked" });
    } finally {
      setFetching(false);
    }
  }

  async function handleAction(action: "checkin" | "checkout") {
    if (geo.status !== "ready") {
      requestGeo();
      toast.error("Waiting for your location. Please enable GPS.");
      return;
    }
    setLoading(true);
    try {
      const res = await markAttendance(geo.lat, geo.lng, action);
      toast.success(action === "checkin" ? `Checked in at ${res.time}` : `Checked out. ${res.workingHours?.toFixed(1)}h worked today`);
      await fetchToday();
    } catch (err: any) {
      toast.error(err.message || "Failed to mark attendance");
    } finally {
      setLoading(false);
    }
  }

  const isCheckedIn  = today?.checkInTime && !today?.checkOutTime;
  const isCheckedOut = today?.checkInTime && today?.checkOutTime;

  const geoColor = geo.status === "ready" ? "text-green-500" : geo.status === "loading" ? "text-yellow-500" : "text-red-500";
  const geoText  = geo.status === "ready" ? "Location detected" : geo.status === "loading" ? "Detecting location..." : geo.status === "denied" ? "Location access denied" : "Unable to get location";

  return (
    <div className="min-h-screen flex flex-col bg-gray-50 max-w-md mx-auto">
      {/* Header */}
      <div className="bg-white px-5 pt-12 pb-5 shadow-sm">
        <p className="text-lg font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>
          {employee ? greeting(employee.fullName) : "Good Morning 👋"}
        </p>
        <p className="text-sm text-gray-500 mt-0.5">{formatDate()}</p>
        {employee?.company && (
          <span className="inline-block mt-2 bg-blue-50 text-blue-700 text-xs font-semibold px-3 py-1 rounded-full">
            {employee.company.name}
          </span>
        )}
      </div>

      <div className="flex-1 px-5 py-5 space-y-4">
        {/* Location Card */}
        <div className={`bg-white rounded-2xl p-4 shadow-sm border ${geo.status === "ready" ? "border-green-100" : "border-orange-100"}`}>
          <div className="flex items-center gap-3">
            <MapPin size={20} className={geoColor} />
            <div>
              <p className={`text-sm font-semibold ${geoColor}`}>{geoText}</p>
              {geo.status === "denied" && (
                <p className="text-xs text-gray-400 mt-0.5">
                  Enable location in browser settings to mark attendance
                </p>
              )}
            </div>
            {geo.status !== "ready" && geo.status !== "loading" && (
              <button
                onClick={requestGeo}
                className="ml-auto text-xs text-blue-700 font-semibold border border-blue-200 rounded-lg px-3 py-1"
              >
                Retry
              </button>
            )}
          </div>
        </div>

        {/* Attendance Status Card */}
        <div className="bg-white rounded-2xl p-5 shadow-sm">
          {fetching ? (
            <div className="h-16 flex items-center justify-center">
              <svg className="animate-spin w-6 h-6 border-2 border-blue-600 border-t-transparent rounded-full" viewBox="0 0 24 24" />
            </div>
          ) : isCheckedOut ? (
            <div className="text-center">
              <div className="w-14 h-14 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-3">
                <CheckCircle size={28} className="text-green-600" />
              </div>
              <p className="font-bold text-gray-900 text-lg" style={{ fontFamily: "Nunito, sans-serif" }}>Attendance Complete</p>
              <p className="text-sm text-gray-500 mt-1">
                {today.workingHours?.toFixed(1)}h worked today
              </p>
              <div className="flex justify-center gap-6 mt-3 text-sm text-gray-600">
                <div>
                  <p className="text-xs text-gray-400">Check In</p>
                  <p className="font-semibold">{formatDateTime(today.checkInTime)}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-400">Check Out</p>
                  <p className="font-semibold">{formatDateTime(today.checkOutTime)}</p>
                </div>
              </div>
            </div>
          ) : isCheckedIn ? (
            <div>
              <div className="flex items-center gap-3 mb-2">
                <div className="w-3 h-3 bg-green-500 rounded-full animate-pulse" />
                <p className="font-semibold text-gray-900">Checked in</p>
              </div>
              <p className="text-sm text-gray-500">
                Since <span className="font-semibold text-gray-700">{formatDateTime(today.checkInTime)}</span>
                {today.geofence && ` · ${today.geofence}`}
              </p>
            </div>
          ) : (
            <div className="flex items-center gap-3">
              <Clock size={24} className="text-gray-400" />
              <div>
                <p className="font-semibold text-gray-700">Not yet checked in</p>
                <p className="text-xs text-gray-400 mt-0.5">Tap Check In when you arrive at the office</p>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Bottom CTA */}
      <div className="px-5 pb-24 safe-bottom">
        {!fetching && (
          isCheckedOut ? (
            <button disabled className="w-full bg-gray-200 text-gray-500 py-4 rounded-full font-bold text-base cursor-not-allowed flex items-center justify-center gap-2">
              <CheckCircle size={20} />
              Attendance Marked ✅
            </button>
          ) : isCheckedIn ? (
            <button
              onClick={() => handleAction("checkout")}
              disabled={loading}
              className="w-full bg-slate-700 text-white py-4 rounded-full font-bold text-base hover:bg-slate-800 active:scale-95 transition-all disabled:opacity-60 flex items-center justify-center gap-2"
            >
              {loading ? <svg className="animate-spin w-5 h-5 border-2 border-white border-t-transparent rounded-full" viewBox="0 0 24 24" /> : "Check Out"}
            </button>
          ) : (
            <button
              onClick={() => handleAction("checkin")}
              disabled={loading || geo.status === "loading"}
              className="w-full bg-blue-900 text-white py-4 rounded-full font-bold text-xl h-14 hover:bg-blue-800 active:scale-95 transition-all disabled:opacity-60 flex items-center justify-center gap-2 shadow-lg"
            >
              {loading ? <svg className="animate-spin w-5 h-5 border-2 border-white border-t-transparent rounded-full" viewBox="0 0 24 24" /> : geo.status === "loading" ? "Detecting location..." : "Check In"}
            </button>
          )
        )}
      </div>

      {/* Bottom Nav */}
      <nav className="fixed bottom-0 left-1/2 -translate-x-1/2 w-full max-w-md bg-white border-t border-gray-200 flex safe-bottom">
        <Link to="/employee/home" className="flex-1 flex flex-col items-center py-3 text-blue-900 font-semibold text-xs gap-1">
          <Home size={22} />Home
        </Link>
        <Link to="/employee/attendance" className="flex-1 flex flex-col items-center py-3 text-gray-400 text-xs gap-1">
          <Calendar size={22} />Attendance
        </Link>
        <Link to="/employee/profile" className="flex-1 flex flex-col items-center py-3 text-gray-400 text-xs gap-1">
          <User size={22} />Profile
        </Link>
      </nav>
    </div>
  );
}

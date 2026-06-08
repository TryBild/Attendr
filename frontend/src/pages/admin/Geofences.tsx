import { useState } from "react";
import { Link } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { ArrowLeft, Plus, Pencil, Trash2, X, MapPin, Navigation } from "lucide-react";
import { getGeofences, addGeofence, updateGeofence, deleteGeofence } from "../../api/admin";
import toast from "react-hot-toast";

interface GfForm { name: string; latitude: string; longitude: string; radiusMeters: string; address: string; }
const EMPTY: GfForm = { name: "", latitude: "", longitude: "", radiusMeters: "100", address: "" };

export default function Geofences() {
  const qc = useQueryClient();
  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState<any>(null);
  const [form, setForm] = useState<GfForm>(EMPTY);
  const [geoLoading, setGeoLoading] = useState(false);

  const { data, isLoading } = useQuery({ queryKey: ["geofences"], queryFn: getGeofences });

  const addMut = useMutation({
    mutationFn: () => addGeofence({ name: form.name.trim(), latitude: Number(form.latitude), longitude: Number(form.longitude), radiusMeters: Number(form.radiusMeters), address: form.address.trim() || undefined }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["geofences"] }); closeModal(); toast.success("Geofence added!"); },
    onError: (e: any) => toast.error(e.message),
  });
  const updateMut = useMutation({
    mutationFn: () => updateGeofence(editing._id, { name: form.name.trim(), latitude: Number(form.latitude), longitude: Number(form.longitude), radiusMeters: Number(form.radiusMeters), address: form.address.trim() }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["geofences"] }); closeModal(); toast.success("Updated!"); },
    onError: (e: any) => toast.error(e.message),
  });
  const deleteMut = useMutation({
    mutationFn: (id: string) => deleteGeofence(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["geofences"] }); toast.success("Geofence deleted"); },
    onError: (e: any) => toast.error(e.message),
  });

  function openAdd() { setEditing(null); setForm(EMPTY); setShowModal(true); }
  function openEdit(gf: any) {
    setEditing(gf);
    setForm({ name: gf.name, latitude: String(gf.latitude), longitude: String(gf.longitude), radiusMeters: String(gf.radiusMeters), address: gf.address || "" });
    setShowModal(true);
  }
  function closeModal() { setShowModal(false); setEditing(null); setForm(EMPTY); }
  function setF(k: string, v: string) { setForm((f) => ({ ...f, [k]: v })); }

  function useMyLocation() {
    if (!navigator.geolocation) { toast.error("Geolocation not supported"); return; }
    setGeoLoading(true);
    navigator.geolocation.getCurrentPosition(
      (pos) => { setF("latitude", String(pos.coords.latitude)); setF("longitude", String(pos.coords.longitude)); setGeoLoading(false); toast.success("Location captured!"); },
      () => { toast.error("Location access denied"); setGeoLoading(false); },
      { enableHighAccuracy: true, timeout: 10000 }
    );
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!form.name.trim() || !form.latitude || !form.longitude) { toast.error("Name, latitude, and longitude are required"); return; }
    if (editing) updateMut.mutate();
    else addMut.mutate();
  }

  return (
    <div className="min-h-screen bg-gray-50 max-w-2xl mx-auto pb-8">
      <div className="bg-white px-5 pt-12 pb-4 shadow-sm">
        <div className="flex items-center gap-3">
          <Link to="/admin/dashboard" className="text-gray-500"><ArrowLeft size={20} /></Link>
          <h1 className="text-xl font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>Geofences</h1>
          <button onClick={openAdd} className="ml-auto flex items-center gap-1 bg-blue-900 text-white px-4 py-2 rounded-full text-sm font-semibold">
            <Plus size={15} />Add
          </button>
        </div>
      </div>

      <div className="px-5 py-3">
        <p className="text-xs text-gray-400 bg-blue-50 text-blue-600 rounded-xl p-3">
          💡 You can find your office coordinates on Google Maps by right-clicking the location and selecting "What's here?"
        </p>
      </div>

      <div className="mx-5 space-y-3">
        {isLoading ? (
          <div className="py-12 flex justify-center"><svg className="animate-spin w-6 h-6 border-2 border-blue-600 border-t-transparent rounded-full" viewBox="0 0 24 24" /></div>
        ) : (data?.geofences || []).length === 0 ? (
          <div className="bg-white rounded-2xl p-8 text-center shadow-sm">
            <MapPin size={32} className="text-gray-300 mx-auto mb-3" />
            <p className="text-gray-400 text-sm">No geofences yet. Add your office location.</p>
          </div>
        ) : (data?.geofences || []).map((gf: any) => (
          <div key={gf._id} className="bg-white rounded-2xl p-4 shadow-sm">
            <div className="flex items-start justify-between">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-blue-50 rounded-xl flex items-center justify-center flex-shrink-0">
                  <MapPin size={18} className="text-blue-700" />
                </div>
                <div>
                  <p className="font-semibold text-gray-800">{gf.name}</p>
                  {gf.address && <p className="text-xs text-gray-400 mt-0.5">{gf.address}</p>}
                  <p className="text-xs text-gray-400 mt-0.5">{gf.latitude.toFixed(4)}, {gf.longitude.toFixed(4)} · {gf.radiusMeters}m radius</p>
                </div>
              </div>
              <div className="flex gap-1">
                <button onClick={() => openEdit(gf)} className="p-2 text-gray-400 hover:text-blue-600"><Pencil size={15} /></button>
                <button onClick={() => deleteMut.mutate(gf._id)} className="p-2 text-gray-400 hover:text-red-600"><Trash2 size={15} /></button>
              </div>
            </div>
            <div className="mt-2">
              <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${gf.isActive ? "bg-green-100 text-green-700" : "bg-gray-100 text-gray-500"}`}>
                {gf.isActive ? "Active" : "Inactive"}
              </span>
            </div>
          </div>
        ))}
      </div>

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black/40 flex items-end justify-center z-50">
          <div className="bg-white rounded-t-3xl w-full max-w-md p-6 pb-8 overflow-y-auto max-h-[90vh]">
            <div className="flex items-center justify-between mb-5">
              <h2 className="text-lg font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>{editing ? "Edit Geofence" : "Add Geofence"}</h2>
              <button onClick={closeModal}><X size={20} className="text-gray-400" /></button>
            </div>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Name *</label>
                <input value={form.name} onChange={(e) => setF("name", e.target.value)} placeholder="Main Office"
                  className="w-full px-4 py-2.5 rounded-xl border border-gray-200 bg-gray-50 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600" />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Address (optional)</label>
                <input value={form.address} onChange={(e) => setF("address", e.target.value)} placeholder="BKC, Mumbai"
                  className="w-full px-4 py-2.5 rounded-xl border border-gray-200 bg-gray-50 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600" />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Latitude *</label>
                  <input type="number" step="any" value={form.latitude} onChange={(e) => setF("latitude", e.target.value)} placeholder="19.0760"
                    className="w-full px-4 py-2.5 rounded-xl border border-gray-200 bg-gray-50 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600" />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Longitude *</label>
                  <input type="number" step="any" value={form.longitude} onChange={(e) => setF("longitude", e.target.value)} placeholder="72.8777"
                    className="w-full px-4 py-2.5 rounded-xl border border-gray-200 bg-gray-50 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600" />
                </div>
              </div>
              <button type="button" onClick={useMyLocation} disabled={geoLoading}
                className="w-full flex items-center justify-center gap-2 border border-blue-200 text-blue-700 py-2.5 rounded-xl text-sm font-medium hover:bg-blue-50 transition disabled:opacity-50">
                <Navigation size={15} />{geoLoading ? "Detecting..." : "Use My Current Location"}
              </button>
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Radius: {form.radiusMeters}m</label>
                <input type="range" min="50" max="1000" step="10" value={form.radiusMeters} onChange={(e) => setF("radiusMeters", e.target.value)}
                  className="w-full accent-blue-900" />
                <div className="flex justify-between text-xs text-gray-400"><span>50m</span><span>1000m</span></div>
              </div>
              <button type="submit" disabled={addMut.isPending || updateMut.isPending}
                className="w-full bg-blue-900 text-white py-3.5 rounded-full font-bold text-sm hover:bg-blue-800 transition disabled:opacity-60">
                {addMut.isPending || updateMut.isPending ? "Saving..." : editing ? "Save Changes" : "Add Geofence"}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

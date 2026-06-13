import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { Copy, Check, LogOut, Building2, Hash, User } from "lucide-react";
import toast from "react-hot-toast";
import { adminProfile } from "../../api/auth";
import { useAuth } from "../../hooks/useAuth";
import { getInitials } from "../../lib/utils";
import { AdminLayout } from "../../components/AdminLayout";

export default function AdminProfile() {
  const navigate = useNavigate();
  const { admin, logout } = useAuth();
  const [copied, setCopied] = useState(false);

  const { data } = useQuery({
    queryKey: ["admin-profile"],
    queryFn:  adminProfile,
  });

  const orgId   = admin?.teamId || data?.orgId  || "";
  const orgName = admin?.name   || data?.orgName || "";
  const adminName = data?.adminName || "";

  function copyOrgId() {
    navigator.clipboard.writeText(orgId);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
    toast.success("Org ID copied!");
  }

  function handleLogout() {
    logout();
    navigate("/", { replace: true });
  }

  return (
    <AdminLayout>
      <div className="min-h-screen max-w-2xl mx-auto pb-24" style={{ backgroundColor: "#F5F7FB" }}>

        {/* Top bar */}
        <div className="bg-white px-5 pt-12 pb-4 shadow-sm">
          <h1 className="text-xl font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>
            Profile
          </h1>
        </div>

        <div className="px-5 py-5 space-y-4">

          {/* Avatar card */}
          <div className="bg-white rounded-2xl shadow-sm p-6 flex flex-col items-center text-center">
            <div className="w-20 h-20 rounded-full bg-[#1B3A7B] flex items-center justify-center mb-3">
              <span className="text-2xl font-bold text-white">
                {getInitials(adminName || orgName || "A")}
              </span>
            </div>
            {adminName ? (
              <>
                <h2 className="text-xl font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>
                  {adminName}
                </h2>
                <p className="text-sm text-gray-500 mt-0.5">{orgName}</p>
              </>
            ) : (
              <h2 className="text-xl font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>
                {orgName}
              </h2>
            )}
            <span className="mt-3 bg-blue-100 text-blue-700 text-xs font-bold px-3 py-1 rounded-full">
              Admin
            </span>
          </div>

          {/* Org details */}
          <div className="bg-white rounded-2xl shadow-sm divide-y divide-gray-50">
            <div className="flex items-center gap-3 px-5 py-4">
              <div className="w-9 h-9 bg-gray-50 rounded-xl flex items-center justify-center flex-shrink-0">
                <Building2 size={17} className="text-gray-400" />
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-xs text-gray-400">Company Name</p>
                <p className="font-semibold text-gray-800 truncate">{orgName || "—"}</p>
              </div>
            </div>

            <div className="flex items-center gap-3 px-5 py-4">
              <div className="w-9 h-9 bg-gray-50 rounded-xl flex items-center justify-center flex-shrink-0">
                <User size={17} className="text-gray-400" />
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-xs text-gray-400">Admin Name</p>
                <p className="font-semibold text-gray-800">{adminName || "—"}</p>
              </div>
            </div>

            <div className="flex items-center gap-3 px-5 py-4">
              <div className="w-9 h-9 bg-blue-50 rounded-xl flex items-center justify-center flex-shrink-0">
                <Hash size={17} className="text-blue-600" />
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-xs text-gray-400">Org ID (Team Code)</p>
                <p className="text-xl font-bold text-[#1B3A7B] tracking-widest">{orgId || "—"}</p>
                <p className="text-xs text-gray-400 mt-0.5">
                  Share with employees to let them join
                </p>
              </div>
              <button
                onClick={copyOrgId}
                className="p-2.5 text-blue-700 hover:bg-blue-50 rounded-xl transition flex-shrink-0"
              >
                {copied ? <Check size={18} /> : <Copy size={18} />}
              </button>
            </div>
          </div>

          {/* Logout */}
          <button
            onClick={handleLogout}
            className="w-full bg-white rounded-2xl shadow-sm flex items-center gap-3 px-5 py-4 text-red-500 hover:bg-red-50 transition"
          >
            <div className="w-9 h-9 bg-red-50 rounded-xl flex items-center justify-center flex-shrink-0">
              <LogOut size={17} className="text-red-500" />
            </div>
            <span className="font-semibold">Sign Out</span>
          </button>

        </div>
      </div>
    </AdminLayout>
  );
}

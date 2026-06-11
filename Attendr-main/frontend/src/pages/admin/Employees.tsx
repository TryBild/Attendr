import { useState } from "react";
import { Link } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { ArrowLeft, UserPlus, MoreVertical, X } from "lucide-react";
import { getEmployees, addEmployee, updateEmployee, deactivateEmployee, getDepartments } from "../../api/admin";
import { getInitials } from "../../lib/utils";
import toast from "react-hot-toast";

interface EmpForm { fullName: string; mobile: string; departmentId: string; employeeCode: string; designation: string; }
const EMPTY: EmpForm = { fullName: "", mobile: "", departmentId: "", employeeCode: "", designation: "" };

export default function Employees() {
  const qc = useQueryClient();
  const [search, setSearch] = useState("");
  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState<any>(null);
  const [form, setForm] = useState<EmpForm>(EMPTY);
  const [menuId, setMenuId] = useState<string | null>(null);

  const { data: empData, isLoading } = useQuery({ queryKey: ["employees"], queryFn: getEmployees });
  const { data: deptData } = useQuery({ queryKey: ["departments"], queryFn: getDepartments });

  const addMut = useMutation({
    mutationFn: (d: EmpForm) => addEmployee({ ...d, mobile: d.mobile, departmentId: d.departmentId || undefined }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["employees"] }); closeModal(); toast.success("Employee added!"); },
    onError: (e: any) => toast.error(e.message),
  });
  const updateMut = useMutation({
    mutationFn: ({ id, d }: { id: string; d: Partial<EmpForm> }) => updateEmployee(id, { ...d, departmentId: d.departmentId || undefined }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["employees"] }); closeModal(); toast.success("Employee updated!"); },
    onError: (e: any) => toast.error(e.message),
  });
  const deactivateMut = useMutation({
    mutationFn: (id: string) => deactivateEmployee(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["employees"] }); toast.success("Employee deactivated"); },
    onError: (e: any) => toast.error(e.message),
  });

  function openAdd() { setEditing(null); setForm(EMPTY); setShowModal(true); }
  function openEdit(emp: any) {
    setEditing(emp);
    setForm({ fullName: emp.fullName, mobile: emp.mobile, departmentId: emp.department?._id || "", employeeCode: emp.employeeCode || "", designation: emp.designation || "" });
    setShowModal(true);
  }
  function closeModal() { setShowModal(false); setEditing(null); setForm(EMPTY); }
  function setF(k: string, v: string) { setForm((f) => ({ ...f, [k]: v })); }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!form.fullName.trim()) { toast.error("Full name required"); return; }
    if (!editing && !/^[6-9]\d{9}$/.test(form.mobile)) { toast.error("Valid 10-digit mobile required"); return; }
    if (editing) updateMut.mutate({ id: editing._id, d: form });
    else addMut.mutate(form);
  }

  const departments = deptData?.departments || [];
  const filtered = (empData?.employees || []).filter((e: any) =>
    !search || e.fullName.toLowerCase().includes(search.toLowerCase()) || e.mobile.includes(search)
  );

  return (
    <div className="min-h-screen bg-gray-50 max-w-2xl mx-auto pb-8">
      <div className="bg-white px-5 pt-12 pb-4 shadow-sm">
        <div className="flex items-center gap-3">
          <Link to="/admin/dashboard" className="text-gray-500"><ArrowLeft size={20} /></Link>
          <h1 className="text-xl font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>Employees</h1>
          <button onClick={openAdd} className="ml-auto flex items-center gap-1 bg-blue-900 text-white px-4 py-2 rounded-full text-sm font-semibold">
            <UserPlus size={15} />Add
          </button>
        </div>
        <input placeholder="Search by name or mobile..." value={search} onChange={(e) => setSearch(e.target.value)}
          className="mt-4 w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm text-gray-800 bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-600" />
      </div>

      <div className="mx-5 mt-4 bg-white rounded-2xl shadow-sm overflow-hidden">
        {isLoading ? (
          <div className="py-12 flex justify-center"><svg className="animate-spin w-6 h-6 border-2 border-blue-600 border-t-transparent rounded-full" viewBox="0 0 24 24" /></div>
        ) : filtered.length === 0 ? (
          <p className="text-center text-gray-400 py-10 text-sm">No employees found</p>
        ) : filtered.map((emp: any, i: number) => (
          <div key={emp._id} className={`px-4 py-3 flex items-center gap-3 ${i > 0 ? "border-t border-gray-50" : ""}`}>
            <div className={`w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0 ${emp.isActive ? "bg-blue-100" : "bg-gray-100"}`}>
              <span className={`text-sm font-bold ${emp.isActive ? "text-blue-700" : "text-gray-400"}`}>{getInitials(emp.fullName)}</span>
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-semibold text-gray-800 truncate">{emp.fullName}</p>
              <p className="text-xs text-gray-400">+91 {emp.mobile} · {emp.department?.name || "—"}</p>
            </div>
            <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${emp.isActive ? "bg-green-100 text-green-700" : "bg-gray-100 text-gray-500"}`}>
              {emp.isActive ? "Active" : "Inactive"}
            </span>
            <div className="relative">
              <button onClick={() => setMenuId(menuId === emp._id ? null : emp._id)} className="p-1 text-gray-400 hover:text-gray-600">
                <MoreVertical size={16} />
              </button>
              {menuId === emp._id && (
                <div className="absolute right-0 top-7 bg-white border border-gray-200 rounded-xl shadow-lg z-50 min-w-32 py-1">
                  <button onClick={() => { openEdit(emp); setMenuId(null); }} className="block w-full text-left px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50">Edit</button>
                  {emp.isActive && <button onClick={() => { deactivateMut.mutate(emp._id); setMenuId(null); }} className="block w-full text-left px-4 py-2.5 text-sm text-red-600 hover:bg-red-50">Deactivate</button>}
                </div>
              )}
            </div>
          </div>
        ))}
      </div>

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black/40 flex items-end justify-center z-50 px-0">
          <div className="bg-white rounded-t-3xl w-full max-w-md p-6 pb-8">
            <div className="flex items-center justify-between mb-5">
              <h2 className="text-lg font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>{editing ? "Edit Employee" : "Add Employee"}</h2>
              <button onClick={closeModal}><X size={20} className="text-gray-400" /></button>
            </div>
            <form onSubmit={handleSubmit} className="space-y-4">
              {[
                { key: "fullName", label: "Full Name *", type: "text", placeholder: "Priya Patel" },
                { key: "mobile", label: "Mobile *", type: "tel", placeholder: "9876543210", disabled: !!editing },
                { key: "employeeCode", label: "Employee Code", type: "text", placeholder: "EMP001" },
                { key: "designation", label: "Designation", type: "text", placeholder: "Senior Developer" },
              ].map(({ key, label, type, placeholder, disabled }) => (
                <div key={key}>
                  <label className="block text-xs font-medium text-gray-600 mb-1">{label}</label>
                  <input type={type} value={form[key as keyof EmpForm]} onChange={(e) => setF(key, e.target.value)}
                    placeholder={placeholder} disabled={disabled}
                    className="w-full px-4 py-2.5 rounded-xl border border-gray-200 bg-gray-50 text-gray-900 text-sm focus:outline-none focus:ring-2 focus:ring-blue-600 disabled:opacity-50" />
                </div>
              ))}
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Department</label>
                <select value={form.departmentId} onChange={(e) => setF("departmentId", e.target.value)}
                  className="w-full px-4 py-2.5 rounded-xl border border-gray-200 bg-gray-50 text-gray-900 text-sm focus:outline-none focus:ring-2 focus:ring-blue-600">
                  <option value="">Select department</option>
                  {departments.map((d: any) => <option key={d._id} value={d._id}>{d.name}</option>)}
                </select>
              </div>
              <button type="submit" disabled={addMut.isPending || updateMut.isPending}
                className="w-full bg-blue-900 text-white py-3.5 rounded-full font-bold text-sm hover:bg-blue-800 transition disabled:opacity-60">
                {addMut.isPending || updateMut.isPending ? "Saving..." : editing ? "Save Changes" : "Add Employee"}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

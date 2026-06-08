import { useState } from "react";
import { Link } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { ArrowLeft, Plus, Pencil, Trash2, X, Check } from "lucide-react";
import { getDepartments, addDepartment, updateDepartment, deleteDepartment } from "../../api/admin";
import toast from "react-hot-toast";

export default function Departments() {
  const qc = useQueryClient();
  const [adding, setAdding]   = useState(false);
  const [editId, setEditId]   = useState<string | null>(null);
  const [newName, setNewName] = useState("");
  const [editName, setEditName] = useState("");

  const { data, isLoading } = useQuery({ queryKey: ["departments"], queryFn: getDepartments });

  const addMut = useMutation({
    mutationFn: () => addDepartment({ name: newName.trim() }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["departments"] }); setAdding(false); setNewName(""); toast.success("Department added!"); },
    onError: (e: any) => toast.error(e.message),
  });
  const updateMut = useMutation({
    mutationFn: ({ id }: { id: string }) => updateDepartment(id, { name: editName.trim() }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["departments"] }); setEditId(null); toast.success("Updated!"); },
    onError: (e: any) => toast.error(e.message),
  });
  const deleteMut = useMutation({
    mutationFn: (id: string) => deleteDepartment(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["departments"] }); toast.success("Department removed"); },
    onError: (e: any) => toast.error(e.message),
  });

  return (
    <div className="min-h-screen bg-gray-50 max-w-2xl mx-auto pb-8">
      <div className="bg-white px-5 pt-12 pb-4 shadow-sm">
        <div className="flex items-center gap-3">
          <Link to="/admin/dashboard" className="text-gray-500"><ArrowLeft size={20} /></Link>
          <h1 className="text-xl font-bold text-gray-900" style={{ fontFamily: "Nunito, sans-serif" }}>Departments</h1>
          <button onClick={() => setAdding(true)} className="ml-auto flex items-center gap-1 bg-blue-900 text-white px-4 py-2 rounded-full text-sm font-semibold">
            <Plus size={15} />Add
          </button>
        </div>
      </div>

      <div className="mx-5 mt-4 space-y-2">
        {adding && (
          <div className="bg-white rounded-2xl p-4 shadow-sm flex items-center gap-2">
            <input autoFocus value={newName} onChange={(e) => setNewName(e.target.value)}
              placeholder="Department name" onKeyDown={(e) => e.key === "Enter" && addMut.mutate()}
              className="flex-1 px-3 py-2 rounded-xl border border-gray-200 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600" />
            <button onClick={() => addMut.mutate()} disabled={!newName.trim() || addMut.isPending} className="p-2 bg-blue-900 text-white rounded-xl disabled:opacity-50"><Check size={16} /></button>
            <button onClick={() => { setAdding(false); setNewName(""); }} className="p-2 text-gray-400"><X size={16} /></button>
          </div>
        )}

        {isLoading ? (
          <div className="py-12 flex justify-center"><svg className="animate-spin w-6 h-6 border-2 border-blue-600 border-t-transparent rounded-full" viewBox="0 0 24 24" /></div>
        ) : (data?.departments || []).map((dept: any) => (
          <div key={dept._id} className="bg-white rounded-2xl p-4 shadow-sm flex items-center gap-3">
            <div className="w-10 h-10 bg-blue-50 rounded-xl flex items-center justify-center flex-shrink-0">
              <span className="text-blue-700 font-bold text-sm">{dept.name[0]?.toUpperCase()}</span>
            </div>
            {editId === dept._id ? (
              <input autoFocus value={editName} onChange={(e) => setEditName(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && updateMut.mutate({ id: dept._id })}
                className="flex-1 px-3 py-2 rounded-xl border border-blue-300 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-600" />
            ) : (
              <div className="flex-1">
                <p className="text-sm font-semibold text-gray-800">{dept.name}</p>
                <p className="text-xs text-gray-400">{dept.employeeCount || 0} employee{dept.employeeCount !== 1 ? "s" : ""}</p>
              </div>
            )}
            {editId === dept._id ? (
              <>
                <button onClick={() => updateMut.mutate({ id: dept._id })} disabled={updateMut.isPending} className="p-2 bg-blue-900 text-white rounded-xl disabled:opacity-50"><Check size={15} /></button>
                <button onClick={() => setEditId(null)} className="p-2 text-gray-400"><X size={15} /></button>
              </>
            ) : (
              <>
                <button onClick={() => { setEditId(dept._id); setEditName(dept.name); }} className="p-2 text-gray-400 hover:text-blue-600"><Pencil size={15} /></button>
                <button onClick={() => { if (dept.employeeCount > 0) { toast.error(`${dept.employeeCount} employee(s) assigned. Reassign first.`); return; } deleteMut.mutate(dept._id); }}
                  className="p-2 text-gray-400 hover:text-red-600"><Trash2 size={15} /></button>
              </>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

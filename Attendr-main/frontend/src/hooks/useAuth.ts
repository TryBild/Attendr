import { useAuthStore } from "../store/auth";

export function useAuth() {
  const { token, kind, companyId, employee, admin, setEmployee, setAdmin, clear } = useAuthStore();
  return {
    token,
    kind,
    companyId,
    employee,
    admin,
    isAuthenticated: !!token,
    isAdmin:         kind === "admin",
    isEmployee:      kind === "employee",
    setEmployee,
    setAdmin,
    logout: clear,
  };
}

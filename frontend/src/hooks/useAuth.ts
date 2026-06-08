import { useAuthStore } from "../store/auth";

export function useAuth() {
  const { token, kind, companyId, setAuth, clear } = useAuthStore();
  return {
    token,
    kind,
    companyId,
    isAuthenticated: !!token,
    isAdmin: kind === "admin",
    isEmployee: kind === "employee",
    setAuth,
    logout: clear,
  };
}

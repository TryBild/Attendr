import { create } from "zustand";
import { persist } from "zustand/middleware";

export type UserKind = "admin" | "employee";

export interface EmployeeInfo {
  id: string;
  fullName: string;
  mobile: string;
  employeeCode?: string;
  designation?: string;
  department?: string;
  joinedAt?: string;
  company: { name: string; teamId: string };
}

export interface AdminInfo {
  id: string;
  name: string;
  teamId: string;
  plan: string;
  city?: string;
  state?: string;
  setupComplete?: boolean;
}

interface AuthState {
  token: string | null;
  kind: UserKind | null;
  companyId: string | null;
  employee: EmployeeInfo | null;
  admin: AdminInfo | null;
  setEmployee:       (token: string, companyId: string, employee: EmployeeInfo) => void;
  setAdmin:          (token: string, companyId: string, admin: AdminInfo) => void;
  markSetupComplete: () => void;
  clear: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      kind: null,
      companyId: null,
      employee: null,
      admin: null,
      setEmployee: (token, companyId, employee) =>
        set({ token, kind: "employee", companyId, employee, admin: null }),
      setAdmin: (token, companyId, admin) =>
        set({ token, kind: "admin", companyId, employee: null, admin }),
      markSetupComplete: () =>
        set((state) => ({ admin: state.admin ? { ...state.admin, setupComplete: true } : null })),
      clear: () =>
        set({ token: null, kind: null, companyId: null, employee: null, admin: null }),
    }),
    { name: "attendr-auth" }
  )
);

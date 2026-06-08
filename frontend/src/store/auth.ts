import { create } from "zustand";
import { persist } from "zustand/middleware";

export type UserKind = "admin" | "employee";

interface AuthState {
  token: string | null;
  kind: UserKind | null;
  companyId: string | null;
  setAuth: (token: string, kind: UserKind, companyId: string) => void;
  clear: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      kind: null,
      companyId: null,
      setAuth: (token, kind, companyId) => set({ token, kind, companyId }),
      clear: () => set({ token: null, kind: null, companyId: null }),
    }),
    { name: "attendr-auth" }
  )
);

import { request } from "./client";
import { useAuthStore } from "../store/auth";

export const getMonthReport = (month?: string) =>
  request<{ ok: true; month: string; days: number; rows: any[] }>(
    `/reports/month${month ? `?month=${month}` : ""}`
  );

export async function downloadMonthCsv(month: string) {
  const token = useAuthStore.getState().token;
  const res = await fetch(
    `/api/reports/month.csv?month=${month}`,
    { headers: token ? { Authorization: `Bearer ${token}` } : {} }
  );
  if (!res.ok) throw new Error("Failed to download report");
  const blob = await res.blob();
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `attendance-${month}.csv`;
  a.click();
  URL.revokeObjectURL(url);
}

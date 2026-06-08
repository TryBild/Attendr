import { request } from "./client";
import { useAuthStore } from "../store/auth";

export interface DayRow {
  id: string;
  name: string;
  department: string;
  status: "present" | "absent";
  checkIn: string | null;
  checkOut: string | null;
  flagged: boolean;
}

export interface DayRegisterResponse {
  ok: true;
  date: string;
  present: number;
  total: number;
  rows: DayRow[];
}

export const getDayRegister = (date?: string) => {
  const qs = date ? `?date=${date}` : "";
  return request<DayRegisterResponse>(`/reports/register/day${qs}`);
};

export async function downloadMonthCsv(year: number, month: number) {
  const token = useAuthStore.getState().token;
  const res = await fetch(
    `/api/reports/register/month.csv?year=${year}&month=${month}`,
    { headers: token ? { Authorization: `Bearer ${token}` } : {} }
  );
  if (!res.ok) throw new Error("Failed to download report");
  const blob = await res.blob();
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `muster-roll-${year}-${month}.csv`;
  a.click();
  URL.revokeObjectURL(url);
}

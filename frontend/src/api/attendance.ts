import { request } from "./client";

export interface AttendanceLog {
  _id: string;
  companyId: string;
  employeeId: string;
  type: "in" | "out";
  at: string;
  lat?: number;
  lng?: number;
  mockDetected: boolean;
}

export const markAttendance = (
  type: "in" | "out",
  options: { lat?: number; lng?: number; mock?: boolean } = {}
) =>
  request<{ ok: true; at: string; flagged: boolean }>("/attendance/mark", {
    method: "POST",
    body: JSON.stringify({ type, ...options }),
  });

export const getTodayLogs = () =>
  request<{ ok: true; logs: AttendanceLog[] }>("/attendance/today");

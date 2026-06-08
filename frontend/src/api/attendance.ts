import { request } from "./client";

export interface TodayAttendance {
  date: string;
  status: string;
  checkInTime:  string | null;
  checkOutTime: string | null;
  workingHours: number | null;
  geofence:     string | null;
}

export interface DayRecord {
  date:         string;
  status:       string;
  checkInTime:  string | null;
  checkOutTime: string | null;
  workingHours: number | null;
}

export interface MonthSummary {
  totalMarked: number;
  present:     number;
  absent:      number;
  late:        number;
  leaves:      number;
  workingDays: number;
  attendancePercent: number;
}

export const markAttendance = (latitude: number, longitude: number, action: "checkin" | "checkout") =>
  request<{ ok: true; action: string; time: string; status: string; workingHours?: number; geofence?: string; distance?: number }>("/attendance/mark", {
    method: "POST",
    body: JSON.stringify({ latitude, longitude, action }),
  });

export const getTodayAttendance = () =>
  request<TodayAttendance & { ok: true }>("/attendance/today");

export const getMyAttendance = (month?: string) =>
  request<{ ok: true; month: string; records: DayRecord[]; summary: MonthSummary }>(`/attendance/my${month ? `?month=${month}` : ""}`);

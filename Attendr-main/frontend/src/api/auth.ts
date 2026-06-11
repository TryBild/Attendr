import { request } from "./client";
import type { EmployeeInfo, AdminInfo } from "../store/auth";

export const requestOtp = (fullName: string, mobile: string, teamId: string) =>
  request<{ ok: true; message: string; expiresInMinutes: number }>("/auth/otp/request", {
    method: "POST",
    body: JSON.stringify({ fullName, mobile, teamId }),
  });

export const verifyOtp = (mobile: string, teamId: string, otp: string) =>
  request<{ ok: true; token: string; employee: EmployeeInfo }>("/auth/otp/verify", {
    method: "POST",
    body: JSON.stringify({ mobile, teamId, otp }),
  });

export const adminLogin = (email: string, password: string) =>
  request<{ ok: true; token: string; company: AdminInfo & { id: string } }>("/auth/admin/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });

export const adminRegister = (data: {
  companyName: string;
  adminEmail: string;
  password: string;
  city?: string;
  state?: string;
}) =>
  request<{ ok: true; token: string; company: { id: string; name: string; teamId: string } }>("/auth/admin/register", {
    method: "POST",
    body: JSON.stringify(data),
  });

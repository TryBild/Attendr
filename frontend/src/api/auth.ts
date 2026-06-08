import { request } from "./client";

export interface AdminVerifyResponse {
  ok: true;
  token: string;
  companyId: string;
}

export interface EmployeeVerifyResponse {
  ok: true;
  token: string;
  name: string;
  companyId: string;
}

export const requestOtp = (phone: string) =>
  request<{ ok: true; message: string }>("/auth/otp/request", {
    method: "POST",
    body: JSON.stringify({ phone }),
  });

export const verifyAdmin = (
  phone: string,
  code: string,
  companyName?: string
) =>
  request<AdminVerifyResponse>("/auth/admin/verify", {
    method: "POST",
    body: JSON.stringify({ phone, code, companyName }),
  });

export const verifyEmployee = (
  phone: string,
  code: string,
  deviceId?: string
) =>
  request<EmployeeVerifyResponse>("/auth/employee/verify", {
    method: "POST",
    body: JSON.stringify({ phone, code, deviceId }),
  });

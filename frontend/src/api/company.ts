import { request } from "./client";

export interface Geofence {
  label: string;
  lat: number;
  lng: number;
  radiusM: number;
}

export interface Company {
  _id: string;
  name: string;
  plan: "free" | "pro";
  geofences: Geofence[];
  requireGeofence: boolean;
  whatsappAdminNumbers: string[];
  trialEndsAt: string;
  active: boolean;
}

export interface Department {
  _id: string;
  companyId: string;
  name: string;
  active: boolean;
}

export interface Employee {
  _id: string;
  companyId: string;
  departmentId?: Department;
  name: string;
  phone: string;
  deviceId: string | null;
  active: boolean;
  consentAt: string | null;
}

export const getCompany = () =>
  request<{ ok: true; company: Company }>("/company");

export const updateGeofences = (
  geofences: Geofence[],
  requireGeofence?: boolean
) =>
  request<{ ok: true; company: Company }>("/company/geofences", {
    method: "PUT",
    body: JSON.stringify({ geofences, requireGeofence }),
  });

export const createDepartment = (name: string) =>
  request<{ ok: true; dept: Department }>("/company/departments", {
    method: "POST",
    body: JSON.stringify({ name }),
  });

export const getDepartments = () =>
  request<{ ok: true; depts: Department[] }>("/company/departments");

export const inviteEmployee = (
  name: string,
  phone: string,
  departmentId?: string
) =>
  request<{ ok: true; emp: Employee }>("/company/employees", {
    method: "POST",
    body: JSON.stringify({ name, phone, departmentId }),
  });

export const getEmployees = () =>
  request<{ ok: true; emps: Employee[] }>("/company/employees");

export const toggleEmployee = (id: string) =>
  request<{ ok: true; active: boolean }>(`/company/employees/${id}/toggle`, {
    method: "PATCH",
  });

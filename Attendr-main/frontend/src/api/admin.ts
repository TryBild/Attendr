import { request } from "./client";

// Dashboard
export const getDashboard = () =>
  request<{ ok: true; today: any; thisMonth: any; recentActivity: any[] }>("/admin/dashboard");

// Day Register
export const getDayRegister = (date?: string) =>
  request<{ ok: true; date: string; rows: any[]; present: number; total: number }>(
    `/admin/attendance/day${date ? `?date=${date}` : ""}`
  );

// Manual attendance
export const postManualAttendance = (data: {
  employeeId: string; date: string; status: string;
  checkInTime?: string; checkOutTime?: string; notes?: string;
}) =>
  request<{ ok: true; record: any }>("/admin/attendance/manual", {
    method: "POST",
    body: JSON.stringify(data),
  });

// Employees
export const getEmployees = () =>
  request<{ ok: true; employees: any[] }>("/admin/employees");

export const addEmployee = (data: {
  fullName: string; mobile: string; departmentId?: string;
  employeeCode?: string; designation?: string;
}) =>
  request<{ ok: true; employee: any }>("/admin/employees", {
    method: "POST",
    body: JSON.stringify(data),
  });

export const updateEmployee = (id: string, data: Partial<{
  fullName: string; departmentId: string; employeeCode: string;
  designation: string; isActive: boolean;
}>) =>
  request<{ ok: true; employee: any }>(`/admin/employees/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });

export const deactivateEmployee = (id: string) =>
  request<{ ok: true }>(`/admin/employees/${id}`, { method: "DELETE" });

// Departments
export const getDepartments = () =>
  request<{ ok: true; departments: any[] }>("/admin/departments");

export const addDepartment = (data: { name: string; description?: string }) =>
  request<{ ok: true; department: any }>("/admin/departments", {
    method: "POST",
    body: JSON.stringify(data),
  });

export const updateDepartment = (id: string, data: { name?: string; description?: string }) =>
  request<{ ok: true; department: any }>(`/admin/departments/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });

export const deleteDepartment = (id: string) =>
  request<{ ok: true }>(`/admin/departments/${id}`, { method: "DELETE" });

// Geofences
export const getGeofences = () =>
  request<{ ok: true; geofences: any[] }>("/admin/geofences");

export const addGeofence = (data: {
  name: string; latitude: number; longitude: number;
  radiusMeters?: number; address?: string;
}) =>
  request<{ ok: true; geofence: any }>("/admin/geofences", {
    method: "POST",
    body: JSON.stringify(data),
  });

export const updateGeofence = (id: string, data: Partial<{
  name: string; latitude: number; longitude: number;
  radiusMeters: number; address: string; isActive: boolean;
}>) =>
  request<{ ok: true; geofence: any }>(`/admin/geofences/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });

export const deleteGeofence = (id: string) =>
  request<{ ok: true }>(`/admin/geofences/${id}`, { method: "DELETE" });

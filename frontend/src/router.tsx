import { createBrowserRouter, Navigate } from "react-router-dom";
import { useAuth } from "./hooks/useAuth";
import { ProtectedRoute } from "./components/ProtectedRoute";

import AdminLogin from "./pages/auth/AdminLogin";
import EmployeeLogin from "./pages/auth/EmployeeLogin";

import Dashboard from "./pages/admin/Dashboard";
import DayRegister from "./pages/admin/DayRegister";
import Employees from "./pages/admin/Employees";
import Departments from "./pages/admin/Departments";
import Geofences from "./pages/admin/Geofences";
import MonthReport from "./pages/admin/MonthReport";

import ClockInOut from "./pages/employee/ClockInOut";

function RootRedirect() {
  const { isAuthenticated, kind } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login/admin" replace />;
  return <Navigate to={kind === "admin" ? "/admin" : "/employee"} replace />;
}

export const router = createBrowserRouter([
  { path: "/", element: <RootRedirect /> },
  { path: "/login/admin", element: <AdminLogin /> },
  { path: "/login/employee", element: <EmployeeLogin /> },

  {
    path: "/admin",
    element: (
      <ProtectedRoute kind="admin">
        <Dashboard />
      </ProtectedRoute>
    ),
  },
  {
    path: "/admin/register",
    element: (
      <ProtectedRoute kind="admin">
        <DayRegister />
      </ProtectedRoute>
    ),
  },
  {
    path: "/admin/employees",
    element: (
      <ProtectedRoute kind="admin">
        <Employees />
      </ProtectedRoute>
    ),
  },
  {
    path: "/admin/departments",
    element: (
      <ProtectedRoute kind="admin">
        <Departments />
      </ProtectedRoute>
    ),
  },
  {
    path: "/admin/geofences",
    element: (
      <ProtectedRoute kind="admin">
        <Geofences />
      </ProtectedRoute>
    ),
  },
  {
    path: "/admin/reports",
    element: (
      <ProtectedRoute kind="admin">
        <MonthReport />
      </ProtectedRoute>
    ),
  },

  {
    path: "/employee",
    element: (
      <ProtectedRoute kind="employee">
        <ClockInOut />
      </ProtectedRoute>
    ),
  },

  { path: "*", element: <Navigate to="/" replace /> },
]);

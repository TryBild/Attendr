import { createBrowserRouter, Navigate } from "react-router-dom";
import { useAuth } from "./hooks/useAuth";

import Welcome          from "./pages/Welcome";
import RoleSelection    from "./pages/RoleSelection";
import EmployeeRegister from "./pages/auth/EmployeeRegister";
import EmployeeOTP    from "./pages/auth/EmployeeOTP";
import AdminLogin     from "./pages/auth/AdminLogin";
import AdminRegister  from "./pages/auth/AdminRegister";

import ClockInOut     from "./pages/employee/ClockInOut";
import MyAttendance   from "./pages/employee/MyAttendance";
import Profile        from "./pages/employee/Profile";

import Dashboard      from "./pages/admin/Dashboard";
import DayRegister    from "./pages/admin/DayRegister";
import Employees      from "./pages/admin/Employees";
import Departments    from "./pages/admin/Departments";
import Geofences      from "./pages/admin/Geofences";
import MonthReport    from "./pages/admin/MonthReport";

import HelpCenter     from "./pages/help/HelpCenter";
import HelpArticle    from "./pages/help/HelpArticle";
import ContactSupport from "./pages/help/ContactSupport";

import { ProtectedRoute } from "./components/ProtectedRoute";

export const router = createBrowserRouter([
  { path: "/",               element: <Welcome /> },
  { path: "/role",           element: <RoleSelection /> },
  { path: "/register",       element: <EmployeeRegister /> },
  { path: "/verify-otp",     element: <EmployeeOTP /> },
  { path: "/admin/login",    element: <AdminLogin /> },
  { path: "/admin/register", element: <AdminRegister /> },

  // Employee routes
  {
    path: "/employee/home",
    element: <ProtectedRoute kind="employee"><ClockInOut /></ProtectedRoute>,
  },
  {
    path: "/employee/attendance",
    element: <ProtectedRoute kind="employee"><MyAttendance /></ProtectedRoute>,
  },
  {
    path: "/employee/profile",
    element: <ProtectedRoute kind="employee"><Profile /></ProtectedRoute>,
  },

  // Admin routes
  {
    path: "/admin/dashboard",
    element: <ProtectedRoute kind="admin"><Dashboard /></ProtectedRoute>,
  },
  {
    path: "/admin/day-register",
    element: <ProtectedRoute kind="admin"><DayRegister /></ProtectedRoute>,
  },
  {
    path: "/admin/employees",
    element: <ProtectedRoute kind="admin"><Employees /></ProtectedRoute>,
  },
  {
    path: "/admin/departments",
    element: <ProtectedRoute kind="admin"><Departments /></ProtectedRoute>,
  },
  {
    path: "/admin/geofences",
    element: <ProtectedRoute kind="admin"><Geofences /></ProtectedRoute>,
  },
  {
    path: "/admin/reports",
    element: <ProtectedRoute kind="admin"><MonthReport /></ProtectedRoute>,
  },

  // Help routes (public)
  { path: "/help",         element: <HelpCenter /> },
  { path: "/help/contact", element: <ContactSupport /> },
  { path: "/help/:slug",   element: <HelpArticle /> },

  { path: "*", element: <Navigate to="/" replace /> },
]);

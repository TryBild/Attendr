import { Router } from "express";
import { requireAuth } from "../middleware/auth.js";
import { requireActiveSubscription } from "../middleware/subscriptionCheck.js";
import {
  getDashboard,
  getDayRegister,
  getEmployees, addEmployee, updateEmployee, deactivateEmployee, resetDevice,
  getDepartments, addDepartment, updateDepartment, deleteDepartment,
  getGeofences, addGeofence, updateGeofence, deleteGeofence,
  manualAttendance,
} from "../controllers/admin.controller.js";

const router = Router();
router.use(requireAuth(["admin"]));

router.get("/dashboard",             getDashboard);
router.get("/attendance/day",        getDayRegister);
router.post("/attendance/manual",    requireActiveSubscription, manualAttendance);

router.get("/employees",             getEmployees);
router.post("/employees",            requireActiveSubscription, addEmployee);
router.put("/employees/:id",         requireActiveSubscription, updateEmployee);
router.delete("/employees/:id",      requireActiveSubscription, deactivateEmployee);
router.post("/employees/:id/reset-device", requireActiveSubscription, resetDevice);

router.get("/departments",           getDepartments);
router.post("/departments",          requireActiveSubscription, addDepartment);
router.put("/departments/:id",       requireActiveSubscription, updateDepartment);
router.delete("/departments/:id",    requireActiveSubscription, deleteDepartment);

router.get("/geofences",             getGeofences);
router.post("/geofences",            requireActiveSubscription, addGeofence);
router.put("/geofences/:id",         requireActiveSubscription, updateGeofence);
router.delete("/geofences/:id",      requireActiveSubscription, deleteGeofence);

export default router;

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
router.use(requireActiveSubscription);

router.get("/dashboard",             getDashboard);
router.get("/attendance/day",        getDayRegister);
router.post("/attendance/manual",    manualAttendance);

router.get("/employees",             getEmployees);
router.post("/employees",            addEmployee);
router.put("/employees/:id",         updateEmployee);
router.delete("/employees/:id",      deactivateEmployee);
router.post("/employees/:id/reset-device", resetDevice);

router.get("/departments",           getDepartments);
router.post("/departments",          addDepartment);
router.put("/departments/:id",       updateDepartment);
router.delete("/departments/:id",    deleteDepartment);

router.get("/geofences",             getGeofences);
router.post("/geofences",            addGeofence);
router.put("/geofences/:id",         updateGeofence);
router.delete("/geofences/:id",      deleteGeofence);

export default router;

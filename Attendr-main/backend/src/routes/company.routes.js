import { Router } from "express";
import { requireAuth } from "../middleware/auth.js";
import { tenantScope } from "../middleware/tenantScope.js";
import {
  getCompany, updateGeofences,
  createDepartment, getDepartments,
  inviteEmployee, getEmployees, toggleEmployee,
} from "../controllers/company.controller.js";

const router = Router();
router.use(requireAuth(["admin"]), tenantScope);

router.get ("/",                  getCompany);
router.put ("/geofences",         updateGeofences);
router.post("/departments",       createDepartment);
router.get ("/departments",       getDepartments);
router.post("/employees",         inviteEmployee);
router.get ("/employees",         getEmployees);
router.patch("/employees/:id/toggle", toggleEmployee);

export default router;

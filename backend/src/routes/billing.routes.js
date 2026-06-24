import { Router } from "express";
import { requireAuth } from "../middleware/auth.js";
import { getBillingStatus, createBillingSubscription, cancelBillingSubscription } from "../controllers/billing.controller.js";

const router = Router();
router.use(requireAuth(["admin"]));

router.get("/status",              getBillingStatus);
router.post("/create-subscription", createBillingSubscription);
router.post("/cancel",             cancelBillingSubscription);

export default router;

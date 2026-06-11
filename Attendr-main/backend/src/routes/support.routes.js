import { Router } from "express";
import { createTicket } from "../controllers/support.controller.js";

const router = Router();

router.post("/ticket", createTicket);

export default router;

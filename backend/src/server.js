import "dotenv/config";
import express from "express";
import cors from "cors";
import cron from "node-cron";
import { connectDB } from "./config/db.js";
import { errorHandler } from "./middleware/errorHandler.js";
import { runDailyDigest } from "./jobs/dailyDigest.job.js";
import { runCleanup } from "./jobs/cleanup.job.js";

import authRoutes       from "./routes/auth.routes.js";
import companyRoutes    from "./routes/company.routes.js";
import attendanceRoutes from "./routes/attendance.routes.js";
import reportsRoutes    from "./routes/reports.routes.js";

const app = express();
app.use(cors());
app.use(express.json());

// Routes
app.get("/health", (_req, res) => res.json({ ok: true, app: "Attendr", v: "0.1.0" }));
app.use("/auth",       authRoutes);
app.use("/company",    companyRoutes);
app.use("/attendance", attendanceRoutes);
app.use("/reports",    reportsRoutes);

// Global error handler (must be last)
app.use(errorHandler);

// Cron jobs (IST timezone)
cron.schedule("0 8 * * *",  runDailyDigest, { timezone: "Asia/Kolkata" }); // 8 AM daily
cron.schedule("0 2 * * *",  runCleanup,     { timezone: "Asia/Kolkata" }); // 2 AM cleanup

const PORT = process.env.PORT || 4000;
connectDB(process.env.MONGO_URI).then(() =>
  app.listen(PORT, () => console.log(`✓ Attendr API running on :${PORT}`))
);

import "dotenv/config";
import express from "express";
import cors from "cors";
import helmet from "helmet";
import morgan from "morgan";
import { connectDB } from "./config/db.js";
import { errorHandler } from "./middleware/errorHandler.js";
import { generalLimiter } from "./middleware/rateLimiter.js";

import authRoutes       from "./routes/auth.routes.js";
import attendanceRoutes from "./routes/attendance.routes.js";
import adminRoutes      from "./routes/admin.routes.js";
import reportsRoutes    from "./routes/reports.routes.js";
import supportRoutes    from "./routes/support.routes.js";

const app = express();
app.set('trust proxy', 1);

// Security middleware
app.use(helmet());
app.use(cors({
  origin: process.env.ALLOWED_ORIGIN || "*",
  credentials: true,
}));
app.use(morgan("dev"));
app.use(express.json({ limit: "10mb" }));
app.use(generalLimiter);

// Health check
app.get("/api/health", (_req, res) => res.json({ ok: true, app: "Attendr", v: "1.0.0" }));

// Routes
app.use("/api/auth",       authRoutes);
app.use("/api/attendance", attendanceRoutes);
app.use("/api/admin",      adminRoutes);
app.use("/api/reports",    reportsRoutes);
app.use("/api/support",    supportRoutes);

// Global error handler
app.use(errorHandler);

const PORT = process.env.PORT || 4000;
connectDB(process.env.MONGO_URI).then(() =>
  app.listen(PORT, () => console.log(`✓ Attendr API running on :${PORT}`))
);

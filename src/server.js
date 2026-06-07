import "dotenv/config";
import express from "express";
import cors from "cors";
import { connectDB } from "./config/db.js";

const app = express();
app.use(cors());
app.use(express.json());

app.get("/health", (_req, res) => res.json({ ok: true, app: "Attendr" }));

const PORT = process.env.PORT || 4000;
connectDB(process.env.MONGO_URI).then(() =>
  app.listen(PORT, () => console.log(`✓ Server on :${PORT}`))
);

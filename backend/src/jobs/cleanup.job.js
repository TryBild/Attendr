import { Otp } from "../models/index.js";

export async function runCleanup() {
  const result = await Otp.deleteMany({ expiresAt: { $lt: new Date() } });
  if (result.deletedCount > 0)
    console.log(`[Cleanup] Deleted ${result.deletedCount} expired OTPs`);
}

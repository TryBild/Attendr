import { Company, Employee, AttendanceLog } from "../models/index.js";
import { sendDigest } from "../services/whatsapp.js";
import { dayStart } from "../utils/dateUtils.js";

export async function runDailyDigest() {
  const start = dayStart();
  const companies = await Company.find({ active: true });

  for (const c of companies) {
    try {
      const total   = await Employee.countDocuments({ companyId: c._id, active: true });
      const logs    = await AttendanceLog.find({ companyId: c._id, type: "in", at: { $gte: start } });
      const present = new Set(logs.map((l) => String(l.employeeId))).size;
      const flagged = logs.filter((l) => l.mockDetected).length;

      const msg =
        `📋 *${c.name}* — ${start.toDateString()}\n\n` +
        `✅ Present : ${present}\n` +
        `❌ Absent  : ${total - present}\n` +
        `👥 Total   : ${total}\n` +
        (flagged ? `⚠️ Fake GPS flagged: ${flagged}` : `✔️ No flags today`);

      for (const num of c.whatsappAdminNumbers || []) {
        await sendDigest(num, msg);
      }
    } catch (e) {
      console.error(`Digest failed for ${c.name}:`, e.message);
    }
  }
}

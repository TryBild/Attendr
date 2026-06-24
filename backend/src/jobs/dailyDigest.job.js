import cron from "node-cron";
import { Company, Employee, Attendance } from "../models/index.js";
import { sendDailyDigest, sendWeeklySummary } from "../services/whatsapp.js";

function todayIST() {
  return new Date().toLocaleDateString("en-CA", { timeZone: "Asia/Kolkata" });
}

function isSunday() {
  const now = new Date();
  const day = parseInt(
    new Intl.DateTimeFormat("en-US", { weekday: "narrow", timeZone: "Asia/Kolkata" }).format(now),
    10,
  );
  return new Date(
    now.toLocaleString("en-US", { timeZone: "Asia/Kolkata" }),
  ).getDay() === 0;
}

function getWeekRange() {
  const now = new Date(new Date().toLocaleString("en-US", { timeZone: "Asia/Kolkata" }));
  const end = new Date(now);
  end.setDate(end.getDate() - 1); // Saturday
  const start = new Date(end);
  start.setDate(start.getDate() - 6); // previous Sunday

  const fmt = (d) =>
    `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
  return { start: fmt(start), end: fmt(end) };
}

function isLate(checkInTime, workStartTime) {
  if (!checkInTime || !workStartTime) return false;
  const [startH, startM] = workStartTime.split(":").map(Number);
  const graceMinutes = 15;
  const thresholdMs = (startH * 60 + startM + graceMinutes) * 60 * 1000;

  const checkIn = new Date(checkInTime);
  const istStr = checkIn.toLocaleString("en-US", { timeZone: "Asia/Kolkata" });
  const ist = new Date(istStr);
  const checkInMs = (ist.getHours() * 60 + ist.getMinutes()) * 60 * 1000 + ist.getSeconds() * 1000;

  return checkInMs > thresholdMs;
}

async function processDaily(company) {
  const date = todayIST();
  const activeEmployees = await Employee.countDocuments({
    company: company._id,
    isActive: true,
  });
  if (activeEmployees === 0) return;

  const records = await Attendance.find({ company: company._id, date });

  const present = records.filter((r) => ["present", "late"].includes(r.status)).length;
  const absent = activeEmployees - present;
  const late = records.filter(
    (r) => r.status === "late" || isLate(r.checkInTime, company.workStartTime),
  ).length;
  const fakeGps = records.filter((r) => r.mockDetected === true).length;

  for (const phone of company.whatsappAdminNumbers) {
    await sendDailyDigest(phone, { present, absent, late, fakeGps, date });
  }

  if (company.phone && !company.whatsappAdminNumbers.includes(company.phone)) {
    await sendDailyDigest(company.phone, { present, absent, late, fakeGps, date });
  }
}

async function processWeekly(company) {
  const { start, end } = getWeekRange();
  const activeEmployees = await Employee.countDocuments({
    company: company._id,
    isActive: true,
  });
  if (activeEmployees === 0) return;

  const records = await Attendance.find({
    company: company._id,
    date: { $gte: start, $lte: end },
  });

  const dayMap = new Map();
  for (const r of records) {
    if (!dayMap.has(r.date)) dayMap.set(r.date, []);
    dayMap.get(r.date).push(r);
  }

  let totalPresent = 0;
  let totalLate = 0;
  let totalFakeGps = 0;
  let workingDays = 0;

  for (const [, dayRecords] of dayMap) {
    workingDays++;
    totalPresent += dayRecords.filter((r) => ["present", "late"].includes(r.status)).length;
    totalLate += dayRecords.filter(
      (r) => r.status === "late" || isLate(r.checkInTime, company.workStartTime),
    ).length;
    totalFakeGps += dayRecords.filter((r) => r.mockDetected === true).length;
  }

  const avgPresent = workingDays > 0 ? Math.round(totalPresent / workingDays) : 0;
  const totalAbsent = workingDays > 0 ? activeEmployees * workingDays - totalPresent : 0;

  const phones = [...company.whatsappAdminNumbers];
  if (company.phone && !phones.includes(company.phone)) phones.push(company.phone);

  for (const phone of phones) {
    await sendWeeklySummary(phone, {
      weekStart: start,
      weekEnd: end,
      avgPresent,
      totalAbsent,
      totalLate,
      totalFakeGps,
    });
  }
}

async function runDigest() {
  console.log("[DailyDigest] Starting...");
  try {
    const companies = await Company.find({ isActive: true, setupComplete: true });
    const sunday = isSunday();

    for (const company of companies) {
      try {
        if (sunday) {
          await processWeekly(company);
        } else {
          await processDaily(company);
        }
      } catch (e) {
        console.error(`[DailyDigest] Error for company ${company.teamId}: ${e.message}`);
      }
    }

    console.log(`[DailyDigest] Done — ${companies.length} companies processed (${sunday ? "weekly" : "daily"})`);
  } catch (e) {
    console.error(`[DailyDigest] Fatal: ${e.message}`);
  }
}

export function startDailyDigestJob() {
  // 8:00 AM IST daily — cron uses server TZ, so specify IST explicitly
  cron.schedule("0 8 * * *", runDigest, {
    timezone: "Asia/Kolkata",
  });
  console.log("✓ Daily digest cron scheduled (08:00 IST)");
}

// Allow manual trigger for testing
export { runDigest };

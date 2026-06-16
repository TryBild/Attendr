/**
 * One-off cleanup: remove stale indexes/records on the `employees` collection.
 *
 * Background:
 *   The Employee schema uses `company` + `mobile` (unique index mobile_1_company_1).
 *   Older orphan indexes `companyId_1_phone_1` (unique) and `companyId_1` reference
 *   fields that no longer exist on Employee documents. Mongo treats the missing
 *   fields as null, so the unique companyId_1_phone_1 index throws E11000
 *   ({ companyId: null, phone: null }) on every insert after the first — breaking
 *   employee OTP registration.
 *
 * This script drops those orphan indexes and removes any leftover null/null row.
 * It is safe to run repeatedly (idempotent).
 *
 * Usage:
 *   MONGO_URI="<connection string>" node scripts/fix-employee-indexes.js
 *   (falls back to MONGODB_URI, then the local default)
 */
import mongoose from "mongoose";

const uri =
  process.env.MONGO_URI ||
  process.env.MONGODB_URI ||
  "mongodb://127.0.0.1:27017/attendr";

const ORPHAN_INDEXES = ["companyId_1_phone_1", "companyId_1"];

async function main() {
  await mongoose.connect(uri, { serverSelectionTimeoutMS: 8000 });
  const col = mongoose.connection.db.collection("employees");

  console.log("Connected. Inspecting `employees` collection…\n");

  const before = await col.indexes();
  console.log("Current indexes:");
  before.forEach((i) => console.log(`  - ${i.name}: ${JSON.stringify(i.key)}${i.unique ? " (unique)" : ""}`));
  console.log("");

  // 1. Drop orphan indexes that reference non-schema fields.
  for (const name of ORPHAN_INDEXES) {
    const exists = before.some((i) => i.name === name);
    if (exists) {
      await col.dropIndex(name);
      console.log(`✓ Dropped orphan index: ${name}`);
    } else {
      console.log(`• Orphan index not present (already clean): ${name}`);
    }
  }
  console.log("");

  // 2. Remove stale null/null record(s) left behind by the bad index.
  const badFilter = { companyId: null, phone: null };
  const badCount = await col.countDocuments(badFilter);
  console.log(`Stale records matching { companyId: null, phone: null }: ${badCount}`);
  if (badCount > 0) {
    const r = await col.deleteMany(badFilter);
    console.log(`✓ Deleted ${r.deletedCount} stale record(s).`);
  }
  console.log("");

  const after = await col.indexes();
  console.log("Indexes after cleanup:");
  after.forEach((i) => console.log(`  - ${i.name}: ${JSON.stringify(i.key)}${i.unique ? " (unique)" : ""}`));

  await mongoose.disconnect();
  console.log("\nDone.");
}

main().catch((e) => {
  console.error("Cleanup failed:", e.message);
  process.exit(1);
});

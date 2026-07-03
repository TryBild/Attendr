import { SecurityLog } from "../models/index.js";

// Fire-and-forget audit trail — must never block or fail the auth flow.
export function logSecurityEvent({ companyId, userId, userKind, event, req }) {
  SecurityLog.create({
    companyId: companyId || null,
    userId:    userId || null,
    userKind,
    event,
    ip:        req?.ip,
    userAgent: req?.headers?.["user-agent"],
  }).catch((e) => console.error("[SecurityLog] failed:", e.message));
}

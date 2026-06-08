// CRITICAL — Every authenticated route must pass through this.
// Ensures req.auth.companyId is always present.
// No route should ever query DB without filtering by companyId.
export function tenantScope(req, res, next) {
  if (!req.auth?.companyId) {
    return res.status(403).json({ ok: false, error: "Tenant not identified" });
  }
  next();
}

import jwt from "jsonwebtoken";

export function requireAuth(kinds = ["admin", "employee"]) {
  return (req, res, next) => {
    const header = req.headers.authorization || "";
    const token = header.startsWith("Bearer ") ? header.slice(7) : null;
    if (!token) return res.status(401).json({ ok: false, error: "Invalid session." });
    try {
      // Pin HS256 so a forged token can't downgrade the algorithm (alg:none attack)
      const payload = jwt.verify(token, process.env.JWT_SECRET, { algorithms: ["HS256"] });
      if (!payload.id || !payload.companyId || !payload.kind)
        return res.status(401).json({ ok: false, error: "Invalid session." });
      if (!kinds.includes(payload.kind))
        return res.status(403).json({ ok: false, error: "Forbidden" });
      req.user = { id: payload.id, companyId: payload.companyId, kind: payload.kind };
      req.auth = payload; // legacy alias — controllers read req.auth.companyId / .id
      next();
    } catch (e) {
      if (e.name === "TokenExpiredError")
        return res.status(401).json({ ok: false, error: "Session expired. Please log in again." });
      return res.status(401).json({ ok: false, error: "Invalid session." });
    }
  };
}

export function signToken(payload, expiresIn) {
  return jwt.sign(payload, process.env.JWT_SECRET, {
    algorithm: "HS256",
    expiresIn: expiresIn || process.env.JWT_EXPIRES_IN || "30d",
  });
}

import jwt from "jsonwebtoken";

export function requireAuth(kinds = ["admin", "employee"]) {
  return (req, res, next) => {
    const header = req.headers.authorization || "";
    const token = header.startsWith("Bearer ") ? header.slice(7) : null;
    if (!token) return res.status(401).json({ ok: false, error: "No token" });
    try {
      const payload = jwt.verify(token, process.env.JWT_SECRET);
      if (!kinds.includes(payload.kind))
        return res.status(403).json({ ok: false, error: "Forbidden" });
      req.auth = payload;
      next();
    } catch {
      return res.status(401).json({ ok: false, error: "Invalid or expired token" });
    }
  };
}

export function signToken(payload, expiresIn) {
  return jwt.sign(payload, process.env.JWT_SECRET, {
    expiresIn: expiresIn || process.env.JWT_EXPIRES_IN || "30d",
  });
}

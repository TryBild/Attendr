export function errorHandler(err, req, res, next) {
  console.error(`[ERROR] ${req.method} ${req.path}:`, err.message);

  if (err.name === "ValidationError") {
    return res.status(400).json({ ok: false, error: err.message });
  }
  if (err.code === 11000) {
    return res.status(409).json({ ok: false, error: "Duplicate entry" });
  }
  res.status(err.status || 500).json({
    ok: false,
    error: err.message || "Internal server error",
  });
}

const attempts = new Map();

// Simple in-memory rate limiter for OTP endpoint
// Max 5 requests per phone per 10 minutes
export function otpRateLimiter(req, res, next) {
  const phone = req.body?.phone;
  if (!phone) return next();

  const now = Date.now();
  const windowMs = 10 * 60 * 1000;
  const max = 5;

  const entry = attempts.get(phone) || { count: 0, resetAt: now + windowMs };

  if (now > entry.resetAt) {
    entry.count = 0;
    entry.resetAt = now + windowMs;
  }

  entry.count++;
  attempts.set(phone, entry);

  if (entry.count > max) {
    return res.status(429).json({
      ok: false,
      error: "Too many OTP requests. Try again in 10 minutes.",
    });
  }
  next();
}

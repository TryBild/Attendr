import rateLimit, { ipKeyGenerator } from "express-rate-limit";

export const otpRateLimiter = rateLimit({
  windowMs:         10 * 60 * 1000,
  max:              5,
  keyGenerator:     (req) => req.body?.mobile || ipKeyGenerator(req),
  standardHeaders:  true,
  legacyHeaders:    false,
  handler: (_req, res) => {
    res.status(429).json({ ok: false, error: "Too many OTP requests. Try again in 10 minutes." });
  },
});

export const otpVerifyLimiter = rateLimit({
  windowMs:         15 * 60 * 1000,
  max:              10,
  // key by mobile so attackers can't rotate IPs; ipKeyGenerator fallback is
  // required by express-rate-limit v8 (raw req.ip fails its IPv6 validation)
  keyGenerator:     (req) => req.body?.mobile || ipKeyGenerator(req),
  standardHeaders:  true,
  legacyHeaders:    false,
  handler: (_req, res) => {
    res.status(429).json({ ok: false, error: "Too many attempts. Try again in 15 minutes." });
  },
});

export const generalLimiter = rateLimit({
  windowMs:        15 * 60 * 1000,
  max:             200,
  keyGenerator:    ipKeyGenerator,
  standardHeaders: true,
  legacyHeaders:   false,
  handler: (_req, res) => {
    res.status(429).json({ ok: false, error: "Too many requests. Please slow down." });
  },
});

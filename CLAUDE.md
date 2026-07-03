PROJECT: Attendr — GPS-based digital attendance SaaS for Indian SMBs/factories/shops.
Builder: Nick (Rahul Yadav), TryBild studio, Mumbai.

TECH STACK (current, real — don't assume anything else):
- Backend: Node.js + Express + MongoDB (Mongoose), JWT auth, bcrypt
  - Repo: TryBild/Attendr → backend/ subfolder
  - Hosted: Render (https://attendr-mfyr.onrender.com), root directory = "backend"
  - OTP delivery: Fast2SMS (Quick SMS route "q", NOT DLT route "otp")
  - DB: MongoDB Atlas
- Frontend: Android native — Kotlin + Jetpack Compose, MVVM, Retrofit, DataStore
  - Repo: TryBild/Attendr → root (app/ folder), package com.trybild.attendr
  - Built/iterated heavily via Claude Code (terminal) on Android Studio

AUTH MODEL (current real backend, not the old WhatsApp-OTP design):
- Admin: registers via companyName + adminEmail + password → gets unique teamId (e.g. TRY190)
  - Admin login: email + password (NOT OTP)
- Employee: registers via fullName + mobile + teamId → OTP via SMS → verify → JWT
- Routes live under /api/auth/*, /api/attendance/*, /api/admin/*, /api/reports/*, /api/support/*

SECURITY — IMPLEMENTED (verified against backend code 2026-07-03 — do NOT redo):
- bcrypt saltRounds=12 passwords, saltRounds=10 OTPs (hashed in DB, never plain text)
- OTP: 6-digit, single-use, 10min expiry; >5 requests = 30min lock, 3 failed verifies = 15min lock
- pendingToken: JWT, expires 15min, kind:"pending"
- Rate limiting: otpRequest 5/10min (keyed by mobile), global 200/15min (keyed by IP)
- JWT payload: id + companyId + kind; requireAuth(kinds) middleware for role checks
- tenantScope middleware: every authed route requires companyId — all queries must filter by it
- Input validation: manual validation on all routes (NOT Zod — no Zod in backend)
- helmet + CORS (single ALLOWED_ORIGIN env var on Render; falls back to "*" if unset)
- Support ticket emails via nodemailer v9
- node_modules tracked in git (known issue, cleanup pending)

SECURITY — NOT IMPLEMENTED (old docs falsely claimed these; treat as pending hardening work):
- AES-256-GCM encryption on lat/lng — coordinates stored plain
- crypto.randomInt() for OTP — currently Math.random(), should be upgraded
- Separate otpVerify / auth-login rate limiters
- HS256 algorithm enforcement on jwt.verify
- Account lockout warning emails / 24hr lock tier
- Security alert emails, SecurityLog model, mongo-sanitize

WORKING STYLE:
- Self-taught, not a CS grad. Explain technical concepts in plain language when asked,
  but give exact runnable commands/code by default — don't oversimplify working sessions.
- Communicate in Hinglish, direct, no corporate hedging, no unnecessary disclaimers.
- When debugging: ask for exact terminal output / screenshots, diagnose from real evidence,
  don't guess. Give copy-paste-ready fixes, not vague suggestions.
- Often paste terminal output or screenshots — read them carefully before responding,
  many bugs come from mismatched file paths, wrong branches, or stale Render config.
- When generating Claude Code prompts, make them extremely detailed and self-contained
  (file paths, exact behavior, constraints on what NOT to touch).

PRODUCT CONTEXT:
- Competitors: greytHR, Keka, SalaryBox, factoHR, HajariBook
- Attendr's differentiation: GPS+OTP only (no biometric/face), simple onboarding,
  transparent pricing, clean minimal UI (navy #1B3A7B brand color).
- Multi-tenant: every query must scope by companyId/teamId — never leak data across orgs.

DO NOT:
- Suggest switching frameworks/stacks unless explicitly asked.
- Assume the old WhatsApp-OTP or biometric design — permanently abandoned.
- Give generic "it depends" answers — clear recommendation with reasoning always.

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository shape

This is a monorepo with **one backend API and two independent client apps** that both talk to it:

- `backend/` — Node.js/Express/MongoDB API (source of truth for all business logic)
- `frontend/` — React + TypeScript + Vite web app (employee **and** admin UI, the original MVP)
- `app/` (root Gradle project, module `:app`) — native Android/Kotlin+Compose app (employee **and** admin UI, the actively-developed client)

The two clients are **not code-shared** — each has its own auth flow, models, and API client hand-written against the same REST contract. A backend contract change (route, request/response shape, JWT payload) must be manually mirrored in both `frontend/src/api/*` and `app/.../data/{api,model,repository}/*`, or one client silently breaks. When changing an auth or attendance endpoint, grep both client directories before assuming the change is complete.

## Commands

### Backend (`backend/`)
```bash
npm install
npm run dev      # node --watch src/server.js — runs on :4000
npm start        # node src/server.js (production)
```
No test suite and no lint script exist in this package — don't assume `npm test`/`npm run lint` work here. There's no local MongoDB dependency baked in; point `MONGO_URI` (or `MONGODB_URI`) at Atlas or a local instance via `.env` (copy from `.env.example`).

### Frontend (`frontend/`)
```bash
npm install
npm run dev       # vite dev server on :5173, proxies /api → localhost:4000
npm run build     # tsc -b && vite build  (type-checks as part of build)
npm run lint      # eslint .
npm run preview
```

### Android (`app/`, from repo root)
```bash
./gradlew assembleDebug
./gradlew test                # JVM unit tests (app/src/test) — currently just boilerplate
./gradlew connectedAndroidTest # instrumented tests (app/src/androidTest) — currently just boilerplate
```
The Android app's `Constants.BASE_URL` (`app/src/main/java/com/trybild/attendr/utils/Constants.kt`) is **hardcoded to the production Render URL** (`https://attendr-mfyr.onrender.com/api/`) — there is no debug/staging flavor. To test against a local backend, edit this constant directly (and don't commit that change). `MAPS_API_KEY` is read from `local.properties` (gitignored) via `manifestPlaceholders`, needed for the geofence map picker.

## Backend architecture

### Multi-tenancy — the one invariant that must never break
Every business document (`Employee`, `Attendance`, `Geofence`, `Department`, `Subscription`, …) carries a `company` field. **Every authenticated query must filter by the caller's `companyId` taken from the verified JWT — never from the request body/params/query.** The middleware chain enforces this:

```
requireAuth(["admin"] | ["employee"])  → verifies JWT (HS256 only), sets req.auth AND req.user
tenantScope                            → 403s if companyId is missing from the token
enforceKind("admin" | "employee")      → 403s if the token's kind doesn't match the route group
requireActiveSubscription              → 402s if the company's Subscription isn't active/trialing (fails open on internal error)
```
See `src/routes/admin.routes.js`, `attendance.routes.js`, `billing.routes.js`, `reports.routes.js` for the `router.use(...)` chains. Controllers still read `req.auth.companyId`/`req.auth.id` (the legacy field), not `req.user` — both are populated by `requireAuth` for compatibility, but don't introduce a third convention.

JWT payload is always `{ id, companyId, teamId, kind }` where `kind` is `"admin" | "employee" | "pending"`. Treat this shape as a contract — both clients decode/expect exactly this.

### Auth flows
- **Admin**: a `Company` document *is* the admin account (`adminEmail`/`adminPassword` live on `Company`, no separate admin-user model). Registration auto-generates `teamId` in the form `ATT-XXXX-XXXX`. Login is email+password.
- **Employee**: `mobile + teamId` identifies the account (unique compound index `{ mobile, company }`). Flow is OTP-gated:
  1. `POST /auth/otp/request` (purpose `register` or `forgot`) — OTP is bcrypt-hashed and stored on the `Employee` doc, delivered via Fast2SMS (or logged to console in dev if `FAST2SMS_API_KEY` is unset — refuses to fake-send in production).
  2. `POST /auth/otp/verify` — on success returns a short-lived `pendingToken` (JWT, `kind: "pending"`, 5 min expiry). This is **not** a usable session token.
  3. `POST /auth/employee/set-password` — exchanges `pendingToken` for a real JWT. This single endpoint handles both first-time registration and "forgot password" resets; there is no separate forgot-password route.
  4. `POST /auth/employee/login` — mobile+teamId+password for returning users.
- **Device binding**: employees are pinned to one `deviceId` after first login/set-password; a mismatched device on subsequent logins gets a 403, not silently allowed. Admin can clear it via `POST /admin/employees/:id/reset-device`.
- **Account lockout**: both employee and admin login count `loginAttempts` on wrong password — 5 attempts locks for 30 minutes, 10 locks for 24 hours (`lockedUntil` field, 423 response). Resets to 0 on success.

### Response convention
Controllers return `{ ok: true, ...data }` or `{ ok: false, error: "message" }` via `utils/response.js`'s `ok()`/`err()` helpers (some older handlers build `res.json()` manually but follow the same shape). Both clients branch on the `ok` boolean, not HTTP status alone — keep this envelope intact for any new/changed route.

### Security layers (defense in depth, in `src/middleware/` and `src/utils/`)
- `validate.js` + `validators/*.js` — Zod schemas per route; on success `req.body` is **replaced** with the parsed/coerced/stripped data, so controllers only ever see declared fields.
- `rateLimiter.js` — layered limiters: `globalLimiter` (200/min/IP, mounted for all routes), `otpRequestLimiter` (5/10min keyed on mobile+teamId), `otpVerifyLimiter` (10/15min/IP), `authLimiter` (20/15min/IP, on both logins + both registers).
- `mongoSanitize` is applied globally in `server.js` to strip `$`-operators/dotted keys from body/params/query (NoSQL injection).
- `auth.js` pins JWT verification to `HS256` only (no `alg:none` downgrade) and requires `id`/`companyId`/`kind` to be present in the payload.
- `utils/encryption.js` — AES-256-GCM field-level encryption ("iv:authTag:data" hex), applied via Mongoose pre-save/post-find hooks **only** to `Attendance.checkInLocation`/`checkOutLocation` lat/lng. **Deliberately not applied to `Employee.mobile`** — mobile is an equality-lookup/unique-index key, and random-IV AES-GCM would break both `findOne({ mobile, company })` lookups and the uniqueness guarantee. Don't add mobile encryption without first solving that (a deterministic/blind-index scheme would be needed). `ENCRYPTION_KEY` is required in production (`assertEncryptionKeyConfigured()` throws at boot if missing); dev falls back to a fixed insecure key.
- `SecurityLog` model (90-day TTL index) records `login_success`/`login_failed`/`account_locked`/`password_changed`/`password_reset_requested`/`otp_brute_force` — always written fire-and-forget via `utils/securityLog.js`, never awaited in the request path.
- Security alert emails (`services/securityAlert.js`) are also fire-and-forget. Employees don't have a captured email address in the registration flow today, so alerts fall back to the company admin's email (`employee.email || company.adminEmail`) — `Employee.email` exists on the schema for when that changes but is currently always empty in practice.

### Attendance & geofencing
Check-in/out (`attendance.controller.js`) validates the submitted GPS point against the company's active `Geofence` docs via haversine distance (`utils/geo.js`); a miss returns the distance to the nearest office in the error message. One `Attendance` doc per `(employee, date)` (unique index) — checkout requires an existing checkin for that date. `mockDetected` (fake-GPS) is recorded but never blocks the mark — admins see it flagged in the dashboard/reports instead. All date logic uses IST (`Asia/Kolkata`) via a repeated `todayIST()` helper pattern in each controller — there's no shared date-util module, so keep new date handling consistent with that pattern rather than introducing a different timezone approach.

### Jobs
`src/jobs/subscription.job.js` and `dailyDigest.job.js` run on `node-cron` and are only started when `NODE_ENV === "production"` (see bottom of `server.js`). The daily digest sends WhatsApp summaries (`services/whatsapp.js`) to `company.whatsappAdminNumbers`.

## Frontend architecture

React Router-based SPA (`src/router.tsx`) with parallel `pages/auth`, `pages/employee`, `pages/admin`, `pages/help` trees, gated by `ProtectedRoute`. Auth/session state lives in a Zustand store (`store/auth.ts`); `api/client.ts`'s `request()` wrapper attaches the bearer token, throws `ApiError` on `{ ok: false }`, and force-redirects to the right login page on a 401. `VITE_API_URL` selects the backend (empty string + Vite dev proxy locally, set explicitly in production per `render.yaml`).

## Deployment

`render.yaml` defines two Render services: `attendr-api` (backend, Node web service) and `attendr-frontend` (static site built from `frontend/`, SPA-rewrites all routes to `index.html`). The frontend's `VITE_API_URL` is wired to the backend service URL automatically at Render build time. The Android app is not part of this deployment config — it's built/signed independently via Gradle (`keystore/attendr-release-key.jks` + `keystore.properties`, both gitignored) and distributed separately.

## Project & team context

**Attendr** — GPS-based digital attendance SaaS for Indian SMBs/factories/shops. Built by Nick (Rahul Yadav) + Viraaj, TryBild studio, Mumbai.

### Current auth model (don't assume the old design)
- **Admin**: registers via `companyName + adminEmail + password` → gets a unique `teamId` (e.g. `TRY190`). Login is email + password, **not** OTP.
- **Employee**: registers via `fullName + mobile + teamId` → OTP via SMS → verify → set-password → JWT.
- Routes live under `/api/auth/*`, `/api/attendance/*`, `/api/admin/*`, `/api/reports/*`, `/api/support/*`.
- The **old WhatsApp-OTP and biometric/face-recognition design was an earlier draft and was abandoned** — don't assume it applies. OTP delivery today is Fast2SMS, Quick SMS route (`"q"`), not the DLT route (`"otp"`).

### Product context
- Competitors: greytHR, Keka, SalaryBox, factoHR, HajariBook — all have UX/reliability complaints (OTP failures, bait pricing, crash-after-update, biometric privacy concerns).
- Attendr's differentiation: GPS+OTP only (no biometric/face), simple onboarding, transparent pricing, clean minimal UI (navy `#1B3A7B` brand color).
- Multi-tenant is non-negotiable: every query must scope by `companyId`/`teamId` — never leak data across orgs (see "Multi-tenancy" above).

### Working style
- Self-taught builders, not CS grads. Explain technical concepts in plain language when asked, but default to exact, runnable commands/code rather than oversimplifying.
- Communicate direct and plain, no corporate hedging, no unnecessary disclaimers. Hinglish is fine.
- When debugging: ask for exact terminal output / screenshots rather than guessing; many bugs trace back to mismatched file paths, wrong branches, or stale Render config — read pasted output carefully before responding.
- Give a clear recommendation with reasoning, not a generic "it depends."
- Don't suggest switching frameworks/stacks unless explicitly asked to evaluate alternatives.
- When generating Claude Code prompts for this repo, make them extremely detailed and self-contained (exact file paths, exact behavior, explicit constraints on what NOT to touch).

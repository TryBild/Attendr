# Attendr — GPS-Based Attendance Platform

Built for Everyday India 🇮🇳

## Tech Stack

**Backend**: Node.js 20 · Express 4 · MongoDB (Mongoose 8) · JWT · bcryptjs  
**Frontend**: React 19 · TypeScript · Vite · Tailwind CSS 4 · Zustand · React Query

---

## Quick Start

### Prerequisites
- Node.js 20+
- MongoDB (local or Atlas)

### Backend Setup

```bash
cd backend

# Copy and configure environment
cp .env.example .env
# Edit .env — set MONGO_URI and JWT_SECRET at minimum

npm install
npm run dev      # runs on :4000
```

### Frontend Setup

```bash
cd frontend
npm install
npm run dev      # runs on :5173, proxies /api → localhost:4000
```

---

## Environment Variables

### Backend (`backend/.env`)

| Variable | Description | Required |
|---|---|---|
| `MONGO_URI` | MongoDB connection string | ✅ |
| `JWT_SECRET` | Secret key (min 32 chars) | ✅ |
| `PORT` | Server port (default: 4000) | |
| `FAST2SMS_API_KEY` | SMS OTP delivery (if empty, OTP is logged to console in dev) | |
| `OTP_EXPIRY_MINUTES` | OTP validity (default: 10) | |
| `SMTP_USER` / `SMTP_PASS` | Gmail SMTP for support emails | |
| `SUPPORT_EMAIL` | Where support tickets are sent | |
| `ALLOWED_ORIGIN` | CORS origin (default: `*`) | |

---

## API Routes

All routes under `/api`:

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/otp/request` | Public | Request OTP for employee login |
| POST | `/api/auth/otp/verify` | Public | Verify OTP, receive JWT |
| POST | `/api/auth/admin/login` | Public | Admin email+password login |
| POST | `/api/auth/admin/register` | Public | Register new company |
| POST | `/api/attendance/mark` | Employee | Check in / check out |
| GET | `/api/attendance/today` | Employee | Today's attendance status |
| GET | `/api/attendance/my?month=2026-06` | Employee | Monthly attendance history |
| GET | `/api/admin/dashboard` | Admin | Dashboard stats |
| GET | `/api/admin/attendance/day?date=YYYY-MM-DD` | Admin | Day register |
| POST | `/api/admin/attendance/manual` | Admin | Manual attendance override |
| GET/POST/PUT/DELETE | `/api/admin/employees` | Admin | Employee management |
| GET/POST/PUT/DELETE | `/api/admin/departments` | Admin | Department management |
| GET/POST/PUT/DELETE | `/api/admin/geofences` | Admin | Geofence management |
| GET | `/api/reports/month.csv?month=2026-06` | Admin | Download CSV report |
| GET | `/api/reports/month?month=2026-06` | Admin | JSON report data |
| POST | `/api/support/ticket` | Public | Submit support ticket |

---

## Screens

**Employee Flow**: Welcome → Register (Name + Mobile + Team ID) → OTP Verify → Home (Clock In/Out) → My Attendance → Profile

**Admin Flow**: Login/Register → Dashboard → Day Register → Employees → Departments → Geofences → Month Report

**Help**: Help Center → Article → Contact Support

---

## Deployment

**Backend** (Railway/Render):
- Set `MONGO_URI`, `JWT_SECRET`, `ALLOWED_ORIGIN=https://your-frontend.vercel.app`
- Build command: `npm install`
- Start command: `node src/server.js`

**Frontend** (Vercel):
- Set `VITE_API_URL=https://your-backend.railway.app/api`
- Build command: `npm run build`
- Output: `dist/`

---

## India-Specific Notes

- OTP uses Fast2SMS (set `FAST2SMS_API_KEY`; without it, OTP logs to console in dev)
- All times in IST (`Asia/Kolkata`)
- Mobile validation: Indian numbers starting with 6, 7, 8, or 9
- Dates displayed in Indian format: `8 Jun 2026`
- Team IDs are auto-generated: 3 letters + 3 digits (e.g., `SHA749`)

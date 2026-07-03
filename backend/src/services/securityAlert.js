import nodemailer from "nodemailer";

function createTransporter() {
  return nodemailer.createTransport({
    host: process.env.SMTP_HOST || "smtp.gmail.com",
    port: Number(process.env.SMTP_PORT) || 587,
    secure: false,
    auth: {
      user: process.env.SMTP_USER,
      pass: process.env.SMTP_PASS,
    },
  });
}

function nowIST() {
  return new Date().toLocaleString("en-IN", { timeZone: "Asia/Kolkata" });
}

async function send(to, subject, text, html) {
  if (!to) return; // no verified email on file — nothing to send
  if (!process.env.SMTP_USER || !process.env.SMTP_PASS) {
    console.log(`[DEV] Security email (no SMTP configured) → ${to}: ${subject}`);
    return;
  }
  const transporter = createTransporter();
  await transporter.sendMail({
    from: `"Attendr Security" <${process.env.SMTP_USER}>`,
    to,
    subject,
    text,
    html,
  });
}

export function sendLoginWarningEmail(email, { attempts, ip, userAgent, time = nowIST() }) {
  const subject = "⚠️ Attendr Security Alert — Suspicious Login Attempts";
  const text =
    `Someone tried to log into your Attendr account ${attempts} times with the wrong password.\n\n` +
    `IP Address: ${ip}\n` +
    `Device: ${userAgent}\n` +
    `Time: ${time} IST\n\n` +
    `If this was you, ignore this email.\n` +
    `If not, change your password immediately: https://attendr.com/employee/forgot-password\n\n` +
    `Contact support: support@attendr.com`;
  const html = `
    <p>Someone tried to log into your Attendr account <b>${attempts}</b> times with the wrong password.</p>
    <p><b>IP Address:</b> ${ip}<br/><b>Device:</b> ${userAgent}<br/><b>Time:</b> ${time} IST</p>
    <p>If this was you, ignore this email.<br/>
    If not, <a href="https://attendr.com/employee/forgot-password">change your password immediately</a>.</p>
    <p>Contact support: support@attendr.com</p>`;
  return send(email, subject, text, html).catch((e) => console.error("[SecurityAlert] warning email failed:", e.message));
}

export function sendAccountLockedEmail(email, { ip, userAgent, lockedUntil }) {
  const lockedUntilIST = new Date(lockedUntil).toLocaleString("en-IN", { timeZone: "Asia/Kolkata" });
  const subject = "🔒 Your Attendr Account Has Been Locked";
  const text =
    `Your account has been locked due to too many failed login attempts.\n\n` +
    `IP Address: ${ip}\n` +
    `Locked until: ${lockedUntilIST} IST\n\n` +
    `To unlock early, reset your password: https://attendr.com/employee/forgot-password\n\n` +
    `If you didn't do this, contact us immediately: support@attendr.com`;
  const html = `
    <p>Your account has been locked due to too many failed login attempts.</p>
    <p><b>IP Address:</b> ${ip}<br/><b>Locked until:</b> ${lockedUntilIST} IST</p>
    <p>To unlock early, <a href="https://attendr.com/employee/forgot-password">reset your password</a>.</p>
    <p>If you didn't do this, contact us immediately: support@attendr.com</p>`;
  return send(email, subject, text, html).catch((e) => console.error("[SecurityAlert] lockout email failed:", e.message));
}

export function sendPasswordChangedEmail(email, { ip, time = nowIST() }) {
  const subject = "✅ Attendr Password Changed";
  const text =
    `Your Attendr password was successfully changed.\n\n` +
    `IP: ${ip}\n` +
    `Time: ${time} IST\n\n` +
    `If you made this change, no action needed.\n` +
    `If you did NOT make this change, contact support@attendr.com immediately.`;
  const html = `
    <p>Your Attendr password was successfully changed.</p>
    <p><b>IP:</b> ${ip}<br/><b>Time:</b> ${time} IST</p>
    <p>If you made this change, no action needed.<br/>
    If you did <b>NOT</b> make this change, contact support@attendr.com immediately.</p>`;
  return send(email, subject, text, html).catch((e) => console.error("[SecurityAlert] password-changed email failed:", e.message));
}

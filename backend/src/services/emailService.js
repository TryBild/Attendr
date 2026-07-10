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

async function sendSecurityEmail(to, subject, html) {
  if (!process.env.SMTP_USER || !process.env.SMTP_PASS) {
    console.log(`[DEV] Security email (no SMTP configured): ${subject} → ${to}`);
    return;
  }
  const transporter = createTransporter();
  await transporter.sendMail({
    from:    `"Attendr Security" <${process.env.SMTP_USER}>`,
    to,
    subject,
    html,
  });
}

// account: short label like `admin (x@y.com)` or `employee Ramesh (98XXXXXX01)`
export async function sendLoginWarningEmail(email, { account, attempts, ip, time }) {
  await sendSecurityEmail(
    email,
    "[Attendr] Warning: repeated failed login attempts",
    `
    <h2>Failed login attempts detected</h2>
    <p>There have been <b>${attempts} failed login attempts</b> on the account: <b>${account || email}</b>.</p>
    <table border="1" cellpadding="8" style="border-collapse:collapse">
      <tr><td><b>IP address</b></td><td>${ip || "unknown"}</td></tr>
      <tr><td><b>Time</b></td><td>${time || new Date().toISOString()}</td></tr>
    </table>
    <p>If this was not you or your employee, we recommend changing the password immediately.</p>
    `
  );
}

export async function sendAccountLockedEmail(email, { account, ip, lockedUntil }) {
  await sendSecurityEmail(
    email,
    "[Attendr] Account locked after repeated failed logins",
    `
    <h2>Account locked</h2>
    <p>The account <b>${account || email}</b> has been locked due to repeated failed login attempts.</p>
    <table border="1" cellpadding="8" style="border-collapse:collapse">
      <tr><td><b>IP address</b></td><td>${ip || "unknown"}</td></tr>
      <tr><td><b>Locked until</b></td><td>${lockedUntil || "unknown"}</td></tr>
    </table>
    <p>Login will be possible again after the lock expires. If this was not expected, contact support.</p>
    `
  );
}

export async function sendPasswordChangedEmail(email, { account, ip, time }) {
  await sendSecurityEmail(
    email,
    "[Attendr] Password was changed",
    `
    <h2>Password changed</h2>
    <p>The password for account <b>${account || email}</b> was just changed.</p>
    <table border="1" cellpadding="8" style="border-collapse:collapse">
      <tr><td><b>IP address</b></td><td>${ip || "unknown"}</td></tr>
      <tr><td><b>Time</b></td><td>${time || new Date().toISOString()}</td></tr>
    </table>
    <p>If this was not you or your employee, contact support immediately.</p>
    `
  );
}

// Nudge an org admin/HR that an employee tried to mark attendance but no office
// location (geofence) is configured yet. Triggered by the employee "Notify admin" action.
export async function sendGeofenceNotSetEmail(adminEmail, employeeName, orgName) {
  await sendSecurityEmail(
    adminEmail,
    "Action needed: set your office location on Attendr",
    `
    <h2>Set up your office location</h2>
    <p><b>${employeeName || "An employee"}</b> tried to mark attendance${orgName ? ` for <b>${orgName}</b>` : ""}, but no office location has been set up yet.</p>
    <p>Employees can't check in or out until an admin adds an office location (geofence) in the Attendr admin app.</p>
    <p><b>What to do:</b> open Attendr → Admin → Office Locations, and add your office so your team can start marking attendance.</p>
    `
  );
}

export async function sendSupportTicketEmail(ticket) {
  if (!process.env.SMTP_USER || !process.env.SMTP_PASS) {
    console.log("[DEV] Support ticket (no SMTP configured):", ticket.ticketId);
    return;
  }
  const transporter = createTransporter();
  const html = `
    <h2>New Support Ticket: ${ticket.ticketId}</h2>
    <table border="1" cellpadding="8" style="border-collapse:collapse">
      <tr><td><b>Ticket ID</b></td><td>${ticket.ticketId}</td></tr>
      <tr><td><b>Mobile</b></td><td>${ticket.mobile || "N/A"}</td></tr>
      <tr><td><b>Team ID</b></td><td>${ticket.teamId || "N/A"}</td></tr>
      <tr><td><b>Status</b></td><td>${ticket.status}</td></tr>
      <tr><td><b>Article Slug</b></td><td>${ticket.articleSlug || "N/A"}</td></tr>
      <tr><td><b>Platform</b></td><td>${ticket.deviceInfo?.platform || "N/A"}</td></tr>
      <tr><td><b>User Agent</b></td><td>${ticket.deviceInfo?.userAgent || "N/A"}</td></tr>
      <tr><td><b>Created At</b></td><td>${ticket.createdAt}</td></tr>
    </table>
    <h3>Issue Description</h3>
    <p>${ticket.issueDescription.replace(/\n/g, "<br>")}</p>
    ${ticket.screenshotUrls?.length ? `<h3>Screenshots</h3><p>${ticket.screenshotUrls.length} screenshot(s) attached (base64 omitted in email body)</p>` : ""}
  `;

  await transporter.sendMail({
    from:    `"Attendr Support" <${process.env.SMTP_USER}>`,
    to:      process.env.SUPPORT_EMAIL || process.env.SMTP_USER,
    subject: `[Attendr] Support Ticket ${ticket.ticketId}`,
    html,
  });
}

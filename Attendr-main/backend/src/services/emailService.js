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

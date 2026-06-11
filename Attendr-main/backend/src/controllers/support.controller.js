import { SupportTicket } from "../models/index.js";
import { sendSupportTicketEmail } from "../services/emailService.js";
import { err } from "../utils/response.js";

async function generateTicketId() {
  const year = new Date().getFullYear();
  const count = await SupportTicket.countDocuments({
    ticketId: { $regex: `^ATD-${year}-` },
  });
  const seq = String(count + 1).padStart(4, "0");
  return `ATD-${year}-${seq}`;
}

// POST /api/support/ticket
export async function createTicket(req, res) {
  try {
    const { issueDescription, mobile, teamId, articleSlug, screenshotBase64, deviceInfo } = req.body;
    if (!issueDescription) return err(res, "issueDescription is required", 400);

    const ticketId = await generateTicketId();

    const ticket = await SupportTicket.create({
      ticketId,
      mobile:           mobile?.trim(),
      teamId:           teamId?.toUpperCase()?.trim(),
      issueDescription: issueDescription.trim(),
      screenshotUrls:   Array.isArray(screenshotBase64) ? screenshotBase64 : [],
      deviceInfo:       deviceInfo || {},
      articleSlug,
    });

    // Fire-and-forget email
    sendSupportTicketEmail(ticket).catch((e) => console.error("Email failed:", e.message));

    return res.status(201).json({
      ok: true,
      ticketId,
      message: "Your request has been received. We'll respond within 24 hours.",
    });
  } catch (e) {
    console.error(e);
    err(res, e.message);
  }
}

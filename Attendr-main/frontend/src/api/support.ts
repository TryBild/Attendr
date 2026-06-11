import { request } from "./client";

export const submitSupportTicket = (data: {
  issueDescription: string;
  mobile?: string;
  teamId?: string;
  articleSlug?: string;
  screenshotBase64?: string[];
  deviceInfo?: { platform?: string; userAgent?: string; appVersion?: string };
}) =>
  request<{ ok: true; ticketId: string; message: string }>("/support/ticket", {
    method: "POST",
    body: JSON.stringify(data),
  });

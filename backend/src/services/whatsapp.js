import axios from "axios";

const API_VERSION = "v21.0";

function getClient() {
  const token = process.env.WHATSAPP_CLOUD_API_TOKEN;
  const phoneNumberId = process.env.WHATSAPP_PHONE_NUMBER_ID;
  if (!token || !phoneNumberId) return null;
  return {
    token,
    url: `https://graph.facebook.com/${API_VERSION}/${phoneNumberId}/messages`,
  };
}

async function sendTemplate(to, templateName, parameters) {
  const client = getClient();
  if (!client) {
    console.log(`[DEV] WhatsApp template "${templateName}" → ${to}`, parameters);
    return true;
  }

  const payload = {
    messaging_product: "whatsapp",
    to,
    type: "template",
    template: {
      name: templateName,
      language: { code: "en" },
      components: [
        {
          type: "body",
          parameters: parameters.map((val) => ({
            type: "text",
            text: String(val),
          })),
        },
      ],
    },
  };

  try {
    const res = await axios.post(client.url, payload, {
      headers: {
        Authorization: `Bearer ${client.token}`,
        "Content-Type": "application/json",
      },
      timeout: 15000,
    });
    console.log(`[WhatsApp] Sent "${templateName}" → ${to} (msgId: ${res.data.messages?.[0]?.id})`);
    return true;
  } catch (e) {
    const detail = e.response?.data?.error?.message || e.message;
    console.error(`[WhatsApp] Failed "${templateName}" → ${to}: ${detail}`);
    return false;
  }
}

export async function sendDailyDigest(phoneNumber, { present, absent, late, fakeGps, date }) {
  const templateName = process.env.WHATSAPP_TEMPLATE_NAME_DAILY || "attendr_daily_digest";
  return sendTemplate(phoneNumber, templateName, [date, present, absent, late, fakeGps]);
}

export async function sendWeeklySummary(phoneNumber, { weekStart, weekEnd, avgPresent, totalAbsent, totalLate, totalFakeGps }) {
  const templateName = process.env.WHATSAPP_TEMPLATE_NAME_WEEKLY || "attendr_weekly_summary";
  return sendTemplate(phoneNumber, templateName, [weekStart, weekEnd, avgPresent, totalAbsent, totalLate, totalFakeGps]);
}

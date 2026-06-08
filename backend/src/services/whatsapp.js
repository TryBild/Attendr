const DEV = process.env.OTP_DEV_MODE !== "false";

async function sendWhatsApp(to, body) {
  if (DEV) {
    console.log(`\n[WA DEV] → ${to}\n${body}\n`);
    return { dev: true };
  }
  const url = `https://graph.facebook.com/${process.env.WA_API_VERSION}/${process.env.WA_PHONE_NUMBER_ID}/messages`;
  const res = await fetch(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${process.env.WA_ACCESS_TOKEN}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      messaging_product: "whatsapp",
      to,
      type: "text",
      text: { body },
    }),
  });
  if (!res.ok) throw new Error(`WA send failed: ${res.status}`);
  return res.json();
}

export const sendOtp     = (phone, code) =>
  sendWhatsApp(phone, `Your Attendr OTP is *${code}*. Valid for 5 minutes. Do not share.`);

export const sendDigest  = (phone, text) => sendWhatsApp(phone, text);

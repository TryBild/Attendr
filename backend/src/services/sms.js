const DEV = process.env.OTP_DEV_MODE !== 'false';

function cleanPhone(phone) {
  const digits = String(phone).replace(/\D/g, '');
  if (digits.length === 12 && digits.startsWith('91')) return digits.slice(2);
  if (digits.length === 11 && digits.startsWith('0'))  return digits.slice(1);
  return digits.slice(-10);
}

export async function sendOtpSms(phone, otp) {
  const num = cleanPhone(phone);

  if (DEV) {
    console.log(`\n[SMS DEV] → ${num}  OTP: ${otp}\n`);
    return;
  }

  const apiKey = process.env.FAST2SMS_API_KEY;
  if (!apiKey) throw new Error('FAST2SMS_API_KEY is not set');

  const res = await fetch('https://www.fast2sms.com/dev/bulkV2', {
    method: 'POST',
    headers: {
      authorization: apiKey,
      'content-type': 'application/json',
    },
    body: JSON.stringify({
      route: 'otp',
      variables_values: String(otp),
      numbers: num,
    }),
  });

  const data = await res.json();

  if (data.return === false) {
    throw new Error(`Fast2SMS: ${data.message}`);
  }

  console.log(`[SMS] OTP sent to ${num} — request_id: ${data.request_id}`);
}

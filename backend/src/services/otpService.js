import axios from "axios";

async function sendFast2SMS(mobile, otp) {
  const apiKey = process.env.FAST2SMS_API_KEY;
  if (!apiKey) {
    console.log(`[DEV] OTP for ${mobile}: ${otp}`);
    return true;
  }
  for (let attempt = 0; attempt < 3; attempt++) {
    try {
      const res = await axios.post(
        "https://www.fast2sms.com/dev/bulkV2",
        {
          route:     "otp",
          variables_values: otp,
          numbers:   mobile,
          flash:     0,
        },
        {
          headers: { authorization: apiKey },
          timeout: 8000,
        }
      );
      if (res.data.return === true) return true;
    } catch (e) {
      if (attempt === 2) throw new Error("SMS delivery failed");
      await new Promise((r) => setTimeout(r, 1000));
    }
  }
  return false;
}

export async function sendOTP(mobile, otp) {
  return sendFast2SMS(mobile, otp);
}

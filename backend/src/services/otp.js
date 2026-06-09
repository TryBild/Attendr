import { Otp } from "../models/index.js";
import { OTP_EXPIRY_MINS, OTP_LENGTH } from "../config/constants.js";
import { sendOtpSms } from "./sms.js";

const genCode = () =>
  String(Math.floor(10 ** (OTP_LENGTH - 1) + Math.random() * 9 * 10 ** (OTP_LENGTH - 1)));

export async function createAndSendOtp(phone) {
  const code = genCode();
  const expiresAt = new Date(Date.now() + OTP_EXPIRY_MINS * 60 * 1000);
  await Otp.create({ phone, code, expiresAt });
  try {
    await sendOtpSms(phone, code);
  } catch (e) {
    console.error('[OTP] SMS delivery failed:', e.message);
    throw new Error('Could not send OTP');
  }
  return true;
}

export async function verifyOtp(phone, code) {
  const otp = await Otp.findOne({ phone, code, used: false }).sort({ _id: -1 });
  if (!otp || otp.expiresAt < new Date()) return false;
  otp.used = true;
  await otp.save();
  return true;
}

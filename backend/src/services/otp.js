import { Otp } from "../models/index.js";
import { OTP_EXPIRY_MINS, OTP_LENGTH } from "../config/constants.js";
import { sendOtp } from "./whatsapp.js";

const genCode = () =>
  String(Math.floor(10 ** (OTP_LENGTH - 1) + Math.random() * 9 * 10 ** (OTP_LENGTH - 1)));

export async function createAndSendOtp(phone) {
  const code = genCode();
  const expiresAt = new Date(Date.now() + OTP_EXPIRY_MINS * 60 * 1000);
  await Otp.create({ phone, code, expiresAt });
  await sendOtp(phone, code);
  return true;
}

export async function verifyOtp(phone, code) {
  const otp = await Otp.findOne({ phone, code, used: false }).sort({ _id: -1 });
  if (!otp || otp.expiresAt < new Date()) return false;
  otp.used = true;
  await otp.save();
  return true;
}

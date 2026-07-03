import crypto from "crypto";

// AES-256-GCM at-rest encryption for sensitive fields (GPS coordinates).
// Output format: "iv:authTag:ciphertext" (all hex). Legacy plain values pass through.

let warned = false;

function getKey() {
  const hex = process.env.ENCRYPTION_KEY;
  if (!hex || !/^[0-9a-fA-F]{64}$/.test(hex)) {
    if (!warned) {
      console.warn("[encryption] ENCRYPTION_KEY missing or not 32-byte hex — coordinates will be stored as plain text");
      warned = true;
    }
    return null;
  }
  return Buffer.from(hex, "hex");
}

export function encrypt(text) {
  const key = getKey();
  if (!key) return String(text);
  const iv = crypto.randomBytes(12);
  const cipher = crypto.createCipheriv("aes-256-gcm", key, iv);
  const enc = Buffer.concat([cipher.update(String(text), "utf8"), cipher.final()]);
  return `${iv.toString("hex")}:${cipher.getAuthTag().toString("hex")}:${enc.toString("hex")}`;
}

export function decrypt(str) {
  if (typeof str !== "string" || !str.includes(":")) return str;
  const key = getKey();
  if (!key) return str;
  try {
    const [ivHex, tagHex, dataHex] = str.split(":");
    const decipher = crypto.createDecipheriv("aes-256-gcm", key, Buffer.from(ivHex, "hex"));
    decipher.setAuthTag(Buffer.from(tagHex, "hex"));
    return Buffer.concat([decipher.update(Buffer.from(dataHex, "hex")), decipher.final()]).toString("utf8");
  } catch {
    // wrong key or corrupted value — return as stored rather than crash reads
    return str;
  }
}

import cloudinary from "../config/cloudinary.js";
import { Company, Employee } from "../models/index.js";
import { err } from "../utils/response.js";

function uploadBufferToCloudinary(buffer) {
  return new Promise((resolve, reject) => {
    const stream = cloudinary.uploader.upload_stream(
      {
        folder: "attendr/profile-photos",
        transformation: [{ width: 512, height: 512, crop: "fill", quality: "auto" }],
      },
      (error, result) => (error ? reject(error) : resolve(result))
    );
    stream.end(buffer);
  });
}

// POST /api/auth/profile/photo — self-service photo upload for the authenticated admin or employee
export async function uploadProfilePhoto(req, res) {
  try {
    if (!req.file) return err(res, "No image file provided", 400);

    const result = await uploadBufferToCloudinary(req.file.buffer);

    if (req.auth.kind === "admin") {
      await Company.findByIdAndUpdate(req.auth.id, { photoUrl: result.secure_url });
    } else {
      await Employee.findByIdAndUpdate(req.auth.id, { photoUrl: result.secure_url });
    }

    return res.json({ ok: true, photoUrl: result.secure_url });
  } catch (e) {
    console.error(e);
    err(res, e.message || "Photo upload failed");
  }
}

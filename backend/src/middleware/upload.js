import multer from "multer";

const multerUpload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 5 * 1024 * 1024 },
  fileFilter: (_req, file, cb) => {
    if (!file.mimetype.startsWith("image/")) {
      return cb(new Error("Only image files are allowed"));
    }
    cb(null, true);
  },
});

export function uploadProfilePhotoImage(req, res, next) {
  multerUpload.single("photo")(req, res, (error) => {
    if (error instanceof multer.MulterError) {
      const message = error.code === "LIMIT_FILE_SIZE"
        ? "Image must be under 5MB"
        : error.message;
      return res.status(400).json({ ok: false, error: message });
    }
    if (error) {
      return res.status(400).json({ ok: false, error: error.message });
    }
    next();
  });
}

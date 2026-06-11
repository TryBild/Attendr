export const ok  = (res, data = {}, status = 200) =>
  res.status(status).json({ ok: true,  ...data });

export const err = (res, error = "Something went wrong", status = 500) =>
  res.status(status).json({ ok: false, error });

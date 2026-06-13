import { useAuthStore } from "../store/auth";

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string
  ) {
    super(message);
  }
}

export async function request<T>(
  path: string,
  init: RequestInit = {}
): Promise<T> {
  const token = useAuthStore.getState().token;

  const base = (import.meta.env.VITE_API_URL as string | undefined) ?? "";
  const res = await fetch(`${base}/api${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...init.headers,
    },
  });

  if (res.status === 401) {
    useAuthStore.getState().clear();
    window.location.href = "/register";
    throw new ApiError(401, "Session expired");
  }

  const body = await res.json();

  if (!body.ok) {
    throw new ApiError(res.status, body.error ?? "Unknown error");
  }

  return body as T;
}

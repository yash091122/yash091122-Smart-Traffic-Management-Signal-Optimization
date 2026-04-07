const TOKEN_KEY = "st_token";

export function getStoredToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(TOKEN_KEY);
}

export function setStoredToken(token: string | null): void {
  if (typeof window === "undefined") return;
  if (token) localStorage.setItem(TOKEN_KEY, token);
  else localStorage.removeItem(TOKEN_KEY);
}

export function authHeaders(): HeadersInit {
  const t = getStoredToken();
  return {
    "Content-Type": "application/json",
    ...(t ? { Authorization: `Bearer ${t}` } : {}),
  };
}

export async function apiPost<T>(
  path: string,
  body: unknown,
): Promise<{ ok: boolean; data: T | string }> {
  const base =
    process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, "") || "http://localhost:8080";
  const res = await fetch(`${base}${path}`, {
    method: "POST",
    headers: authHeaders(),
    body: JSON.stringify(body),
  });
  const text = await res.text();
  try {
    return { ok: res.ok, data: JSON.parse(text) as T };
  } catch {
    return { ok: res.ok, data: text };
  }
}

export async function apiGetText(path: string): Promise<{ ok: boolean; text: string }> {
  const base =
    process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, "") || "http://localhost:8080";
  const res = await fetch(`${base}${path}`, { headers: authHeaders() });
  const text = await res.text();
  return { ok: res.ok, text };
}

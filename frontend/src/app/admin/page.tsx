"use client";

import { getIntersections } from "@/lib/api";
import { apiPost, getStoredToken, setStoredToken } from "@/lib/authFetch";
import type { IntersectionRow } from "@/types/traffic";
import { ArrowLeft, Loader2 } from "lucide-react";
import Link from "next/link";
import { useEffect, useState } from "react";

const base = () =>
  process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, "") || "http://localhost:8080";

type LoginResponse = { token: string; username: string; primaryRole: string };

export default function AdminPage() {
  const [token, setToken] = useState<string | null>(null);
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("adminpass");
  const [busy, setBusy] = useState(false);
  const [msg, setMsg] = useState<string | null>(null);
  const [intersections, setIntersections] = useState<IntersectionRow[]>([]);
  const [setCountId, setSetCountId] = useState<number | "">("");
  const [setCountVal, setSetCountVal] = useState(12);
  const [routeFrom, setRouteFrom] = useState<number | "">("");
  const [routeTo, setRouteTo] = useState<number | "">("");
  const [routeResult, setRouteResult] = useState<string | null>(null);
  const [emergencyIds, setEmergencyIds] = useState("");
  const [autoSim, setAutoSim] = useState(false);
  const [lastSim, setLastSim] = useState<string | null>(null);

  useEffect(() => {
    setToken(getStoredToken());
  }, []);

  useEffect(() => {
    (async () => {
      try {
        const list = await getIntersections();
        setIntersections(list);
        if (list[0]) {
          setSetCountId((v) => (v === "" ? list[0].id : v));
          setRouteFrom((v) => (v === "" ? list[0].id : v));
        }
        if (list[1]) setRouteTo((v) => (v === "" ? list[1].id : v));
      } catch {
        setMsg("Could not load intersections — is the API running on port 8080?");
      }
    })();
  }, []);

  async function login(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setMsg(null);
    const { ok, data } = await apiPost<LoginResponse>("/api/auth/login", {
      username,
      password,
    });
    setBusy(false);
    if (ok && data && typeof data === "object" && "token" in data) {
      setStoredToken(data.token);
      setToken(data.token);
      setMsg(`Signed in as ${data.username} (${data.primaryRole})`);
    } else {
      setMsg(typeof data === "string" ? data : "Login failed");
    }
  }

  function logout() {
    setStoredToken(null);
    setToken(null);
    setMsg("Logged out");
  }

  async function runSim(body: Record<string, unknown> = {}) {
    setBusy(true);
    setMsg(null);
    const { ok, data } = await apiPost<Record<string, unknown>>(
      "/api/simulation/run",
      body,
    );
    setBusy(false);
    if (ok && data && typeof data === "object") {
      setLastSim(JSON.stringify(data, null, 2));
      if ("autoSimulate" in data) setAutoSim(Boolean(data.autoSimulate));
      setMsg("Simulation request OK");
    } else {
      setMsg(
        typeof data === "string"
          ? data
          : "Simulation failed — sign in as ADMIN (admin / adminpass)",
      );
    }
  }

  async function pushSetCount() {
    if (setCountId === "") return;
    setBusy(true);
    setMsg(null);
    const { ok, data } = await apiPost("/api/simulation/set-count", {
      intersectionId: setCountId,
      vehicleCount: setCountVal,
    });
    setBusy(false);
    if (ok) setMsg("Vehicle count updated");
    else
      setMsg(
        typeof data === "string"
          ? data
          : "set-count failed — ADMIN token required",
      );
  }

  async function activateEmergency() {
    const ids = emergencyIds
      .split(/[\s,]+/)
      .map((s) => Number(s.trim()))
      .filter((n) => Number.isFinite(n));
    if (ids.length === 0) {
      setMsg("Enter one or more intersection IDs (comma-separated)");
      return;
    }
    setBusy(true);
    setMsg(null);
    const { ok, data } = await apiPost("/api/emergency/activate", {
      routeIntersectionIds: ids,
    });
    setBusy(false);
    if (ok) setMsg(`Emergency activated (event id: ${(data as { id?: number })?.id ?? "ok"})`);
    else
      setMsg(
        typeof data === "string"
          ? data
          : "Emergency activate failed — ADMIN token required",
      );
  }

  async function optimizeRoute() {
    if (routeFrom === "" || routeTo === "") return;
    setBusy(true);
    setRouteResult(null);
    setMsg(null);
    try {
      const res = await fetch(
        `${base()}/api/route/optimize?from=${routeFrom}&to=${routeTo}`,
      );
      const text = await res.text();
      setBusy(false);
      if (!res.ok) {
        setRouteResult(text);
        setMsg("Route request failed");
        return;
      }
      try {
        setRouteResult(JSON.stringify(JSON.parse(text), null, 2));
      } catch {
        setRouteResult(text);
      }
      setMsg("Route computed");
    } catch {
      setBusy(false);
      setMsg("Network error calling route API");
    }
  }

  const authed = Boolean(token);

  return (
    <div className="min-h-screen bg-black px-4 py-10 text-slate-100">
      <div className="mx-auto max-w-2xl space-y-6">
        <div className="flex items-center justify-between gap-4">
          <Link
            href="/"
            className="inline-flex items-center gap-2 text-sm text-emerald-400 hover:text-emerald-300"
          >
            <ArrowLeft className="h-4 w-4" />
            Dashboard
          </Link>
          {authed ? (
            <button
              type="button"
              onClick={logout}
              className="rounded-lg border border-white/15 px-3 py-1.5 text-sm hover:bg-white/5"
            >
              Log out
            </button>
          ) : null}
        </div>

        <header>
          <h1 className="text-2xl font-semibold tracking-tight text-white">
            Admin console
          </h1>
          <p className="mt-1 text-sm text-slate-400">
            Sign in with the seeded ADMIN user, then run simulation, emergencies,
            and vehicle overrides against the Spring API.
          </p>
        </header>

        {msg ? (
          <p className="rounded-xl border border-white/10 bg-slate-900/50 px-4 py-3 text-sm text-slate-300">
            {msg}
          </p>
        ) : null}

        <section className="glass-panel space-y-4 p-6">
          <h2 className="text-sm font-semibold uppercase tracking-wider text-slate-400">
            Authentication
          </h2>
          {!authed ? (
            <form onSubmit={login} className="space-y-3">
              <div>
                <label className="block text-xs text-slate-500">Username</label>
                <input
                  className="mt-1 w-full rounded-lg border border-white/[0.08] bg-black/60 px-3 py-2 text-sm outline-none focus:border-emerald-500/50"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  autoComplete="username"
                />
              </div>
              <div>
                <label className="block text-xs text-slate-500">Password</label>
                <input
                  type="password"
                  className="mt-1 w-full rounded-lg border border-white/[0.08] bg-black/60 px-3 py-2 text-sm outline-none focus:border-emerald-500/50"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  autoComplete="current-password"
                />
              </div>
              <p className="text-xs text-slate-500">
                Default seed: <code className="text-slate-400">admin</code> /{" "}
                <code className="text-slate-400">adminpass</code>
              </p>
              <button
                type="submit"
                disabled={busy}
                className="flex w-full items-center justify-center gap-2 rounded-xl bg-emerald-600 py-2.5 text-sm font-medium text-white hover:bg-emerald-500 disabled:opacity-50"
              >
                {busy ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
                Sign in
              </button>
            </form>
          ) : (
            <p className="text-sm text-emerald-400/90">
              JWT stored in localStorage — admin API calls are authorized.
            </p>
          )}
        </section>

        <section className="glass-panel space-y-4 p-6">
          <h2 className="text-sm font-semibold uppercase tracking-wider text-slate-400">
            Simulation
          </h2>
          <div className="flex flex-wrap gap-2">
            <button
              type="button"
              disabled={busy || !authed}
              onClick={() => runSim({})}
              className="rounded-lg border border-white/15 bg-white/5 px-4 py-2 text-sm hover:bg-white/10 disabled:opacity-40"
            >
              Run one tick
            </button>
            <button
              type="button"
              disabled={busy || !authed}
              onClick={() => runSim({ auto: !autoSim })}
              className="rounded-lg border border-white/15 bg-white/5 px-4 py-2 text-sm hover:bg-white/10 disabled:opacity-40"
            >
              Toggle auto + tick
            </button>
          </div>
          <p className="text-xs text-slate-500">
            Auto mode after toggle:{" "}
            <span className="text-slate-300">{autoSim ? "on" : "off"}</span>
            {" · "}
            POST <code className="text-slate-400">/api/simulation/run</code>
          </p>
          {lastSim ? (
            <pre className="max-h-40 overflow-auto rounded-lg border border-white/10 bg-black/30 p-3 text-xs text-slate-400">
              {lastSim}
            </pre>
          ) : null}
        </section>

        <section className="glass-panel space-y-4 p-6">
          <h2 className="text-sm font-semibold uppercase tracking-wider text-slate-400">
            Set vehicle count
          </h2>
          <div className="flex flex-wrap items-end gap-3">
            <div>
              <label className="block text-xs text-slate-500">Intersection</label>
              <select
                className="mt-1 min-w-[200px] rounded-lg border border-white/10 bg-slate-950/60 px-3 py-2 text-sm"
                value={setCountId === "" ? "" : String(setCountId)}
                onChange={(e) =>
                  setSetCountId(e.target.value ? Number(e.target.value) : "")
                }
              >
                {intersections.length === 0 ? (
                  <option value="">No intersections loaded</option>
                ) : null}
                {intersections.map((i) => (
                  <option key={i.id} value={i.id}>
                    {i.name} (#{i.id})
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-xs text-slate-500">Vehicles</label>
              <input
                type="number"
                min={0}
                className="mt-1 w-28 rounded-lg border border-white/10 bg-slate-950/60 px-3 py-2 text-sm"
                value={setCountVal}
                onChange={(e) => setSetCountVal(Number(e.target.value) || 0)}
              />
            </div>
            <button
              type="button"
              disabled={busy || !authed || setCountId === ""}
              onClick={pushSetCount}
              className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium hover:bg-emerald-500 disabled:opacity-40"
            >
              Apply
            </button>
          </div>
        </section>

        <section className="glass-panel space-y-4 p-6">
          <h2 className="text-sm font-semibold uppercase tracking-wider text-slate-400">
            Emergency corridor
          </h2>
          <input
            className="w-full rounded-lg border border-white/10 bg-slate-950/60 px-3 py-2 text-sm"
            placeholder="e.g. 1, 2, 3"
            value={emergencyIds}
            onChange={(e) => setEmergencyIds(e.target.value)}
          />
          <button
            type="button"
            disabled={busy || !authed}
            onClick={activateEmergency}
            className="rounded-lg border border-amber-500/40 bg-amber-500/10 px-4 py-2 text-sm text-amber-200 hover:bg-amber-500/20 disabled:opacity-40"
          >
            POST /api/emergency/activate
          </button>
        </section>

        <section className="glass-panel space-y-4 p-6">
          <h2 className="text-sm font-semibold uppercase tracking-wider text-slate-400">
            Route optimize (public GET)
          </h2>
          <div className="flex flex-wrap items-end gap-3">
            <div>
              <label className="block text-xs text-slate-500">From</label>
              <select
                className="mt-1 min-w-[160px] rounded-lg border border-white/10 bg-slate-950/60 px-3 py-2 text-sm"
                value={routeFrom === "" ? "" : String(routeFrom)}
                onChange={(e) =>
                  setRouteFrom(e.target.value ? Number(e.target.value) : "")
                }
              >
                {intersections.length === 0 ? (
                  <option value="">—</option>
                ) : null}
                {intersections.map((i) => (
                  <option key={i.id} value={i.id}>
                    #{i.id} {i.name}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-xs text-slate-500">To</label>
              <select
                className="mt-1 min-w-[160px] rounded-lg border border-white/10 bg-slate-950/60 px-3 py-2 text-sm"
                value={routeTo === "" ? "" : String(routeTo)}
                onChange={(e) =>
                  setRouteTo(e.target.value ? Number(e.target.value) : "")
                }
              >
                {intersections.length === 0 ? (
                  <option value="">—</option>
                ) : null}
                {intersections.map((i) => (
                  <option key={i.id} value={i.id}>
                    #{i.id} {i.name}
                  </option>
                ))}
              </select>
            </div>
            <button
              type="button"
              disabled={busy || routeFrom === "" || routeTo === ""}
              onClick={optimizeRoute}
              className="rounded-lg border border-white/15 bg-white/5 px-4 py-2 text-sm hover:bg-white/10 disabled:opacity-40"
            >
              Compute path
            </button>
          </div>
          {routeResult ? (
            <pre className="max-h-48 overflow-auto rounded-lg border border-white/10 bg-black/30 p-3 text-xs text-slate-400">
              {routeResult}
            </pre>
          ) : null}
        </section>
      </div>
    </div>
  );
}

"use client";

import {
  getCongestionSeries,
  getLiveSignals,
  getPeakHours,
  getTopCongested,
  wsBase,
} from "@/lib/api";
import type {
  AppNotification,
  DashboardStats,
  LiveSignal,
  TopCongested,
} from "@/types/traffic";
import {
  Activity,
  Bell,
  Car,
  ChevronDown,
  Maximize2,
  Minimize2,
  RefreshCw,
  Search,
  Settings,
  Video,
  Waves,
  X,
} from "lucide-react";
import dynamic from "next/dynamic";
import Image from "next/image";
import Link from "next/link";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import {
  Area,
  AreaChart,
  CartesianGrid,
  Cell,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { Client, IMessage } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const TrafficMap = dynamic(
  () =>
    import("@/components/TrafficMap").then((m) => ({ default: m.TrafficMap })),
  { ssr: false, loading: () => <MapSkeleton /> },
);

function MapSkeleton() {
  return (
    <div className="flex h-full min-h-[420px] items-center justify-center rounded-xl bg-slate-950/60 text-sm text-slate-500">
      Loading live map…
    </div>
  );
}

const COLORS = ["#10b981", "#6366f1", "#a3a3a3"];

/** Fixed locale so Node (SSR) and the browser emit the same strings — avoids clock hydration errors. */
const DASH_LOCALE = "en-US";

function formatDashClockTime(d: Date): string {
  return d.toLocaleTimeString(DASH_LOCALE, {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });
}

function formatDashClockDate(d: Date): string {
  return d.toLocaleDateString(DASH_LOCALE, {
    weekday: "short",
    month: "short",
    day: "numeric",
    year: "numeric",
  });
}

function deriveDonut(signals: LiveSignal[]) {
  if (signals.length === 0) {
    return [
      { name: "Cars", value: 73 },
      { name: "Trucks", value: 18 },
      { name: "Bikes", value: 9 },
    ];
  }
  let heavy = 0;
  let mid = 0;
  let light = 0;
  for (const s of signals) {
    if (s.vehicleCount >= 25) heavy += s.vehicleCount;
    else if (s.vehicleCount >= 10) mid += s.vehicleCount;
    else light += Math.max(1, s.vehicleCount);
  }
  const t = heavy + mid + light;
  if (t === 0) {
    return [
      { name: "Cars", value: 73 },
      { name: "Trucks", value: 18 },
      { name: "Bikes", value: 9 },
    ];
  }
  return [
    { name: "Heavy lanes", value: Math.round((heavy / t) * 100) },
    { name: "Moderate", value: Math.round((mid / t) * 100) },
    { name: "Light", value: Math.max(0, 100 - Math.round((heavy / t) * 100) - Math.round((mid / t) * 100)) },
  ];
}

export function DashboardView() {
  const [signals, setSignals] = useState<LiveSignal[]>([]);
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [topCongested, setTopCongested] = useState<TopCongested[]>([]);
  const [series, setSeries] = useState<{ t: string; v: number }[]>([]);
  const [peakRows, setPeakRows] = useState<{ hour: number; eventCount: number }[]>([]);
  const [now, setNow] = useState(() => new Date());
  const [searchQuery, setSearchQuery] = useState("");
  const [searchOpen, setSearchOpen] = useState(false);
  const [selectedIntersectionId, setSelectedIntersectionId] = useState<number | null>(null);
  const [mapFlyNonce, setMapFlyNonce] = useState(0);
  const [mapZoomPct, setMapZoomPct] = useState(100);
  const [alertsExpanded, setAlertsExpanded] = useState(true);
  const [notifOpen, setNotifOpen] = useState(false);
  const [notifications, setNotifications] = useState<AppNotification[]>([]);
  const [apiOk, setApiOk] = useState<boolean | null>(null);
  const [wsConnected, setWsConnected] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const mapSectionRef = useRef<HTMLElement>(null);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const searchRef = useRef<HTMLDivElement>(null);

  const refreshStatic = useCallback(async () => {
    setRefreshing(true);
    try {
      const [live, top, cong, peaks] = await Promise.all([
        getLiveSignals(),
        getTopCongested(5),
        getCongestionSeries(),
        getPeakHours().catch(() => []),
      ]);
      setSignals(live);
      setTopCongested(top);
      setPeakRows(peaks);
      setApiOk(true);
      const pts = cong.map((b, i) => ({
        t: `H${i}`,
        v: Math.round(b.averageVehicleCount * 10) / 10,
      }));
      setSeries(
        pts.length > 0
          ? pts
          : [
              { t: "Jan", v: 12 },
              { t: "Feb", v: 18 },
              { t: "Mar", v: 15 },
              { t: "Apr", v: 22 },
              { t: "May", v: 28 },
              { t: "Jun", v: 24 },
            ],
      );
    } catch {
      setApiOk(false);
    } finally {
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    refreshStatic();
  }, [refreshStatic]);

  useEffect(() => {
    const t = setInterval(() => setNow(new Date()), 1000);
    return () => clearInterval(t);
  }, []);

  useEffect(() => {
    const onFs = () => setIsFullscreen(!!document.fullscreenElement);
    document.addEventListener("fullscreenchange", onFs);
    return () => document.removeEventListener("fullscreenchange", onFs);
  }, []);

  useEffect(() => {
    const onDoc = (e: MouseEvent) => {
      if (searchRef.current && !searchRef.current.contains(e.target as Node)) {
        setSearchOpen(false);
      }
    };
    document.addEventListener("mousedown", onDoc);
    return () => document.removeEventListener("mousedown", onDoc);
  }, []);

  const pushNotification = useCallback(
    (title: string, body: string) => {
      const n: AppNotification = {
        id: `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`,
        title,
        body,
        time: formatDashClockTime(new Date()),
        read: false,
      };
      setNotifications((prev) => [n, ...prev].slice(0, 25));
    },
    [],
  );

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS(`${wsBase()}/ws`) as unknown as WebSocket,
      reconnectDelay: 5000,
      debug: () => {},
      onConnect: () => {
        setWsConnected(true);
        client.subscribe("/topic/signals", (msg: IMessage) => {
          try {
            setSignals(JSON.parse(msg.body) as LiveSignal[]);
          } catch {
            /* ignore */
          }
        });
        client.subscribe("/topic/stats", (msg: IMessage) => {
          try {
            setStats(JSON.parse(msg.body) as DashboardStats);
          } catch {
            /* ignore */
          }
        });
        client.subscribe("/topic/emergency", (msg: IMessage) => {
          try {
            const raw = JSON.parse(msg.body) as Record<string, unknown>;
            const ids = Array.isArray(raw.intersectionIds)
              ? (raw.intersectionIds as number[]).join(", ")
              : "";
            pushNotification(
              "Emergency corridor",
              `${String(raw.message ?? "Active")}${ids ? ` · Nodes: ${ids}` : ""}`,
            );
          } catch {
            pushNotification("Emergency", msg.body || "Event received");
          }
        });
      },
      onDisconnect: () => setWsConnected(false),
      onWebSocketClose: () => setWsConnected(false),
    });
    client.activate();
    return () => {
      void client.deactivate();
    };
  }, [pushNotification]);

  const handleSelectIntersection = useCallback((id: number) => {
    setSelectedIntersectionId(id);
    setMapFlyNonce((n) => n + 1);
  }, []);

  const onZoomPercent = useCallback((pct: number) => {
    setMapZoomPct(pct);
  }, []);

  const displaySignals = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    if (!q) return signals;
    return signals.filter(
      (s) =>
        s.name.toLowerCase().includes(q) ||
        String(s.id).includes(q) ||
        `intersection ${s.id}`.includes(q),
    );
  }, [signals, searchQuery]);

  const searchSuggestions = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    if (q.length < 1) return [];
    return signals
      .filter(
        (s) =>
          s.name.toLowerCase().includes(q) || String(s.id).includes(q),
      )
      .slice(0, 8);
  }, [signals, searchQuery]);

  const activeVehicles = useMemo(() => {
    const sum = signals.reduce((a, s) => a + s.vehicleCount, 0);
    return sum > 0 ? sum.toLocaleString(DASH_LOCALE) : "—";
  }, [signals]);

  const congestionPct = useMemo(() => {
    if (signals.length === 0) return 0;
    const avg =
      signals.reduce((a, s) => a + s.vehicleCount, 0) / signals.length;
    return Math.min(100, Math.round((avg / 80) * 100));
  }, [signals]);

  const donutData = useMemo(() => deriveDonut(signals), [signals]);
  const donutCenter = donutData[0]?.value ?? 73;

  const { peakBarPct, currentHourBarPct } = useMemo(() => {
    if (peakRows.length === 0) {
      return { peakBarPct: 78, currentHourBarPct: congestionPct };
    }
    const maxC = Math.max(1, ...peakRows.map((p) => p.eventCount));
    const morning = peakRows
      .filter((p) => p.hour >= 7 && p.hour <= 9)
      .reduce((a, p) => a + p.eventCount, 0);
    const peakBarPct = Math.min(100, Math.round((morning / maxC) * 100));
    const hr = now.getHours();
    const thisHour = peakRows.find((p) => p.hour === hr)?.eventCount ?? 0;
    const currentHourBarPct = Math.min(100, Math.round((thisHour / maxC) * 100));
    return { peakBarPct, currentHourBarPct };
  }, [peakRows, now, congestionPct]);

  const alerts = useMemo(() => {
    const top = topCongested[0];
    const top2 = topCongested[1];
    const low = signals.filter((s) => s.vehicleCount < 8);
    return [
      {
        tone: "red" as const,
        title: "Heavy congestion",
        sub: top?.name ?? "Awaiting analytics data",
        time: "Live",
        intersectionId: top?.intersectionId,
      },
      {
        tone: "emerald" as const,
        title: "Secondary hotspot",
        sub: top2?.name ?? "Monitoring corridors",
        time: "Live",
        intersectionId: top2?.intersectionId,
      },
      {
        tone: "amber" as const,
        title: "Light traffic zones",
        sub:
          low[0]?.name ??
          (signals.length ? "All segments observed" : "No live data"),
        time: "Live",
        intersectionId: low[0]?.id,
      },
    ];
  }, [topCongested, signals]);

  const unreadCount = notifications.filter((n) => !n.read).length;

  const toggleFullscreen = () => {
    const el = mapSectionRef.current;
    if (!el) return;
    if (!document.fullscreenElement) {
      void el.requestFullscreen();
    } else {
      void document.exitFullscreen();
    }
  };

  const markAllRead = () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
  };

  const dismissNotification = (id: string) => {
    setNotifications((prev) => prev.filter((n) => n.id !== id));
  };

  return (
    <div className="relative min-h-screen overflow-hidden">
      <div className="pointer-events-none fixed inset-0 z-0">
        <Image
          src="https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=2400&q=70"
          alt=""
          fill
          className="object-cover object-center"
          priority
          sizes="100vw"
        />
        <div className="absolute inset-0 bg-gradient-to-br from-black/95 via-neutral-950/92 to-black/94" />
        <div
          className="absolute inset-0 opacity-25"
          style={{
            backgroundSize: "48px 48px",
            backgroundImage:
              "linear-gradient(rgba(255,255,255,0.05) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.05) 1px, transparent 1px)",
          }}
        />
      </div>

      <div className="relative z-10 mx-auto flex min-h-screen max-w-[1920px] flex-col gap-4 p-4 md:p-6 lg:gap-5 lg:p-8">
        <header className="glass-panel-strong relative z-50 flex flex-col gap-4 px-5 py-4 md:flex-row md:items-center md:justify-between">
          <div className="flex items-center gap-3">
            <div className="flex h-11 w-11 items-center justify-center rounded-full bg-gradient-to-br from-emerald-500 to-emerald-400 shadow-lg shadow-emerald-500/20">
              <Activity className="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 className="text-lg font-semibold tracking-tight text-white md:text-xl">
                TrafficPulse AI
              </h1>
              <p className="text-xs text-neutral-500">Central Dashboard</p>
            </div>
          </div>

          <div ref={searchRef} className="relative mx-auto w-full max-w-xl md:mx-0">
            <Search className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />
            <input
              type="search"
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value);
                setSearchOpen(true);
              }}
              onFocus={() => setSearchOpen(true)}
              placeholder="Search road, location, camera…"
              className="w-full rounded-full border border-white/[0.08] bg-white/5 py-2.5 pl-11 pr-4 text-sm text-neutral-200 placeholder:text-neutral-500 backdrop-blur-md transition focus:border-emerald-500/40 focus:outline-none focus:ring-2 focus:ring-emerald-500/20"
            />
            {searchOpen && searchQuery.trim().length > 0 && (
              <ul className="absolute left-0 right-0 top-full z-[9999] mt-2 max-h-64 overflow-auto rounded-xl border border-white/[0.08] bg-black/95 py-1 shadow-xl backdrop-blur-xl">
                {searchSuggestions.length > 0 ? searchSuggestions.map((s) => (
                  <li key={s.id}>
                    <button
                      type="button"
                      className="flex w-full flex-col gap-0.5 px-4 py-2.5 text-left text-sm hover:bg-white/10"
                      onClick={() => {
                        handleSelectIntersection(s.id);
                        setSearchQuery("");
                        setSearchOpen(false);
                      }}
                    >
                      <span className="font-medium text-white">{s.name}</span>
                      <span className="text-xs text-neutral-500">
                        ID {s.id} · {s.vehicleCount} vehicles · {s.phase}
                      </span>
                    </button>
                  </li>
                )) : (
                  <li className="px-4 py-3 text-sm text-neutral-500">No intersections matching &ldquo;{searchQuery.trim()}&rdquo;</li>
                )}
              </ul>
            )}
          </div>

          <div className="flex flex-wrap items-center justify-end gap-2 md:gap-3">
            <div
              className="flex items-center gap-2 rounded-lg border border-white/10 bg-white/5 px-2 py-1 text-[10px] text-slate-400"
              title="API and live feed status"
            >
              <span
                className={`h-2 w-2 rounded-full ${apiOk ? "bg-emerald-500" : "bg-red-500"}`}
              />
              API
              <span
                className={`h-2 w-2 rounded-full ${wsConnected ? "bg-emerald-500" : "bg-amber-500"}`}
              />
              WS
            </div>
            <button
              type="button"
              onClick={() => refreshStatic()}
              disabled={refreshing}
              className="rounded-xl border border-white/10 bg-white/5 p-2.5 text-slate-300 backdrop-blur-md transition hover:bg-white/10 disabled:opacity-50"
              title="Refresh data"
            >
              <RefreshCw className={`h-5 w-5 ${refreshing ? "animate-spin" : ""}`} />
            </button>
            <Link
              href="/admin"
              className="rounded-xl border border-white/10 bg-white/5 p-2.5 text-slate-300 backdrop-blur-md transition hover:bg-white/10"
              title="Admin console"
            >
              <Settings className="h-5 w-5" />
            </Link>
            <div className="text-right text-xs text-slate-400">
              <div className="font-medium text-slate-200">
                {formatDashClockTime(now)}
              </div>
              <div>{formatDashClockDate(now)}</div>
            </div>
            <div className="relative">
              <button
                type="button"
                onClick={() => setNotifOpen((o) => !o)}
                className="relative rounded-xl border border-white/10 bg-white/5 p-2.5 text-slate-300 backdrop-blur-md transition hover:bg-white/10"
                aria-expanded={notifOpen}
                aria-label="Notifications"
              >
                <Bell className="h-5 w-5" />
                {unreadCount > 0 && (
                  <span className="absolute -right-0.5 -top-0.5 flex h-4 min-w-4 items-center justify-center rounded-full bg-red-500 px-1 text-[10px] font-bold text-white">
                    {unreadCount > 9 ? "9+" : unreadCount}
                  </span>
                )}
              </button>
              {notifOpen && (
                <div className="absolute right-0 top-full z-[9999] mt-2 w-80 max-h-96 overflow-auto rounded-xl border border-white/[0.08] bg-black/95 p-2 shadow-xl backdrop-blur-xl">
                  <div className="mb-2 flex items-center justify-between gap-2 px-2">
                    <span className="text-xs font-semibold text-white">
                      Notifications
                    </span>
                    <div className="flex items-center gap-1">
                      {unreadCount > 0 && (
                        <button
                          type="button"
                          onClick={markAllRead}
                          className="rounded px-2 py-1 text-[10px] text-emerald-400 hover:bg-white/10"
                        >
                          Mark read
                        </button>
                      )}
                      <button
                        type="button"
                        onClick={() => setNotifOpen(false)}
                        className="rounded p-1 text-slate-400 hover:bg-white/10 hover:text-white"
                      >
                        <X className="h-4 w-4" />
                      </button>
                    </div>
                  </div>
                  {notifications.length === 0 ? (
                    <p className="px-2 py-6 text-center text-xs text-slate-500">
                      No notifications yet. Emergency events and system alerts
                      appear here.
                    </p>
                  ) : (
                    <ul className="flex flex-col gap-1">
                      {notifications.map((n) => (
                        <li
                          key={n.id}
                          className="group relative rounded-lg border border-white/5 bg-white/5 p-3 pr-8"
                        >
                          <button
                            type="button"
                            onClick={() => dismissNotification(n.id)}
                            className="absolute right-1 top-1 rounded p-1 text-slate-500 opacity-0 transition hover:bg-white/10 hover:text-white group-hover:opacity-100"
                            aria-label="Dismiss"
                          >
                            <X className="h-3.5 w-3.5" />
                          </button>
                          <p className="text-sm font-medium text-slate-100">
                            {n.title}
                          </p>
                          <p className="text-xs text-slate-400">{n.body}</p>
                          <p className="mt-1 text-[10px] text-slate-600">
                            {n.time}
                          </p>
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
              )}
            </div>
            <div className="flex items-center gap-3 rounded-xl border border-white/10 bg-white/5 py-1.5 pl-1.5 pr-4 backdrop-blur-md">
              <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-gradient-to-br from-neutral-700 to-neutral-800 text-xs font-bold text-white">
                JR
              </div>
              <div className="text-left text-xs">
                <div className="font-medium text-slate-200">James Roy</div>
                <div className="text-slate-500">Admin</div>
              </div>
            </div>
          </div>
        </header>

        <div className="grid flex-1 grid-cols-1 gap-4 lg:grid-cols-[minmax(260px,1fr)_minmax(0,3fr)_minmax(280px,1fr)] lg:gap-5">
          <aside className="flex flex-col gap-4">
            <div className="grid grid-cols-2 gap-3">
              <div className="glass-panel p-4">
                <div className="mb-2 flex items-center justify-between text-slate-400">
                  <span className="text-xs font-medium uppercase tracking-wide">
                    Active vehicles
                  </span>
                  <Car className="h-4 w-4 text-emerald-400" />
                </div>
                <p className="text-2xl font-bold tabular-nums text-white">
                  {activeVehicles}
                </p>
              </div>
              <div className="glass-panel p-4">
                <div className="mb-2 flex items-center justify-between text-slate-400">
                  <span className="text-xs font-medium uppercase tracking-wide">
                    Congestion
                  </span>
                  <Waves className="h-4 w-4 text-neutral-400" />
                </div>
                <p className="text-2xl font-bold tabular-nums text-white">
                  {congestionPct}%
                </p>
              </div>
            </div>

            <div className="glass-panel flex flex-col gap-3 p-4">
              <button
                type="button"
                onClick={() => setAlertsExpanded((e) => !e)}
                className="flex w-full items-center justify-between text-left"
              >
                <h2 className="text-sm font-semibold text-white">
                  Recent alerts
                </h2>
                <ChevronDown
                  className={`h-4 w-4 text-slate-500 transition ${alertsExpanded ? "rotate-180" : ""}`}
                />
              </button>
              {alertsExpanded && (
                <ul className="flex flex-col gap-2">
                  {alerts.map((a) => (
                    <li key={a.title}>
                      <button
                        type="button"
                        onClick={() =>
                          a.intersectionId != null &&
                          handleSelectIntersection(a.intersectionId)
                        }
                        className="flex w-full gap-3 rounded-xl border border-white/5 bg-white/5 p-3 text-left backdrop-blur-sm transition hover:border-emerald-500/30 hover:bg-white/10"
                      >
                        <span
                          className={`mt-1 h-2 w-2 shrink-0 rounded-full ${
                            a.tone === "red"
                              ? "bg-red-500 shadow-[0_0_8px_rgba(239,68,68,0.8)]"
                              : a.tone === "emerald"
                                ? "bg-emerald-400 shadow-[0_0_8px_rgba(52,211,153,0.7)]"
                                : "bg-amber-400"
                          }`}
                        />
                        <div className="min-w-0 flex-1">
                          <p className="text-sm font-medium text-slate-100">
                            {a.title}
                          </p>
                          <p className="truncate text-xs text-slate-500">
                            {a.sub}
                          </p>
                          <p className="text-[11px] text-emerald-500/80">
                            {a.time} · Click to focus map
                          </p>
                        </div>
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </div>

            <div className="glass-panel p-4">
              <h2 className="mb-3 text-sm font-semibold text-white">
                Traffic flow
              </h2>
              <div className="mb-3">
                <div className="mb-1 flex justify-between text-xs text-slate-400">
                  <span>Peak hours (7–9 AM)</span>
                  <span>{peakBarPct}%</span>
                </div>
                <div className="h-2 overflow-hidden rounded-full bg-slate-800">
                  <div
                    className="h-full rounded-full bg-gradient-to-r from-amber-500 to-amber-400 transition-all duration-500"
                    style={{ width: `${peakBarPct}%` }}
                  />
                </div>
              </div>
              <div>
                <div className="mb-1 flex justify-between text-xs text-slate-400">
                  <span>Current hour load</span>
                  <span>{currentHourBarPct}%</span>
                </div>
                <div className="h-2 overflow-hidden rounded-full bg-slate-800">
                  <div
                    className="h-full rounded-full bg-gradient-to-r from-emerald-500 to-emerald-400 transition-all duration-500"
                    style={{
                      width: `${currentHourBarPct > 0 ? currentHourBarPct : congestionPct}%`,
                    }}
                  />
                </div>
              </div>
            </div>
          </aside>

          <section
            ref={mapSectionRef}
            className="glass-panel-strong flex min-h-[480px] flex-col overflow-hidden p-1 md:min-h-0"
          >
            <div className="flex items-start justify-between gap-2 px-4 pb-2 pt-3">
              <div>
                <h2 className="text-sm font-semibold text-white md:text-base">
                  Live city traffic map
                </h2>
                <p className="text-xs text-slate-400">
                  {searchQuery.trim()
                    ? `Showing ${displaySignals.length} match(es) for “${searchQuery.trim()}”`
                    : "Real-time monitoring & signal optimization"}
                </p>
              </div>
              <div className="flex items-center gap-2 rounded-full border border-white/10 bg-black/30 px-2 py-1 backdrop-blur-md">
                <span className="px-2 text-xs tabular-nums text-slate-400">
                  {mapZoomPct}%
                </span>
                <button
                  type="button"
                  onClick={toggleFullscreen}
                  className="rounded-lg p-1.5 text-slate-300 hover:bg-white/10"
                  aria-label={isFullscreen ? "Exit fullscreen" : "Fullscreen map"}
                >
                  {isFullscreen ? (
                    <Minimize2 className="h-4 w-4" />
                  ) : (
                    <Maximize2 className="h-4 w-4" />
                  )}
                </button>
              </div>
            </div>
            <div className="relative min-h-[420px] flex-1 px-2 pb-2">
              <TrafficMap
                signals={displaySignals}
                selectedIntersectionId={selectedIntersectionId}
                mapFlyNonce={mapFlyNonce}
                onSelectIntersection={handleSelectIntersection}
                onZoomPercent={onZoomPercent}
              />
              <div className="pointer-events-none absolute bottom-6 left-6 z-[500] rounded-xl border border-white/10 bg-black/70 px-3 py-2 text-[10px] text-slate-300 backdrop-blur-md">
                <p className="mb-1 font-semibold text-white">Traffic status</p>
                <div className="flex flex-col gap-1">
                  <span className="flex items-center gap-2">
                    <span className="h-2 w-2 rounded-full bg-emerald-500" />{" "}
                    Normal
                  </span>
                  <span className="flex items-center gap-2">
                    <span className="h-2 w-2 rounded-full bg-amber-400" />{" "}
                    Moderate
                  </span>
                  <span className="flex items-center gap-2">
                    <span className="h-2 w-2 rounded-full bg-red-500" />{" "}
                    Congested
                  </span>
                </div>
              </div>
            </div>
          </section>

          <aside className="flex flex-col gap-4">
            <div className="glass-panel p-4">
              <h2 className="mb-3 text-sm font-semibold text-white">
                Traffic analytics
              </h2>
              <div className="h-[180px] w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={series}>
                    <defs>
                      <linearGradient id="fill" x1="0" y1="0" x2="0" y2="1">
                        <stop
                          offset="0%"
                          stopColor="#10b981"
                          stopOpacity={0.5}
                        />
                        <stop
                          offset="100%"
                          stopColor="#10b981"
                          stopOpacity={0}
                        />
                      </linearGradient>
                    </defs>
                    <CartesianGrid
                      strokeDasharray="3 3"
                      stroke="rgba(255,255,255,0.06)"
                    />
                    <XAxis
                      dataKey="t"
                      tick={{ fill: "#64748b", fontSize: 10 }}
                      axisLine={false}
                      tickLine={false}
                    />
                    <YAxis
                      tick={{ fill: "#64748b", fontSize: 10 }}
                      axisLine={false}
                      tickLine={false}
                    />
                    <Tooltip
                      contentStyle={{
                        background: "rgba(0,0,0,0.95)",
                        border: "1px solid rgba(255,255,255,0.1)",
                        borderRadius: "8px",
                        fontSize: "12px",
                      }}
                    />
                    <Area
                      type="monotone"
                      dataKey="v"
                      stroke="#10b981"
                      strokeWidth={2}
                      fill="url(#fill)"
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </div>

            <div className="glass-panel p-4">
              <h2 className="mb-2 text-sm font-semibold text-white">
                Vehicle mix (by lane load)
              </h2>
              <div className="flex items-center gap-4">
                <div className="relative h-[140px] w-[140px] shrink-0">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={donutData}
                        cx="50%"
                        cy="50%"
                        innerRadius={48}
                        outerRadius={62}
                        paddingAngle={2}
                        dataKey="value"
                      >
                        {donutData.map((_, i) => (
                          <Cell key={i} fill={COLORS[i % COLORS.length]} />
                        ))}
                      </Pie>
                    </PieChart>
                  </ResponsiveContainer>
                  <div className="pointer-events-none absolute inset-0 flex items-center justify-center">
                    <div className="rounded-full bg-slate-900/90 px-3 py-2 text-center shadow-inner ring-1 ring-white/10">
                      <p className="text-lg font-bold text-white">
                        {donutCenter}%
                      </p>
                      <p className="text-[9px] uppercase text-slate-500">
                        Top share
                      </p>
                    </div>
                  </div>
                </div>
                <ul className="flex flex-col gap-2 text-xs">
                  {donutData.map((d, i) => (
                    <li key={d.name} className="flex items-center gap-2">
                      <span
                        className="h-2 w-2 rounded-full"
                        style={{ background: COLORS[i % COLORS.length] }}
                      />
                      <span className="text-slate-400">{d.name}</span>
                      <span className="ml-auto font-medium text-slate-200">
                        {d.value}%
                      </span>
                    </li>
                  ))}
                </ul>
              </div>
            </div>

            <div className="glass-panel overflow-hidden p-0">
              <div className="flex items-center justify-between border-b border-white/10 px-4 py-3">
                <h2 className="text-sm font-semibold text-white">
                  Live camera
                </h2>
                <Video className="h-4 w-4 text-slate-500" />
              </div>
              <div className="relative aspect-video bg-slate-950/80">
                <Image
                  src="https://images.unsplash.com/photo-1449824913935-59a10b8d2000?auto=format&fit=crop&w=800&q=60"
                  alt="Camera preview"
                  fill
                  className="object-cover opacity-80"
                  sizes="400px"
                />
                <span className="absolute left-3 top-3 rounded bg-red-600 px-2 py-0.5 text-[10px] font-bold uppercase tracking-wide text-white">
                  Live
                </span>
                <p className="absolute bottom-2 left-3 text-[11px] text-white drop-shadow-md">
                  Main Street Cam — Downtown
                </p>
              </div>
            </div>
          </aside>
        </div>
      </div>
    </div>
  );
}

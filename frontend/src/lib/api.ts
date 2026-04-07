import type {
  CongestionBucket,
  IntersectionRow,
  LiveSignal,
  PeakHourRow,
  TopCongested,
} from "@/types/traffic";

const base = () =>
  process.env.NEXT_PUBLIC_API_URL?.replace(/\/$/, "") || "http://localhost:8080";

async function getJson<T>(path: string): Promise<T> {
  const res = await fetch(`${base()}${path}`, { cache: "no-store" });
  if (!res.ok) throw new Error(`${path} ${res.status}`);
  return res.json() as Promise<T>;
}

export function getLiveSignals(): Promise<LiveSignal[]> {
  return getJson<LiveSignal[]>("/api/signals/live");
}

export function getIntersections(): Promise<IntersectionRow[]> {
  return getJson<IntersectionRow[]>("/api/intersections");
}

export function getTopCongested(limit = 5): Promise<TopCongested[]> {
  return getJson<TopCongested[]>(`/api/analytics/top-congested?limit=${limit}`);
}

export function getCongestionSeries(): Promise<CongestionBucket[]> {
  return getJson<CongestionBucket[]>("/api/analytics/congestion?hours=24");
}

export function getPeakHours(): Promise<PeakHourRow[]> {
  return getJson<PeakHourRow[]>("/api/analytics/peak-hours");
}

export function wsBase(): string {
  return base();
}

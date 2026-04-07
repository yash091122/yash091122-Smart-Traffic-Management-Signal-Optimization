"use client";

import type { LiveSignal, SignalPhase } from "@/types/traffic";
import { useEffect, useMemo, useRef } from "react";
import L from "leaflet";
import {
  CircleMarker,
  MapContainer,
  Popup,
  TileLayer,
  ZoomControl,
  useMap,
  useMapEvents,
} from "react-leaflet";
import "leaflet/dist/leaflet.css";

function phaseColor(phase: SignalPhase): string {
  switch (phase) {
    case "GREEN":
      return "#22c55e";
    case "YELLOW":
      return "#eab308";
    default:
      return "#ef4444";
  }
}

/** Fit all markers once when data first arrives (avoid resetting view on every WebSocket tick). */
function InitialFitBounds({ signals }: { signals: LiveSignal[] }) {
  const map = useMap();
  const done = useRef(false);
  useEffect(() => {
    if (signals.length === 0 || done.current) return;
    done.current = true;
    const b = L.latLngBounds(
      signals.map((s) => [s.latitude, s.longitude] as [number, number]),
    );
    map.fitBounds(b, { padding: [48, 48], maxZoom: 15 });
  }, [map, signals]);
  return null;
}

function FlyToSelection({
  targetId,
  flyNonce,
  signals,
}: {
  targetId: number | null;
  flyNonce: number;
  signals: LiveSignal[];
}) {
  const map = useMap();
  const signalsRef = useRef(signals);
  signalsRef.current = signals;
  useEffect(() => {
    if (targetId == null) return;
    const s = signalsRef.current.find((x) => x.id === targetId);
    if (!s) return;
    map.flyTo([s.latitude, s.longitude], 16, { duration: 0.75 });
  }, [targetId, flyNonce, map]);
  return null;
}

function ZoomPctReporter({
  onZoomPercent,
}: {
  onZoomPercent: (pct: number) => void;
}) {
  const map = useMap();
  const onZoomPercentRef = useRef(onZoomPercent);
  onZoomPercentRef.current = onZoomPercent;
  const report = () => {
    const z = map.getZoom();
    const pct = Math.round(((z - 10) / 8) * 100);
    onZoomPercentRef.current(Math.min(100, Math.max(35, pct)));
  };
  useMapEvents({
    zoomend: report,
    moveend: report,
  });
  useEffect(() => {
    report();
    // eslint-disable-next-line react-hooks/exhaustive-deps -- one-time initial zoom % after map mounts
  }, [map]);
  return null;
}

export type TrafficMapProps = {
  signals: LiveSignal[];
  selectedIntersectionId: number | null;
  /** Increment when the same intersection should be flown to again (e.g. search pick). */
  mapFlyNonce: number;
  onSelectIntersection: (id: number) => void;
  onZoomPercent: (pct: number) => void;
};

export function TrafficMap({
  signals,
  selectedIntersectionId,
  mapFlyNonce,
  onSelectIntersection,
  onZoomPercent,
}: TrafficMapProps) {
  const center = useMemo((): [number, number] => {
    if (signals.length === 0) return [28.6300, 77.2200];
    const lat = signals.reduce((a, s) => a + s.latitude, 0) / signals.length;
    const lng = signals.reduce((a, s) => a + s.longitude, 0) / signals.length;
    return [lat, lng];
  }, [signals]);

  return (
    <MapContainer
      center={center}
      zoom={14}
      className="h-full min-h-[420px] w-full rounded-xl"
      scrollWheelZoom
      zoomControl={false}
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OSM</a>'
        url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
      />
      <ZoomControl position="topright" />
      <InitialFitBounds signals={signals} />
      <FlyToSelection
        targetId={selectedIntersectionId}
        flyNonce={mapFlyNonce}
        signals={signals}
      />
      <ZoomPctReporter onZoomPercent={onZoomPercent} />
      {signals.map((s) => {
        const r =
          8 +
          Math.min(s.vehicleCount / 3, 28) +
          (selectedIntersectionId === s.id ? 6 : 0);
        const selected = selectedIntersectionId === s.id;
        return (
          <CircleMarker
            key={s.id}
            center={[s.latitude, s.longitude]}
            radius={r}
            pathOptions={{
              color: selected ? "#10b981" : "#0a0a0a",
              weight: selected ? 3 : 2,
              fillColor: phaseColor(s.phase),
              fillOpacity: selected ? 1 : 0.9,
            }}
            eventHandlers={{
              click: () => onSelectIntersection(s.id),
            }}
          >
            <Popup className="text-slate-800">
              <div className="min-w-[160px]">
                <p className="font-semibold">{s.name}</p>
                <p className="text-sm">
                  Phase: <span className="font-medium">{s.phase}</span>
                </p>
                <p className="text-sm">Vehicles: {s.vehicleCount}</p>
                <p className="text-xs text-slate-600">
                  Green / Red: {s.greenDuration}s / {s.redDuration}s
                </p>
                <button
                  type="button"
                  className="mt-2 w-full rounded-md bg-slate-800 px-2 py-1 text-xs text-white hover:bg-slate-700"
                  onClick={() => onSelectIntersection(s.id)}
                >
                  Focus on map
                </button>
              </div>
            </Popup>
          </CircleMarker>
        );
      })}
    </MapContainer>
  );
}

export type SignalPhase = "RED" | "GREEN" | "YELLOW";

/** Raw intersection from GET /api/intersections (Spring entity JSON). */
export interface IntersectionRow {
  id: number;
  name: string;
  latitude: number;
  longitude: number;
  laneCount: number;
  currentPhase: SignalPhase;
  greenDuration: number;
  redDuration: number;
  vehicleCount: number;
  mainRoad: boolean;
}

export interface LiveSignal {
  id: number;
  name: string;
  latitude: number;
  longitude: number;
  phase: SignalPhase;
  vehicleCount: number;
  greenDuration: number;
  redDuration: number;
  laneCount: number;
}

export interface DashboardStats {
  totalIntersections: number;
  averageGreenSeconds: number;
  activeEmergencies: number;
}

export interface TopCongested {
  intersectionId: number;
  name: string;
  averageVehicleCount: number;
  congestionScore: number;
}

export interface CongestionBucket {
  fromEpochMs: number;
  averageVehicleCount: number;
  sampleCount: number;
}

export interface PeakHourRow {
  hour: number;
  eventCount: number;
}

export interface AppNotification {
  id: string;
  title: string;
  body: string;
  time: string;
  read: boolean;
}

# 🚦 TrafficPulse AI — Smart Traffic Management System

> **Real-time, AI-driven traffic management and signal optimization platform for Delhi NCR**
> Live monitoring · Adaptive signals · Emergency corridors · Route optimization · Rich analytics

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Next.js](https://img.shields.io/badge/Next.js-15-000000?style=flat-square&logo=nextdotjs&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?style=flat-square&logo=react&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=flat-square&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=flat-square&logo=docker&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-010101?style=flat-square&logo=socketdotio&logoColor=white)

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Default Credentials](#-default-credentials)
- [API Reference](#-api-reference)
- [WebSocket Topics](#-websocket-topics)
- [Signal Optimization Algorithm](#-signal-optimization-algorithm)
- [Route Optimization](#-route-optimization-dijkstra)
- [Emergency Corridor System](#-emergency-corridor-system)
- [Traffic Simulation Engine](#-traffic-simulation-engine)
- [Dashboard UI](#-dashboard-ui)
- [Admin Console](#-admin-console)
- [Configuration Reference](#-configuration-reference)
- [Database Schema](#-database-schema)
- [Seed Data — Delhi NCR Intersections](#-seed-data--delhi-ncr-intersections)

---

## 🌐 Overview

**TrafficPulse AI** is a full-stack smart traffic management system that simulates, monitors, and optimizes traffic signals across **16 real-world intersections** in the Delhi NCR region.

The platform combines a **Spring Boot 3** backend with a **Next.js 15** frontend to deliver a real-time operational command dashboard for traffic authorities. The system continuously adapts signal timing based on live vehicle counts, provides shortest-path route optimization using Dijkstra's algorithm weighted by signal wait times, and supports one-click **emergency corridor activation** that forces green lights along an entire route for emergency vehicles.

---

## ✨ Key Features

### 🔴🟡🟢 Adaptive Signal Optimization
- Automatically adjusts green/red durations every **10 seconds** based on real-time vehicle counts
- Configurable thresholds: base green time, bonus seconds per vehicle batch, min/max bounds
- Post-violation recalibration with automatic bonus green extensions

### 📡 Real-Time Live Feed (WebSocket)
- **STOMP over SockJS** pushes signal state and dashboard stats to all clients every 5 seconds
- Live vehicle counts, signal phases (RED / YELLOW / GREEN), and duration values
- API + WebSocket connection status indicators on the dashboard header

### 🚑 Emergency Corridor Activation
- Instantly flips all intersections along a route to **GREEN** for a configurable hold duration (default: 60s)
- Broadcasts emergency events to all connected dashboards via WebSocket
- Auto-deactivation when the hold period expires

### 🗺️ Interactive Traffic Map
- Leaflet-based map centered on Delhi NCR with all 16 seeded intersections
- Color-coded markers — 🟢 Normal · 🟡 Moderate · 🔴 Congested
- Click-to-inspect popups with live signal details, vehicle counts, and phase info
- Global search with autocomplete and map fly-to, plus fullscreen mode

### 📊 Rich Analytics Dashboard
- 24-hour congestion trend area chart (via Recharts)
- Top 5 most congested intersections with congestion scores
- Peak-hour heatmap analysis (hourly event distribution over 30 days)
- Average wait time estimations per intersection (7-day rolling window)
- Vehicle mix donut chart (heavy / moderate / light lane classification)

### 🧭 Route Optimization
- **Dijkstra's shortest-path** on the road-segment graph
- Edge weights combine base travel time + estimated signal wait (phase, queue length)
- Returns the optimal intersection ID path via REST API

### 🔐 JWT Authentication & RBAC
- Role-based access: **ADMIN** (full control) and **VIEWER** (read-only)
- JWT tokens with configurable expiration (default: 24 hours)
- Spring Security filter chain with per-endpoint authorization

### 🔄 Traffic Simulation Engine
- Time-aware simulation with **morning peak** (7–9 AM) and **evening peak** (5–8 PM) patterns
- Main roads receive higher simulated traffic load during peak hours
- Manual vehicle count override per intersection via API

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT (Browser)                         │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │       Next.js 15 · React 19 · Tailwind CSS 3            │   │
│  │       Leaflet Map · Recharts · STOMP WebSocket Client    │   │
│  └──────────────┬──────────────────────────┬───────────────┘   │
│                 │ REST (fetch/JWT)          │ WebSocket STOMP   │
└─────────────────┼──────────────────────────┼───────────────────┘
                  │                          │
┌─────────────────┼──────────────────────────┼───────────────────┐
│               BACKEND  (Spring Boot 3.2.5)                      │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  REST Controllers                                        │   │
│  │  Auth · Intersection · Signal · Analytics · Emergency    │   │
│  │  Route · Simulation · Dashboard                          │   │
│  ├─────────────────────────────────────────────────────────┤   │
│  │  Service Layer                                           │   │
│  │  SignalOptimizationService  ·  EmergencyService          │   │
│  │  RouteOptimizationService   ·  TrafficSimulatorService   │   │
│  │  AnalyticsService  ·  WebSocketBroadcastService          │   │
│  │  SignalPhaseScheduler  ·  TrafficLogService              │   │
│  ├─────────────────────────────────────────────────────────┤   │
│  │  Signal State  →  Redis (prod)  or  In-Memory (dev)     │   │
│  ├─────────────────────────────────────────────────────────┤   │
│  │  Spring Security · JWT Filter Chain                      │   │
│  ├──────────────────────┬──────────────────────────────────┤   │
│  │  JPA / Hibernate     │                                   │   │
│  └──────────────────────┼───────────────────────────────────┘  │
│              ┌──────────┴──────────┐  ┌───────────────┐        │
│              │  MySQL 8  /  H2     │  │  Redis 7      │        │
│              └─────────────────────┘  └───────────────┘        │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Backend Framework | Spring Boot | 3.2.5 |
| Language | Java | 17 |
| ORM | Hibernate / Spring Data JPA | — |
| Database (Production) | MySQL | 8.0 |
| Database (Development) | H2 in-memory (MySQL mode) | — |
| Cache / Signal State | Redis | 7 Alpine |
| Real-Time | WebSocket — STOMP + SockJS | — |
| Authentication | JWT via jjwt | 0.12.5 |
| Frontend Framework | Next.js | 15.2.4 |
| UI Library | React | 19 |
| Styling | Tailwind CSS | 3.4.16 |
| Charts | Recharts | 2.15.0 |
| Maps | Leaflet + React-Leaflet | 1.9.4 / 5.0.0-rc.2 |
| Icons | Lucide React | 0.469.0 |
| Build Tool | Maven Wrapper | included |
| Containerization | Docker Compose | — |
| Code Generation | Lombok | 1.18.34 |

---

## 📁 Project Structure

```
Smart Traffic/
├── docker-compose.yml                   # MySQL 8 + Redis 7 infrastructure
├── pom.xml                              # Maven build config
├── mvnw / mvnw.cmd                      # Maven wrapper scripts
│
├── src/main/java/com/smarttraffic/
│   ├── SmartTrafficApplication.java     # Spring Boot entry point
│   │
│   ├── bootstrap/
│   │   └── DataInitializer.java         # Seeds 16 Delhi intersections + users
│   │
│   ├── config/
│   │   ├── SecurityConfig.java          # Filter chain, CORS, CSRF
│   │   ├── WebSocketConfig.java         # STOMP broker (/ws endpoint)
│   │   ├── WebMvcConfig.java            # MVC & CORS config
│   │   ├── JwtProperties.java           # JWT secret & expiration
│   │   ├── SignalProperties.java        # Signal timing parameters
│   │   └── EmergencyProperties.java     # Emergency hold duration
│   │
│   ├── controller/
│   │   ├── AuthController.java          # POST /api/auth/login
│   │   ├── IntersectionController.java  # CRUD + manual phase override
│   │   ├── SignalController.java        # GET /api/signals/live
│   │   ├── AnalyticsController.java     # Wait times, peak hours, congestion
│   │   ├── EmergencyController.java     # POST /api/emergency/activate
│   │   ├── RouteController.java         # GET /api/route/optimize
│   │   ├── SimulationController.java    # Vehicle count & tick control
│   │   └── DashboardController.java     # Thymeleaf-served pages
│   │
│   ├── dto/
│   │   ├── LoginRequest / LoginResponse
│   │   ├── LiveSignalDto                # Real-time signal snapshot
│   │   ├── IntersectionRequest          # Create/update payload
│   │   └── PhaseOverrideRequest         # Manual phase switch
│   │
│   ├── entity/
│   │   ├── Intersection.java            # Core intersection entity
│   │   ├── RoadSegment.java             # Weighted directed edge
│   │   ├── TrafficLog.java              # Historical traffic entry
│   │   ├── EmergencyEvent.java          # Emergency corridor event
│   │   ├── User.java / Role.java        # Auth user + role
│   │   ├── SignalPhase.java             # RED · YELLOW · GREEN enum
│   │   └── SavedRoute.java              # Persisted optimized routes
│   │
│   ├── repository/                      # Spring Data JPA repositories
│   │   └── (Intersection, RoadSegment, TrafficLog, Emergency, User, Role)
│   │
│   ├── security/
│   │   ├── JwtService.java              # Token generation & validation
│   │   ├── JwtAuthFilter.java           # Per-request JWT filter
│   │   └── CustomUserDetailsService.java
│   │
│   └── service/
│       ├── SignalStateService.java            # Interface
│       ├── RedisSignalStateService.java       # Redis-backed state
│       ├── InMemorySignalStateService.java    # ConcurrentHashMap fallback
│       ├── SignalOptimizationService.java     # Adaptive timing calculator
│       ├── SignalPhaseScheduler.java          # RED→GREEN→YELLOW cycle
│       ├── EmergencyService.java              # Corridor activation
│       ├── RouteOptimizationService.java      # Dijkstra shortest path
│       ├── TrafficSimulatorService.java       # Time-aware simulator
│       ├── TrafficLogService.java             # History persistence
│       ├── AnalyticsService.java              # Aggregation & reporting
│       ├── SimulationScheduler.java           # Periodic sim ticks
│       └── WebSocketBroadcastService.java     # Scheduled WS broadcasts
│
├── src/main/resources/
│   ├── application.yml                  # Production (MySQL + Redis)
│   └── application-local.yml            # Dev (H2 + no Redis)
│
└── frontend/
    ├── package.json
    ├── next.config.ts
    ├── tailwind.config.ts
    └── src/
        ├── app/
        │   ├── layout.tsx               # Root layout — Inter font
        │   ├── page.tsx                 # Home → DashboardView
        │   ├── globals.css              # Glassmorphism styles
        │   └── admin/page.tsx           # Admin console
        ├── components/
        │   ├── TrafficMap.tsx           # Leaflet interactive map
        │   └── dashboard/DashboardView.tsx  # Main dashboard (877 lines)
        ├── hooks/useTrafficFeed.ts      # WebSocket hook
        ├── lib/
        │   ├── api.ts                   # REST API client
        │   └── authFetch.ts             # JWT-aware fetch wrapper
        └── types/traffic.ts             # TypeScript type definitions
```

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Version |
|---|---|
| Java JDK | 17+ |
| Node.js | 18+ |
| npm | 9+ |
| Docker *(optional)* | 20+ |

---

### Option A — Docker (Recommended)

Spins up MySQL 8 and Redis 7 with zero configuration.

```bash
# 1. Start infrastructure (MySQL + Redis)
docker compose up -d

# 2. Start the Spring Boot backend
./mvnw spring-boot:run

# Backend available at: http://localhost:8080
```

> On first boot, `DataInitializer` automatically seeds 16 intersections, road segments, and default users.

---

### Option B — Local Development (H2 In-Memory)

Run without Docker — uses an embedded H2 database and skips Redis entirely.

```bash
# Start backend with the 'local' profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

H2 Console available at `http://localhost:8080/h2-console`

```
JDBC URL : jdbc:h2:mem:smarttraffic
Username : sa
Password : (leave empty)
```

---

### Frontend Setup

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Create environment config
cp .env.local.example .env.local

# Start the development server (Turbopack)
npm run dev

# Open: http://localhost:3000
```

**`.env.local` contents:**

```env
# Spring Boot API base URL
NEXT_PUBLIC_API_URL=http://localhost:8080
```

---

## 🔑 Default Credentials

| Username | Password | Role | Permissions |
|---|---|---|---|
| `admin` | `adminpass` | **ADMIN** | Full CRUD, emergency activation, simulation control |
| `viewer` | `viewerpass` | **VIEWER** | Read-only dashboard and analytics |

> **Login endpoint:** `POST /api/auth/login`

---

## 📡 API Reference

All protected endpoints require the `Authorization: Bearer <token>` header.

### Authentication

| Method | Endpoint | Auth |
|---|---|---|
| `POST` | `/api/auth/login` | Public |

```json
// Request
{ "username": "admin", "password": "adminpass" }

// Response
{ "token": "eyJhbG...", "username": "admin", "role": "ADMIN" }
```

---

### Intersections

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/intersections` | List all intersections |
| `GET` | `/api/intersections/{id}` | Get single intersection |
| `POST` | `/api/intersections` | Create intersection |
| `PUT` | `/api/intersections/{id}` | Update intersection |
| `DELETE` | `/api/intersections/{id}` | Delete intersection |
| `POST` | `/api/intersections/{id}/override-phase` | Lock phase for 120 seconds |

**Phase Override body:**
```json
{ "phase": "GREEN" }
```

---

### Live Signals

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/signals/live` | Get real-time state for all intersections |

**Response per signal:**
```json
{
  "id": 1,
  "name": "ITO Crossing",
  "phase": "GREEN",
  "greenDuration": 35,
  "redDuration": 30,
  "vehicleCount": 42,
  "latitude": 28.6280,
  "longitude": 77.2410
}
```

---

### Analytics

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/analytics/wait-times` | Avg wait per intersection (7-day window) |
| `GET` | `/api/analytics/peak-hours` | Hourly event distribution (30 days) |
| `GET` | `/api/analytics/congestion?hours=24` | Congestion trend over time |
| `GET` | `/api/analytics/top-congested?limit=5` | Top N congested intersections |

---

### Route Optimization

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/route/optimize?from={id}&to={id}` | Dijkstra optimal path |

```json
// Response
{
  "from": 1,
  "to": 12,
  "path": [1, 3, 11, 12],
  "reachable": true
}
```

---

### Emergency Corridor

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/emergency/activate` | Force GREEN along a route for 60s |

```json
{ "routeIntersectionIds": [1, 2, 4, 8] }
```

---

### Simulation Control

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/simulation/set-count` | Manually set vehicle count |
| `POST` | `/api/simulation/run` | Trigger tick / toggle auto-simulate |

```json
// Set count
{ "intersectionId": 1, "vehicleCount": 85 }

// Toggle auto-simulate
{ "auto": false }
```

---

## 🔌 WebSocket Topics

Connect to: `ws://localhost:8080/ws` (SockJS fallback enabled)

| Topic | Interval | Payload Description |
|---|---|---|
| `/topic/signals` | Every 5s | `LiveSignalDto[]` — all intersection signal states |
| `/topic/stats` | Every 5s | `{ totalIntersections, averageGreenSeconds, activeEmergencies }` |
| `/topic/emergency` | On event | `{ message, intersectionIds[], endsAt }` |

**TypeScript client example:**

```typescript
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const client = new Client({
  webSocketFactory: () => new SockJS("http://localhost:8080/ws"),
  reconnectDelay: 5000,
  onConnect: () => {
    client.subscribe("/topic/signals", (msg) => {
      const signals = JSON.parse(msg.body);
      // update UI state...
    });

    client.subscribe("/topic/emergency", (msg) => {
      const event = JSON.parse(msg.body);
      console.log("Emergency corridor active:", event);
    });
  },
});

client.activate();
```

---

## 🧠 Signal Optimization Algorithm

`SignalOptimizationService` runs every **10 seconds** on a Spring `@Scheduled` task:

```
greenDuration = baseGreen + (vehicleCount / threshold) × bonusPerThreshold + recalibrationBonus
greenDuration = clamp(minGreen, maxGreen, greenDuration)

if vehicleCount < threshold / 2  →  redDuration = max(15, defaultRed - 5)   # sparse traffic
if vehicleCount > threshold × 3  →  redDuration = min(60, defaultRed + 10)  # heavy cross-traffic
else                             →  redDuration = defaultRed
```

**Default Parameters:**

| Config Key | Value | Description |
|---|---|---|
| `app.signal.base-green-seconds` | 25s | Baseline green duration |
| `app.signal.bonus-seconds-per-threshold` | 5s | Extra green per vehicle batch |
| `app.signal.vehicle-threshold` | 10 | Vehicles per bonus unit |
| `app.signal.min-green-seconds` | 15s | Hard lower bound |
| `app.signal.max-green-seconds` | 90s | Hard upper bound |
| `app.signal.default-red-seconds` | 30s | Baseline red duration |

---

## 🧭 Route Optimization (Dijkstra)

`RouteOptimizationService` runs Dijkstra's algorithm on a directed graph of road segments:

**Edge weight formula:**

```
weight(edge) = baseTravelSeconds + estimatedWaitAt(targetIntersection)

estimatedWaitAt(intersection):
  GREEN  → 2s  (can proceed immediately)
  YELLOW → 4s  (clearing phase)
  RED    → redDuration × 0.45  (expected partial wait)
  + vehicleCount × 0.35   (queue delay per vehicle)
```

The algorithm dynamically favors routes through **green signals** and **low queue lengths**, producing traffic-aware shortest paths in real time.

---

## 🚑 Emergency Corridor System

When `POST /api/emergency/activate` is called with a list of intersection IDs:

1. All currently active emergencies are **deactivated**
2. A new `EmergencyEvent` is persisted with the route + expiry timestamp
3. Every intersection in the route is **locked to GREEN** for the hold duration
4. A WebSocket broadcast notifies all connected dashboards instantly
5. Expired events are auto-deactivated on the next check cycle

**Default hold duration:** 60 seconds (`app.emergency.hold-seconds`)

---

## 🔄 Traffic Simulation Engine

`TrafficSimulatorService` generates realistic vehicle counts based on time of day:

| Time Window | Main Roads | Side Roads |
|---|---|---|
| **Morning Peak** (7–9 AM) | Base + 15–45 extra vehicles | max(0, base − 10) |
| **Evening Peak** (5–8 PM) | Base + 10–40 extra vehicles | Base + 5–25 extra |
| **Off-Peak** | Base + (−3 to +8) variance | Base + (−3 to +8) variance |

- Base vehicle range: **2–25** (random per tick)
- Counts clamped to **0–200** range automatically
- Manual override range: **0–500** via API

---

## 🎨 Dashboard UI

The dashboard uses a **dark glassmorphism** design with an emerald accent palette.

| Panel | Content |
|---|---|
| **Header** | Logo · Global search with autocomplete · API/WS status · Live clock · Notifications · Admin link |
| **Left Sidebar** | Active vehicle total · Congestion % · Recent alerts (click-to-focus) · Peak/current hour flow bars |
| **Center** | Interactive Leaflet map · Color-coded markers · Fullscreen toggle · Zoom indicator · Traffic legend |
| **Right Sidebar** | 24h congestion area chart · Vehicle mix donut chart · Top 5 congested intersections leaderboard |

**Notable UI behaviours:**
- Search dropdown shows live intersection matches and flies the map to the selection
- Notification panel receives emergency broadcasts pushed via WebSocket in real time
- Alerts sidebar highlights the top congested intersection and lets operators click to focus
- Responsive 3-column grid that collapses gracefully on mobile screens

---

## ⚙️ Admin Console

Available at `/admin`, the admin console exposes:

- **Intersection management** — create, edit, delete intersections
- **Manual phase override** — lock any signal to RED / YELLOW / GREEN for 120 seconds
- **Emergency corridor** — activate a green corridor along a chosen route
- **Simulation controls** — toggle auto-simulate, manually set vehicle counts, trigger simulation ticks

---

## 🔧 Configuration Reference

### `application.yml` — Production

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smart_traffic
    username: smartuser
    password: smartpass
  data.redis:
    host: localhost
    port: 6379
  jpa.hibernate.ddl-auto: update

server.port: 8080

app:
  jwt:
    secret: "ChangeThisToASecretKeyAtLeast256BitsLong..."
    expiration-ms: 86400000        # 24 hours
  signal:
    base-green-seconds: 25
    bonus-seconds-per-threshold: 5
    vehicle-threshold: 10
    min-green-seconds: 15
    max-green-seconds: 90
    default-red-seconds: 30
  emergency:
    hold-seconds: 60
```

### `application-local.yml` — Development (H2, no Redis)

```yaml
spring:
  autoconfigure.exclude:
    - RedisAutoConfiguration
    - RedisRepositoriesAutoConfiguration
  datasource:
    url: jdbc:h2:mem:smarttraffic;MODE=MySQL
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2.console:
    enabled: true
    path: /h2-console
  jpa.hibernate.ddl-auto: create-drop
```

---

## 🗄️ Database Schema

```
intersection                road_segment
─────────────────           ──────────────────
id            PK  ◄──────── from_id       FK
name                ◄──────── to_id         FK
latitude                    base_travel_secs
longitude
lane_count                  traffic_log
is_main_road                ──────────────────
current_phase               id            PK
green_duration              intersection_id FK
red_duration                vehicle_count
vehicle_count               green_duration
                            red_duration
                            logged_at

user                        emergency_event
──────────────────          ──────────────────
id            PK            id            PK
username                    active
password (BCrypt)           route_ids (JSON)
roles  (M:N → role)        ends_at

role                        saved_route
──────────────────          ──────────────────
id            PK            id            PK
name (ADMIN|VIEWER)         from_id · to_id
                            path_ids
```

---

## 🌏 Seed Data — Delhi NCR Intersections

16 real-world intersections are seeded automatically on first startup:

| # | Name | Latitude | Longitude | Lanes | Main Road |
|---|---|---|---|---|---|
| 1 | ITO Crossing | 28.6280 | 77.2410 | 4 | ✅ |
| 2 | Connaught Place | 28.6315 | 77.2167 | 4 | ✅ |
| 3 | India Gate Circle | 28.6129 | 77.2295 | 3 | ✅ |
| 4 | Chandni Chowk | 28.6506 | 77.2334 | 3 | ✅ |
| 5 | AIIMS Crossing | 28.5672 | 77.2100 | 3 | — |
| 6 | Hauz Khas Junction | 28.5494 | 77.2001 | 2 | — |
| 7 | Nehru Place Flyover | 28.5491 | 77.2530 | 2 | — |
| 8 | Rajouri Garden Metro | 28.6493 | 77.1215 | 3 | ✅ |
| 9 | Punjabi Bagh Chowk | 28.6682 | 77.1310 | 2 | — |
| 10 | Rohini Sec-3 Signal | 28.7159 | 77.1166 | 3 | ✅ |
| 11 | Akshardham Crossing | 28.6127 | 77.2773 | 2 | — |
| 12 | Noida Sec-18 Signal | 28.5700 | 77.3210 | 2 | — |
| 13 | Moolchand Flyover | 28.5688 | 77.2375 | 3 | ✅ |
| 14 | Dhaula Kuan Junction | 28.5930 | 77.1660 | 2 | — |
| 15 | Kashmere Gate ISBT | 28.6674 | 77.2295 | 2 | — |
| 16 | Sarai Kale Khan | 28.5893 | 77.2571 | 3 | ✅ |

Also seeded: **20 bidirectional directed road segments** with base travel times ranging from 8–25 seconds, forming the graph used by the Dijkstra route optimizer.

---

> Built with ❤️ for smarter cities
>
> Spring Boot 3 · Next.js 15 · React 19 · Leaflet · Recharts · WebSocket · Dijkstra · JWT

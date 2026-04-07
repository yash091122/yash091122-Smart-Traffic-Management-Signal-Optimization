# 🚦 TrafficPulse AI

### Smart Traffic Management & Signal Optimization System — Delhi NCR

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Next.js](https://img.shields.io/badge/Next.js-15.2.4-000000?style=flat-square&logo=nextdotjs&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?style=flat-square&logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-5.7-3178C6?style=flat-square&logo=typescript&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=flat-square&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket-STOMP%20%2B%20SockJS-010101?style=flat-square)
![JWT](https://img.shields.io/badge/Auth-JWT-000000?style=flat-square&logo=jsonwebtokens&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind-3.4-06B6D4?style=flat-square&logo=tailwindcss&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

---

> **TrafficPulse AI** is a production-grade, full-stack smart city platform that monitors, simulates, and adaptively optimizes traffic signals across **16 real-world Delhi NCR intersections** in real time. It combines a Spring Boot 3 microservice backend with a Next.js 15 dashboard featuring live WebSocket feeds, interactive Leaflet maps, Dijkstra-based route optimization, emergency corridor activation, and rich analytics — all wrapped in a dark glassmorphism UI.

---

## 📋 Table of Contents

1. [Overview](#-overview)
2. [Live Features](#-live-features)
3. [System Architecture](#-system-architecture)
4. [Tech Stack](#-tech-stack)
5. [Repository Structure](#-repository-structure)
6. [Getting Started](#-getting-started)
   - [Prerequisites](#prerequisites)
   - [Option A — Docker + MySQL + Redis](#option-a--docker--mysql--redis-recommended)
   - [Option B — Local Dev with H2](#option-b--local-dev-with-h2-in-memory-no-docker)
   - [Frontend Setup](#frontend-setup)
7. [Environment Variables](#-environment-variables)
8. [Default Credentials & Roles](#-default-credentials--roles)
9. [REST API Reference](#-rest-api-reference)
   - [Authentication](#authentication)
   - [Intersections](#intersections)
   - [Live Signals](#live-signals)
   - [Analytics](#analytics)
   - [Route Optimization](#route-optimization)
   - [Emergency Corridor](#emergency-corridor)
   - [Simulation Control](#simulation-control)
10. [WebSocket Events](#-websocket-events)
11. [Core Algorithms](#-core-algorithms)
    - [Adaptive Signal Optimization](#adaptive-signal-optimization)
    - [Dijkstra Route Optimizer](#dijkstra-route-optimizer)
12. [Emergency Corridor System](#-emergency-corridor-system)
13. [Traffic Simulation Engine](#-traffic-simulation-engine)
14. [Frontend Dashboard](#-frontend-dashboard)
15. [Admin Console](#-admin-console)
16. [Signal State Backends](#-signal-state-backends-redis-vs-in-memory)
17. [Configuration Reference](#-configuration-reference)
18. [Database Schema](#-database-schema)
19. [Seed Data — Delhi NCR](#-seed-data--delhi-ncr-intersections)
20. [Roadmap](#-roadmap)

---

## 🌐 Overview

Modern cities waste millions of hours annually to poorly timed traffic signals. **TrafficPulse AI** addresses this by:

- **Continuously measuring** vehicle density at every intersection via a time-aware simulation engine (replaceable with real IoT sensors)
- **Automatically recalculating** green/red durations every 10 seconds using a vehicle-count-weighted algorithm
- **Broadcasting** all state changes to every connected dashboard client in real time via WebSocket (STOMP protocol)
- **Computing optimal routes** between any two intersections using Dijkstra's algorithm, with edge weights that factor in current signal phases and queue lengths
- **Activating emergency corridors** on demand — instantly turning all signals along a route GREEN and notifying all clients
- **Providing deep analytics** on wait times, peak-hour patterns, congestion trends, and intersection rankings

The backend runs on **Spring Boot 3.2.5 (Java 17)** and supports two database modes: MySQL 8 + Redis 7 for production and H2 in-memory + in-memory signal state for immediate local development. The frontend is a **Next.js 15 / React 19** single-page application styled with Tailwind CSS 3 and communicating over both REST (JWT-secured) and WebSocket (STOMP/SockJS).

---

## ✨ Live Features

### 🔴🟡🟢 Adaptive Signal Timing
Every intersection's green and red durations are recalculated every **10 seconds** by `SignalOptimizationService`. The formula considers real-time vehicle count, configurable bonus increments per vehicle threshold, hard min/max bounds, and a one-time recalibration bonus after a violation event. Signals never get stuck in a fixed cycle — they breathe with traffic.

### 📡 Real-Time WebSocket Feed
`WebSocketBroadcastService` runs two `@Scheduled` tasks every **5 seconds**:
- Pushes the full `LiveSignalDto[]` array to `/topic/signals`
- Pushes aggregate stats (`totalIntersections`, `averageGreenSeconds`, `activeEmergencies`) to `/topic/stats`

Emergency events push immediately to `/topic/emergency` upon activation.

### 🗺️ Interactive Leaflet Map
The React-Leaflet map centers on Delhi NCR. Each intersection is rendered as a color-coded circle marker:
- 🟢 **Green** — low congestion (vehicle count < 10)
- 🟡 **Amber** — moderate congestion (10–25 vehicles)
- 🔴 **Red** — heavy congestion (> 25 vehicles)

Clicking a marker opens a popup with live phase, vehicle count, green/red durations, and lane count. The global search bar filters intersections and flies the map camera to the selected node.

### 🚑 Emergency Corridor
One API call forces every intersection along a specified route to GREEN for a configurable hold window (default 60 s). A WebSocket broadcast instantly notifies all connected operator dashboards. Expired corridors are auto-deactivated.

### 🧭 Traffic-Aware Route Optimization
Dijkstra's algorithm runs across a directed graph of `RoadSegment` entities. Edge weights are dynamically computed from current signal state: a RED signal adds ~45% of its full red duration as wait cost; a GREEN signal adds only 2 s. This produces routes that genuinely avoid congested corridors.

### 📊 Analytics Engine
Four dedicated analytics endpoints deliver:
- Per-intersection **average wait estimates** (7-day rolling window)
- **Hourly event distribution** heatmap (30-day window)
- **24-hour congestion trend** bucketed by hour
- **Top-N congested intersections** ranked by average vehicle count with a 0–100 congestion score

### 🔐 JWT Authentication & RBAC
Spring Security protects all `/api/**` routes. A JWT is obtained via `/api/auth/login` and attached as `Authorization: Bearer <token>`. Two roles exist: **ADMIN** (full CRUD + emergency + simulation) and **VIEWER** (read-only).

### 🔄 Time-Aware Traffic Simulation
`TrafficSimulatorService` generates realistic vehicle counts using time-of-day curves — morning rush (7–9 AM) and evening peak (5–8 PM) produce much higher vehicle counts on main roads, while off-peak periods generate low-variance baseline noise. This makes the dashboard look alive without needing physical sensors.

---

## 🏗️ System Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                         BROWSER  (Client)                            │
│                                                                      │
│   ┌──────────────────────────────────────────────────────────────┐   │
│   │          Next.js 15  ·  React 19  ·  Tailwind CSS 3          │   │
│   │                                                              │   │
│   │  DashboardView.tsx          TrafficMap.tsx (Leaflet)         │   │
│   │  AdminPage.tsx              Recharts (area/pie charts)       │   │
│   │  useTrafficFeed hook        @stomp/stompjs + sockjs-client   │   │
│   └──────────────────┬───────────────────────────┬──────────────┘   │
│                      │  REST  (JWT Bearer)        │  WebSocket       │
│                      │  /api/**                   │  STOMP/SockJS   │
└──────────────────────┼────────────────────────────┼──────────────────┘
                       │                            │
┌──────────────────────┼────────────────────────────┼──────────────────┐
│                    SPRING BOOT 3.2.5  (Port 8080)                    │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │                    REST Controllers                            │  │
│  │  AuthController · IntersectionController · SignalController    │  │
│  │  AnalyticsController · EmergencyController · RouteController   │  │
│  │  SimulationController · DashboardController                    │  │
│  ├────────────────────────────────────────────────────────────────┤  │
│  │                    Service Layer                               │  │
│  │  SignalOptimizationService   (runs every 10 s)                 │  │
│  │  SignalPhaseScheduler        (RED→GREEN→YELLOW cycle)          │  │
│  │  WebSocketBroadcastService   (broadcasts every 5 s)            │  │
│  │  EmergencyService            (corridor activation/expiry)      │  │
│  │  RouteOptimizationService    (Dijkstra on RoadSegment graph)   │  │
│  │  TrafficSimulatorService     (time-of-day vehicle sim)         │  │
│  │  SimulationScheduler         (scheduled sim ticks)             │  │
│  │  AnalyticsService            (aggregation & reporting)         │  │
│  │  TrafficLogService           (history persistence)             │  │
│  ├────────────────────────────────────────────────────────────────┤  │
│  │               Signal State (Strategy Pattern)                  │  │
│  │  SignalStateService  ──►  RedisSignalStateService  (prod)      │  │
│  │                      └──►  InMemorySignalStateService  (dev)   │  │
│  ├────────────────────────────────────────────────────────────────┤  │
│  │  Spring Security  ·  JwtAuthFilter  ·  JwtService              │  │
│  ├──────────────────────────┬─────────────────────────────────────┤  │
│  │   JPA / Hibernate        │  WebSocket (STOMP broker)           │  │
│  └──────────────────────────┼─────────────────────────────────────┘  │
│                 ┌───────────┴────────┐    ┌──────────────┐           │
│                 │  MySQL 8  /  H2    │    │   Redis 7    │           │
│                 └────────────────────┘    └──────────────┘           │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

### Backend

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Language |
| Spring Boot | 3.2.5 | Application framework |
| Spring Web (MVC) | — | REST API |
| Spring Data JPA + Hibernate | — | ORM / database access |
| Spring Security | — | Authentication & authorization |
| Spring WebSocket + STOMP | — | Real-time bidirectional events |
| Spring Validation | — | Request DTO validation |
| Spring Data Redis | — | Redis integration |
| Thymeleaf | — | Server-side HTML templates (admin/dashboard pages) |
| jjwt | 0.12.5 | JWT generation and validation |
| Lombok | 1.18.34 | Boilerplate code reduction |
| MySQL Connector/J | — | MySQL 8 JDBC driver |
| H2 Database | — | Embedded in-memory DB for dev profile |
| Maven | 3.x (wrapper) | Build tool |

### Frontend

| Technology | Version | Purpose |
|---|---|---|
| Next.js | 15.2.4 | React framework (App Router, Turbopack) |
| React | 19 | UI library |
| TypeScript | 5.7 | Type safety |
| Tailwind CSS | 3.4.16 | Utility-first styling |
| Leaflet + React-Leaflet | 1.9.4 / 5.0.0-rc.2 | Interactive traffic map |
| Recharts | 2.15.0 | Area charts and pie/donut charts |
| @stomp/stompjs | 7.0.0 | STOMP WebSocket client |
| sockjs-client | 1.6.1 | WebSocket fallback transport |
| lucide-react | 0.469.0 | Icon set |

### Infrastructure

| Technology | Purpose |
|---|---|
| Docker Compose | One-command MySQL 8 + Redis 7 setup |
| MySQL 8.0 | Production relational database |
| Redis 7 Alpine | Signal state cache (production) |

---

## 📁 Repository Structure

```
Smart Traffic/
│
├── .gitignore                        ← Java + Node + IDE + env exclusions
├── docker-compose.yml                ← MySQL 8 + Redis 7 containers
├── pom.xml                           ← Maven dependencies & build plugins
├── mvnw / mvnw.cmd                   ← Maven wrapper (no install needed)
├── README.md                         ← This file
│
├── src/
│   └── main/
│       ├── java/com/smarttraffic/
│       │   │
│       │   ├── SmartTrafficApplication.java      ← @SpringBootApplication entry point
│       │   │
│       │   ├── bootstrap/
│       │   │   └── DataInitializer.java          ← Seeds users, 16 intersections,
│       │   │                                        road segments on first boot
│       │   │
│       │   ├── config/
│       │   │   ├── SecurityConfig.java            ← Filter chain, CORS, CSRF, roles
│       │   │   ├── WebSocketConfig.java           ← STOMP broker, /ws endpoint, SockJS
│       │   │   ├── WebMvcConfig.java              ← MVC & cross-origin setup
│       │   │   ├── JwtProperties.java             ← secret, expiration-ms
│       │   │   ├── SignalProperties.java          ← base-green, bonus, threshold, min/max
│       │   │   └── EmergencyProperties.java       ← hold-seconds
│       │   │
│       │   ├── controller/
│       │   │   ├── AuthController.java            ← POST /api/auth/login
│       │   │   ├── IntersectionController.java    ← CRUD + phase override
│       │   │   ├── SignalController.java          ← GET /api/signals/live
│       │   │   ├── AnalyticsController.java       ← 4 analytics endpoints
│       │   │   ├── EmergencyController.java       ← POST /api/emergency/activate
│       │   │   ├── RouteController.java           ← GET /api/route/optimize
│       │   │   ├── SimulationController.java      ← POST /api/simulation/*
│       │   │   └── DashboardController.java       ← Thymeleaf page routes
│       │   │
│       │   ├── dto/
│       │   │   ├── LoginRequest.java              ← { username, password }
│       │   │   ├── LoginResponse.java             ← { token, username, role }
│       │   │   ├── LiveSignalDto.java             ← Real-time signal snapshot record
│       │   │   ├── IntersectionRequest.java       ← Create/update intersection body
│       │   │   └── PhaseOverrideRequest.java      ← { phase: "GREEN" }
│       │   │
│       │   ├── entity/
│       │   │   ├── Intersection.java              ← Core @Entity (lat, lng, phase, counts)
│       │   │   ├── RoadSegment.java               ← Directed edge (from → to, baseSecs)
│       │   │   ├── TrafficLog.java                ← Historical snapshot per intersection
│       │   │   ├── EmergencyEvent.java            ← Active/expired emergency corridors
│       │   │   ├── User.java                      ← Auth user (username, BCrypt pw, roles)
│       │   │   ├── Role.java                      ← Enum-backed ADMIN / VIEWER
│       │   │   ├── SignalPhase.java               ← Enum: RED, YELLOW, GREEN
│       │   │   └── SavedRoute.java                ← Persisted optimized route
│       │   │
│       │   ├── repository/
│       │   │   ├── IntersectionRepository.java
│       │   │   ├── RoadSegmentRepository.java
│       │   │   ├── TrafficLogRepository.java      ← Custom JPQL for analytics queries
│       │   │   ├── EmergencyEventRepository.java
│       │   │   ├── UserRepository.java
│       │   │   └── RoleRepository.java
│       │   │
│       │   ├── security/
│       │   │   ├── JwtService.java                ← Token generation, claims extraction
│       │   │   ├── JwtAuthFilter.java             ← OncePerRequestFilter JWT validation
│       │   │   └── CustomUserDetailsService.java  ← UserDetailsService impl
│       │   │
│       │   └── service/
│       │       ├── SignalStateService.java         ← Interface (strategy pattern)
│       │       ├── RedisSignalStateService.java    ← Redis hash-based implementation
│       │       ├── InMemorySignalStateService.java ← ConcurrentHashMap implementation
│       │       ├── SignalOptimizationService.java  ← @Scheduled every 10 s
│       │       ├── SignalPhaseScheduler.java       ← Timed RED→GREEN→YELLOW cycling
│       │       ├── EmergencyService.java           ← Corridor activate/expire logic
│       │       ├── RouteOptimizationService.java   ← Dijkstra shortest path
│       │       ├── TrafficSimulatorService.java    ← Time-of-day vehicle simulation
│       │       ├── SimulationScheduler.java        ← Scheduled simulation ticks
│       │       ├── TrafficLogService.java          ← Persist snapshot to TrafficLog
│       │       ├── AnalyticsService.java           ← Aggregation & reporting methods
│       │       └── WebSocketBroadcastService.java  ← @Scheduled STOMP broadcasts
│       │
│       └── resources/
│           ├── application.yml                    ← Production config (MySQL + Redis)
│           ├── application-local.yml              ← Dev config (H2 + no Redis)
│           └── templates/
│               ├── dashboard.html                 ← Thymeleaf dashboard template
│               ├── admin.html                     ← Thymeleaf admin template
│               └── login.html                     ← Thymeleaf login page
│
└── frontend/
    ├── package.json                  ← Dependencies & npm scripts
    ├── next.config.ts                ← Next.js config
    ├── tailwind.config.ts            ← Custom colors, glassmorphism tokens
    ├── tsconfig.json
    ├── postcss.config.mjs
    ├── .env.local.example            ← Template for env vars
    │
    └── src/
        ├── app/
        │   ├── globals.css           ← Glassmorphism panel classes, base styles
        │   ├── layout.tsx            ← Root layout (Inter font, metadata)
        │   ├── page.tsx              ← / → renders <DashboardView />
        │   └── admin/
        │       └── page.tsx          ← /admin → Admin console (14 KB)
        │
        ├── components/
        │   ├── TrafficMap.tsx        ← Leaflet map w/ live markers (SSR-disabled)
        │   └── dashboard/
        │       └── DashboardView.tsx ← Main dashboard component (877 lines)
        │
        ├── hooks/
        │   └── useTrafficFeed.ts     ← STOMP WebSocket subscription hook
        │
        ├── lib/
        │   ├── api.ts               ← getLiveSignals, getTopCongested, etc.
        │   └── authFetch.ts         ← JWT-injecting fetch wrapper
        │
        └── types/
            └── traffic.ts           ← LiveSignal, DashboardStats, TopCongested, etc.
```

---

## 🚀 Getting Started

### Prerequisites

| Tool | Version | Notes |
|---|---|---|
| Java JDK | **17+** | Required for Spring Boot 3.2 |
| Maven | Any | Wrapper (`mvnw`) included — no installation needed |
| Node.js | **18+** | For the Next.js frontend |
| npm | **9+** | Included with Node |
| Docker | **20+** | Optional — only for Option A |

---

### Option A — Docker + MySQL + Redis (Recommended)

```bash
# 1. Start MySQL 8 and Redis 7 containers
docker compose up -d

# 2. Verify containers are healthy
docker compose ps

# 3. Start the Spring Boot backend
./mvnw spring-boot:run

# API available at: http://localhost:8080
```

> **First boot:** `DataInitializer` detects an empty database and automatically seeds
> 16 intersections, road segments, and both default users. No SQL scripts needed.

---

### Option B — Local Dev with H2 In-Memory (No Docker)

Ideal for quick testing — zero external dependencies.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

| Resource | URL | Notes |
|---|---|---|
| API | `http://localhost:8080` | Same endpoints |
| H2 Console | `http://localhost:8080/h2-console` | Web SQL UI |
| JDBC URL | `jdbc:h2:mem:smarttraffic` | |
| Username | `sa` | Password: *(empty)* |

In `local` profile:
- Redis is **excluded** from auto-configuration — `InMemorySignalStateService` is used automatically
- Schema is created fresh on every boot (`create-drop`) and seeded by `DataInitializer`
- H2 runs in MySQL compatibility mode

---

### Frontend Setup

```bash
# Navigate to the frontend directory
cd frontend

# Install all dependencies
npm install

# Copy the environment template
cp .env.local.example .env.local

# Start the Next.js dev server (Turbopack)
npm run dev
```

Open **http://localhost:3000** in your browser.

> **Production build:**
> ```bash
> npm run build
> npm run start   # serves on port 3000
> ```

---

## 🔐 Environment Variables

### Backend — `src/main/resources/application.yml`

All values can be overridden with environment variables or a `.env` file.

| Key | Default | Description |
|---|---|---|
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/smart_traffic` | MySQL connection URL |
| `spring.datasource.username` | `smartuser` | DB username |
| `spring.datasource.password` | `smartpass` | DB password |
| `spring.data.redis.host` | `localhost` | Redis host |
| `spring.data.redis.port` | `6379` | Redis port |
| `app.jwt.secret` | *(placeholder)* | **Change in production!** Min 256-bit string |
| `app.jwt.expiration-ms` | `86400000` | Token lifetime — 24 hours |
| `app.signal.base-green-seconds` | `25` | Baseline green duration |
| `app.signal.bonus-seconds-per-threshold` | `5` | Extra green per vehicle batch |
| `app.signal.vehicle-threshold` | `10` | Vehicles per bonus unit |
| `app.signal.min-green-seconds` | `15` | Hard lower bound |
| `app.signal.max-green-seconds` | `90` | Hard upper bound |
| `app.signal.default-red-seconds` | `30` | Baseline red duration |
| `app.emergency.hold-seconds` | `60` | Emergency GREEN hold time |

### Frontend — `frontend/.env.local`

```env
# Base URL of the Spring Boot API (no trailing slash)
NEXT_PUBLIC_API_URL=http://localhost:8080
```

---

## 👤 Default Credentials & Roles

| Username | Password | Role | Access Level |
|---|---|---|---|
| `admin` | `adminpass` | **ADMIN** | Full: CRUD intersections, emergency, simulation, analytics |
| `viewer` | `viewerpass` | **VIEWER** | Read-only: dashboard, live signals, analytics |

> Passwords are stored as **BCrypt** hashes. Change before production deployment.

---

## 📡 REST API Reference

All protected endpoints require:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

---

### Authentication

#### `POST /api/auth/login` — Public

Authenticate and receive a JWT token.

**Request body:**
```json
{
  "username": "admin",
  "password": "adminpass"
}
```

**Response `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "role": "ADMIN"
}
```

---

### Intersections

Base path: `/api/intersections`

#### `GET /api/intersections` — List all

Returns all intersections with their current state.

**Response `200 OK`:**
```json
[
  {
    "id": 1,
    "name": "ITO Crossing",
    "latitude": 28.6280,
    "longitude": 77.2410,
    "laneCount": 4,
    "mainRoad": true,
    "currentPhase": "GREEN",
    "greenDuration": 35,
    "redDuration": 30,
    "vehicleCount": 42
  }
]
```

#### `GET /api/intersections/{id}` — Get by ID

Returns `404 Not Found` if ID does not exist.

#### `POST /api/intersections` — Create

**Request body:**
```json
{
  "name": "New Junction",
  "latitude": 28.6500,
  "longitude": 77.2200,
  "laneCount": 3,
  "currentPhase": "RED",
  "greenDuration": 25,
  "redDuration": 30,
  "vehicleCount": 0,
  "mainRoad": false
}
```

#### `PUT /api/intersections/{id}` — Update

Same body as create. Returns `404` if not found.

#### `DELETE /api/intersections/{id}` — Delete

Returns `204 No Content` on success, `404` if not found.

#### `POST /api/intersections/{id}/override-phase` — Manual Phase Lock

Forces the intersection to the specified phase for **120 seconds**, bypassing the automatic scheduler.

**Request body:**
```json
{ "phase": "GREEN" }
```

**Response `200 OK`:** returns the updated `LiveSignalDto`

---

### Live Signals

#### `GET /api/signals/live` — All live signal states

Returns real-time signal data for all intersections from the signal state service (Redis or in-memory).

**Response `200 OK`:**
```json
[
  {
    "id": 1,
    "name": "ITO Crossing",
    "phase": "GREEN",
    "greenDuration": 35,
    "redDuration": 28,
    "vehicleCount": 47,
    "latitude": 28.6280,
    "longitude": 77.2410
  },
  ...
]
```

---

### Analytics

Base path: `/api/analytics`

#### `GET /api/analytics/wait-times`

Average wait estimates per intersection over the **last 7 days**.

**Response `200 OK`:**
```json
{
  "1": {
    "intersectionName": "ITO Crossing",
    "averageRedDurationSeconds": 28.5,
    "estimatedWaitSeconds": 11.4
  }
}
```

#### `GET /api/analytics/peak-hours`

Hourly event count distribution over the **last 30 days** — useful for heatmap rendering.

**Response `200 OK`:**
```json
[
  { "hour": 7, "eventCount": 1842 },
  { "hour": 8, "eventCount": 2310 },
  ...
]
```

#### `GET /api/analytics/congestion?hours=24`

Average vehicle count per hour over the specified window (default: 24 hours), bucketed into 1-hour slots.

**Response `200 OK`:**
```json
[
  { "fromEpochMs": 1712400000000, "averageVehicleCount": 18.3, "sampleCount": 48 },
  ...
]
```

#### `GET /api/analytics/top-congested?limit=5`

Top N intersections ranked by average vehicle count over the **last 7 days**, with a 0–100 congestion score.

**Response `200 OK`:**
```json
[
  {
    "intersectionId": 1,
    "name": "ITO Crossing",
    "averageVehicleCount": 38.7,
    "congestionScore": 77.4
  },
  ...
]
```

---

### Route Optimization

#### `GET /api/route/optimize?from={id}&to={id}`

Finds the traffic-time-optimal path between two intersections using Dijkstra's algorithm. Edge weights include base travel time plus estimated signal wait based on current phase and queue length.

**Response `200 OK`:**
```json
{
  "from": 1,
  "to": 12,
  "path": [1, 3, 11, 12],
  "reachable": true
}
```

Returns `"reachable": false` and an empty `path` if no path exists.

---

### Emergency Corridor

#### `POST /api/emergency/activate`

Deactivates any existing active emergency, creates a new `EmergencyEvent`, forces all specified intersections to GREEN, and broadcasts to all WebSocket clients.

**Request body:**
```json
{
  "routeIntersectionIds": [1, 3, 11, 12]
}
```

**Response `200 OK`:** returns the created `EmergencyEvent` entity

---

### Simulation Control

#### `POST /api/simulation/set-count`

Manually sets the vehicle count for a specific intersection (overrides simulation).

```json
{ "intersectionId": 1, "vehicleCount": 85 }
```

#### `POST /api/simulation/run`

Triggers one simulation tick immediately. Optionally toggle the auto-simulate flag.

```json
{ "auto": false }
```

**Response `200 OK`:**
```json
{ "status": "tick", "autoSimulate": false }
```

---

## 🔌 WebSocket Events

**Endpoint:** `http://localhost:8080/ws` (SockJS with STOMP fallback)

Connect using `@stomp/stompjs` + `sockjs-client`:

```typescript
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const client = new Client({
  webSocketFactory: () =>
    new SockJS(`${process.env.NEXT_PUBLIC_API_URL}/ws`) as unknown as WebSocket,
  reconnectDelay: 5000,
  debug: () => {},
  onConnect: () => {
    // Live signal updates every 5 s
    client.subscribe("/topic/signals", (msg) => {
      const signals = JSON.parse(msg.body); // LiveSignal[]
    });

    // Aggregate stats every 5 s
    client.subscribe("/topic/stats", (msg) => {
      const stats = JSON.parse(msg.body);
      // { totalIntersections, averageGreenSeconds, activeEmergencies }
    });

    // Emergency corridor activations (pushed immediately)
    client.subscribe("/topic/emergency", (msg) => {
      const event = JSON.parse(msg.body);
      // { message, intersectionIds[], endsAt }
    });
  },
  onDisconnect: () => console.log("WS disconnected"),
});

client.activate();
```

| Topic | Push Interval | Payload |
|---|---|---|
| `/topic/signals` | Every 5 s | `LiveSignalDto[]` |
| `/topic/stats` | Every 5 s | `{ totalIntersections, averageGreenSeconds, activeEmergencies }` |
| `/topic/emergency` | Immediate on event | `{ message, intersectionIds, endsAt }` |

---

## 🧠 Core Algorithms

### Adaptive Signal Optimization

**Class:** `SignalOptimizationService` · **Trigger:** `@Scheduled(fixedRate = 10_000)`

```
FOR each intersection i:

  vehicleCount  ← signalStateService.getLive(i.id).vehicleCount
                  (falls back to DB value if not in state store)

  bonus         ← recalibrationBonus.get(i.id) or 0
                  (one-time bonus after violation; consumed immediately)

  extra         ← (vehicleCount / vehicleThreshold) × bonusSecondsPerThreshold

  greenDuration ← baseGreenSeconds + extra + bonus
  greenDuration ← clamp(minGreenSeconds, maxGreenSeconds, greenDuration)

  IF vehicleCount < vehicleThreshold / 2:
      redDuration ← max(15, defaultRedSeconds − 5)     ← lighter red for sparse traffic
  ELSE IF vehicleCount > vehicleThreshold × 3:
      redDuration ← min(60, defaultRedSeconds + 10)    ← longer red for heavy cross-flow
  ELSE:
      redDuration ← defaultRedSeconds

  SAVE updated durations to DB + signal state store
```

**Effect:** A quiet intersection (e.g., 3 vehicles) gets `green = 25s, red = 25s`. A congested one (e.g., 50 vehicles) gets `green = 50s, red = 40s`. The optimizer adapts every 10 seconds with no manual intervention.

---

### Dijkstra Route Optimizer

**Class:** `RouteOptimizationService`

```
FUNCTION shortestPath(from, to):

  Build adjacency list from all RoadSegment entities

  dist[from]  ← 0
  PQ          ← { from }

  WHILE PQ not empty:
    u ← PQ.poll (lowest dist)
    IF u == to → break

    FOR each edge (u → v, baseTravelSeconds):
      waitAtV ← estimatedWaitAt(v)    ← see formula below
      alt     ← dist[u] + baseTravelSeconds + waitAtV

      IF alt < dist[v]:
        dist[v] ← alt
        prev[v] ← u
        PQ.add(v)

  Reconstruct path by walking prev map from to → from
  Reverse and return

FUNCTION estimatedWaitAt(intersectionId):
  signal ← signalStateService.getLive(intersectionId)

  phaseWait ← switch signal.phase:
    RED    → signal.redDuration × 0.45
    YELLOW → 4
    GREEN  → 2

  RETURN phaseWait + signal.vehicleCount × 0.35
```

This makes the route optimizer traffic-aware in real time: a RED signal with 40 vehicles adds ~(`30 × 0.45`) + (`40 × 0.35`) = **13.5 + 14 = 27.5 seconds** of wait cost, causing the algorithm to seek an alternative route.

---

## 🚑 Emergency Corridor System

**Class:** `EmergencyService`

When `activate(routeIntersectionIds)` is called:

```
1. Mark all existing active EmergencyEvents as inactive (only one active at a time)

2. Create a new EmergencyEvent:
   - active = true
   - routeIntersectionIds = [provided list]
   - endsAt = now + holdSeconds (default: 60 s)

3. For each intersection ID in the route:
   - signalStateService.setPhase(id, GREEN, now + holdSeconds × 1000)
   (intersections not found in DB are silently skipped)

4. Broadcast via WebSocket /topic/emergency:
   {
     "message": "Emergency corridor active",
     "intersectionIds": [...],
     "endsAt": "2026-04-07T08:50:00Z"
   }

5. Auto-deactivation: EmergencyService.deactivateExpired() is called on
   a schedule — any EmergencyEvent whose endsAt < now is set to active=false
```

The dashboard's notification panel receives the push and displays a dismissible alert. Emergency intersections return to normal optimization after the hold window expires.

---

## 🔄 Traffic Simulation Engine

**Class:** `TrafficSimulatorService` · **Trigger:** `SimulationScheduler` (`@Scheduled`)

```
FUNCTION tickSimulation():

  IF autoSimulate == false → return

  hour ← LocalTime.now().hour
  morningPeak ← hour in [7, 8]
  eveningPeak ← hour in [17, 18, 19]

  FOR each intersection i:
    base ← random(2, 25)

    IF morningPeak:
      count ← i.isMainRoad
               ? base + random(15, 45)     ← heavy arterial traffic
               : max(0, base − 10)         ← side streets clearing out

    ELSE IF eveningPeak:
      count ← i.isMainRoad
               ? base + random(10, 40)     ← heavy return traffic
               : base + random(5, 25)      ← side streets also busy

    ELSE:
      count ← base + random(−3, 8)        ← off-peak low variance

    count ← clamp(0, 200, count)
    signalStateService.setVehicleCount(i.id, count)
    i.vehicleCount ← count

  intersectionRepository.saveAll(all)
```

Manual override via `POST /api/simulation/set-count` accepts values 0–500 and bypasses the simulation for that intersection until the next tick.

---

## 🎨 Frontend Dashboard

The dashboard renders at `/` and is built entirely in `DashboardView.tsx` (877 lines).

### Layout

```
┌─────────────────────────────────────────────────────────────────┐
│  HEADER: Logo · Search · API/WS status · Clock · 🔔 · Admin ⚙  │
├───────────────┬─────────────────────────────┬───────────────────┤
│  LEFT SIDEBAR │        CENTER MAP           │  RIGHT SIDEBAR    │
│               │                             │                   │
│  Active       │   Leaflet map (Delhi NCR)   │  Traffic         │
│  vehicles     │   Color-coded markers        │  analytics       │
│               │   Click-to-inspect popups   │  (area chart)    │
│  Congestion % │   Search fly-to             │                   │
│               │   Fullscreen toggle         │  Vehicle mix     │
│  Recent       │   Zoom indicator            │  (donut chart)   │
│  alerts       │   Traffic legend            │                   │
│               │                             │  Top congested   │
│  Peak hour    │                             │  intersections   │
│  flow bars    │                             │  (leaderboard)   │
└───────────────┴─────────────────────────────┴───────────────────┘
```

### Key UI Behaviours

| Behaviour | Detail |
|---|---|
| **Live search** | Filters intersection list in real time; dropdown shows up to 8 matches with ID, vehicle count, and phase; clicking flies the map camera to that node |
| **Notification panel** | Receives WebSocket emergency pushes; shows unread badge count; supports mark-all-read and per-notification dismiss |
| **Alert sidebar** | Shows top-congested intersection, secondary hotspot, and lightest-traffic zone; clicking any alert focuses the map on that intersection |
| **Connection status** | Two pips in the header — green API pip (REST reachable) + green/amber WS pip (WebSocket connected) |
| **Fullscreen map** | Expands the center map panel to browser fullscreen via the Fullscreen API |
| **Responsive grid** | 3-column layout on desktop (`lg:grid-cols-[1fr_3fr_1fr]`), stacks vertically on mobile |
| **Real-time clock** | Updates every second; fixed `en-US` locale prevents SSR/client hydration mismatch |
| **Auto-refresh** | `RefreshCw` button manually triggers all REST fetches; animated spinner during loading |

### Tailwind Design Tokens

```typescript
// tailwind.config.ts
colors: {
  glass: {
    border:     "rgba(255,255,255,0.12)",
    highlight:  "rgba(255,255,255,0.08)",
  },
  pulse: {
    blue:   "#3b82f6",
    cyan:   "#22d3ee",
    violet: "#8b5cf6",
  },
},
```

Glass panel classes are defined in `globals.css`:
- `.glass-panel` — standard panel with border + backdrop blur
- `.glass-panel-strong` — higher opacity for map container and header

---

## ⚙️ Admin Console

Available at `/admin`. Requires ADMIN role JWT to call mutating API endpoints.

| Section | Capabilities |
|---|---|
| **Intersection Manager** | Create new intersections with all fields; edit name, coordinates, lane count, phase, durations, vehicle count; delete intersections |
| **Phase Override** | Select any intersection and lock it to RED / YELLOW / GREEN for 120 seconds |
| **Emergency Corridor** | Multi-select intersections, activate a green corridor, view active emergency status |
| **Simulation Controls** | Toggle auto-simulate on/off; set manual vehicle count per intersection; trigger an immediate simulation tick |

---

## 🔀 Signal State Backends (Redis vs In-Memory)

`SignalStateService` is an interface with two implementations, selected by Spring profile:

| Implementation | Profile | Storage | Persistence |
|---|---|---|---|
| `RedisSignalStateService` | default (MySQL) | Redis hashes per intersection | Survives restarts |
| `InMemorySignalStateService` | `local` | `ConcurrentHashMap` | Lost on restart |

Both implementations support the same operations:
- `syncFromIntersection(intersection)` — initialises state from DB entity
- `setPhase(id, phase, lockedUntilMs)` — phase with optional lock timestamp
- `setVehicleCount(id, count)`
- `setDurations(id, green, red)`
- `getLive(id)` → `Optional<LiveSignalDto>`
- `getAllLive()` → `List<LiveSignalDto>`

Switching backends requires only changing the active Spring profile — no code changes.

---

## 🔧 Configuration Reference

### `src/main/resources/application.yml` (Production)

```yaml
spring:
  application:
    name: smart-traffic
  datasource:
    url: jdbc:mysql://localhost:3306/smart_traffic?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: smartuser
    password: smartpass
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
  data:
    redis:
      host: localhost
      port: 6379
  jackson:
    serialization:
      write-dates-as-timestamps: false

server:
  port: 8080

app:
  jwt:
    secret: "ChangeThisToASecretKeyAtLeast256BitsLongForHS256Algorithm!!"
    expiration-ms: 86400000         # 24 hours
  signal:
    base-green-seconds: 25
    bonus-seconds-per-threshold: 5
    vehicle-threshold: 10
    min-green-seconds: 15
    max-green-seconds: 90
    default-red-seconds: 30
    violation-wait-seconds: 120
  emergency:
    hold-seconds: 60
```

### `src/main/resources/application-local.yml` (Development)

```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
      - org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration
  datasource:
    url: jdbc:h2:mem:smarttraffic;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect
    show-sql: false
```

### `docker-compose.yml`

```yaml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: smart_traffic
      MYSQL_USER: smartuser
      MYSQL_PASSWORD: smartpass
      MYSQL_ROOT_PASSWORD: rootpass
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  mysql_data:
```

---

## 🗄️ Database Schema

```
┌─────────────────────────┐         ┌─────────────────────────┐
│       intersection       │         │       road_segment       │
├─────────────────────────┤         ├─────────────────────────┤
│ id           BIGINT  PK │◄────────│ id           BIGINT  PK │
│ name         VARCHAR    │◄────────│ from_id      BIGINT  FK │
│ latitude     DOUBLE     │         │ to_id        BIGINT  FK │
│ longitude    DOUBLE     │         │ base_travel_ INT         │
│ lane_count   INT        │         │   seconds                │
│ is_main_road BOOLEAN    │         └─────────────────────────┘
│ current_phase ENUM      │
│ green_duration INT      │         ┌─────────────────────────┐
│ red_duration  INT       │         │       traffic_log        │
│ vehicle_count INT       │         ├─────────────────────────┤
└─────────────────────────┘         │ id           BIGINT  PK │
                                    │ intersection_id BIGINT FK│
┌─────────────────────────┐         │ vehicle_count INT        │
│          user            │         │ green_duration INT       │
├─────────────────────────┤         │ red_duration  INT        │
│ id       BIGINT      PK │         │ logged_at    INSTANT     │
│ username VARCHAR         │         └─────────────────────────┘
│ password VARCHAR (BCrypt)│
│                          │         ┌─────────────────────────┐
│   (M:N with role)        │         │     emergency_event      │
└─────────────────────────┘         ├─────────────────────────┤
                                    │ id           BIGINT  PK │
┌─────────────────────────┐         │ active       BOOLEAN     │
│          role            │         │ route_ids    JSON        │
├─────────────────────────┤         │ ends_at      INSTANT     │
│ id   BIGINT          PK │         └─────────────────────────┘
│ name ENUM (ADMIN|VIEWER) │
└─────────────────────────┘         ┌─────────────────────────┐
                                    │       saved_route        │
                                    ├─────────────────────────┤
                                    │ id           BIGINT  PK │
                                    │ from_id      BIGINT      │
                                    │ to_id        BIGINT      │
                                    │ path_ids     JSON        │
                                    └─────────────────────────┘
```

---

## 🌏 Seed Data — Delhi NCR Intersections

All 16 intersections and 20 bidirectional road segments are seeded automatically on first startup by `DataInitializer`.

### Intersections

| ID | Name | Latitude | Longitude | Lanes | Main Road | Zone |
|---|---|---|---|---|---|---|
| 1 | ITO Crossing | 28.6280 | 77.2410 | 4 | ✅ | Central |
| 2 | Connaught Place | 28.6315 | 77.2167 | 4 | ✅ | Central |
| 3 | India Gate Circle | 28.6129 | 77.2295 | 3 | ✅ | Central |
| 4 | Chandni Chowk | 28.6506 | 77.2334 | 3 | ✅ | Central |
| 5 | AIIMS Crossing | 28.5672 | 77.2100 | 3 | ❌ | South |
| 6 | Hauz Khas Junction | 28.5494 | 77.2001 | 2 | ❌ | South |
| 7 | Nehru Place Flyover | 28.5491 | 77.2530 | 2 | ❌ | South |
| 8 | Rajouri Garden Metro | 28.6493 | 77.1215 | 3 | ✅ | West |
| 9 | Punjabi Bagh Chowk | 28.6682 | 77.1310 | 2 | ❌ | West |
| 10 | Rohini Sec-3 Signal | 28.7159 | 77.1166 | 3 | ✅ | North |
| 11 | Akshardham Crossing | 28.6127 | 77.2773 | 2 | ❌ | East |
| 12 | Noida Sec-18 Signal | 28.5700 | 77.3210 | 2 | ❌ | Noida |
| 13 | Moolchand Flyover | 28.5688 | 77.2375 | 3 | ✅ | Ring Road |
| 14 | Dhaula Kuan Junction | 28.5930 | 77.1660 | 2 | ❌ | Ring Road |
| 15 | Kashmere Gate ISBT | 28.6674 | 77.2295 | 2 | ❌ | Outer |
| 16 | Sarai Kale Khan | 28.5893 | 77.2571 | 3 | ✅ | Outer |

### Road Segments (bidirectional, 20 pairs)

| From | To | Base Travel (s) | Corridor |
|---|---|---|---|
| ITO ↔ Connaught Place | 1 ↔ 2 | 20 | Central arterial |
| Connaught ↔ Chandni Chowk | 2 ↔ 4 | 18 | North-Central |
| Connaught ↔ AIIMS | 2 ↔ 5 | 10 | South axis |
| ITO ↔ India Gate | 1 ↔ 3 | 25 | Ring Road |
| ITO ↔ Hauz Khas | 1 ↔ 6 | 15 | South corridor |
| AIIMS ↔ Nehru Place | 5 ↔ 7 | 12 | South-East |
| AIIMS ↔ Hauz Khas | 5 ↔ 6 | 14 | South inner |
| Chandni Chowk ↔ Moolchand | 4 ↔ 13 | 10 | Ring Road |
| Moolchand ↔ Dhaula Kuan | 13 ↔ 14 | 22 | Ring Road west |
| ITO ↔ Rajouri Garden | 1 ↔ 8 | 20 | West arterial |
| Rajouri ↔ Punjabi Bagh | 8 ↔ 9 | 12 | West |
| Punjabi Bagh ↔ Rohini | 9 ↔ 10 | 18 | North-West |
| Rajouri ↔ Sarai Kale Khan | 8 ↔ 16 | 10 | Outer ring |
| Sarai Kale Khan ↔ Kashmere Gate | 16 ↔ 15 | 8 | Outer North |
| Kashmere Gate ↔ Akshardham | 15 ↔ 11 | 14 | East |
| Akshardham ↔ Noida | 11 ↔ 12 | 16 | East → Noida |
| India Gate ↔ Akshardham | 3 ↔ 11 | 12 | East axis |
| Hauz Khas ↔ Akshardham | 6 ↔ 11 | 18 | South-East cross |
| Dhaula Kuan ↔ Rajouri | 14 ↔ 8 | 15 | West Ring Road |
| India Gate ↔ Noida | 3 ↔ 12 | 15 | East corridor |

---

## 🗺️ Roadmap

- [ ] **Real IoT sensor integration** — Replace simulation with hardware input via MQTT or WebSocket
- [ ] **ML-based prediction** — Forecast congestion 15–30 min ahead using historical `TrafficLog` data
- [ ] **Turn-restriction support** — Add prohibited turn metadata to `RoadSegment` for more accurate routing
- [ ] **Multi-city profiles** — Parameterize intersection datasets beyond Delhi NCR
- [ ] **Alert escalation** — Email/SMS dispatch when congestion score exceeds configurable threshold
- [ ] **User management UI** — Admin CRUD for users and role assignments
- [ ] **Audit log** — Track all manual overrides and emergency events with operator identity
- [ ] **Kubernetes deployment** — Helm chart for production cluster deployment

---

> Built with ❤️ for smarter, safer cities.
>
> **Stack:** Spring Boot 3 · Java 17 · Next.js 15 · React 19 · Leaflet · Recharts · STOMP WebSocket · Dijkstra · JWT · MySQL 8 · Redis 7 · Docker

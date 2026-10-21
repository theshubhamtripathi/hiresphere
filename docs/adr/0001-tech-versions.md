# 1. Pinned technology versions

- Status: accepted
- Date: 2026-10-22

## Context

HireSphere targets Spring Boot 4.x with a Spring Cloud microservice stack and a
React 19 frontend. Versions must be mutually compatible GA releases, verified at
project start rather than assumed.

## Decision

### Backend

| Component | Version | Notes |
|---|---|---|
| Java | 21 (LTS) | Boot 4 baseline is Java 17; 21 is the current LTS. Compiled with `maven.compiler.release=21`; the local JDK is 23 and is backward compatible. |
| Spring Boot | 4.0.8 | Latest 4.0.x GA (Aug 2026). |
| Spring Cloud | 2025.1.3 (Oakwood) | The release train compatible with Boot 4.0.x. Provides Eureka, Config, Gateway, OpenFeign, Resilience4j. |
| Spring AI | not used | No Boot-4-compatible GA (see ADR 0002). |
| jjwt | 0.12.6 | JWT create/parse. |
| MapStruct | 1.6.3 | DTO/entity mapping. |
| Lombok | managed by Boot BOM | |
| springdoc-openapi | 3.0.0 | OpenAPI 3 UI for Spring Framework 7 / Boot 4. |
| Flyway | managed by Boot BOM (11.x) | PostgreSQL migrations. |
| PostgreSQL driver | managed by Boot BOM | |
| Testcontainers | managed by Boot BOM (2.0.x) | Overridden only on incompatibility. |
| Redis (Spring Data) | managed by Boot BOM | |
| Spring Kafka | managed by Boot BOM | |

### Frontend

| Component | Version | Notes |
|---|---|---|
| React / React DOM | 19.2.x | |
| Vite | 7.x | Build tool / dev server. |
| Redux Toolkit | 2.13.x | Slices + async thunks. |
| react-redux | 9.x | |
| React Router | 7.x | |
| Axios | latest | JWT + refresh interceptors. |
| Tailwind CSS | 4.3.x | Utility layer. |
| MUI (Material UI) | 7.x | Works with Tailwind v4 via the `@layer` directive (MUI 7.1+). See ADR 0003. |
| Formik + Yup | latest | Forms + validation. |
| Recharts | latest | Dashboard charts. |
| react-hot-toast | latest | Toasts. |

### Infrastructure

PostgreSQL 17, Redis 7, Apache Kafka (KRaft, single node), Mailpit, kafka-ui
(tools profile). All via Docker Compose.

## Consequences

Dependency versions are centrally managed by the Spring Boot and Spring Cloud
BOMs wherever possible; only libraries outside those BOMs (jjwt, MapStruct,
springdoc) are pinned explicitly in the root POM. The frontend pins are applied
in `frontend/package.json`.

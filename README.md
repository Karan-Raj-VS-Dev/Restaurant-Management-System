# Restaurant Management System

This repository contains the backend scaffold, frontend SPAs, and architecture documentation for a restaurant management platform built as multiple microservices with multiple role-based UIs.

## Final Tech Stack

### Frontend

- `React.js` for all UI components
- `Vite` for the current React.js SPA implementation
- `Next.js` as the planned framework target for future expansion
- `TypeScript`
- `Tailwind CSS`
- `TanStack Query`
- `WebSockets` or `SSE` for live updates
- `React Hook Form + Zod`

### Backend

- `Java 21` target architecture, scaffold currently compatible with local `Java 17`
- `Spring Boot`
- `REST APIs`
- `Shared domain event contracts`
- `HTTP event gateway for local event transport`
- `Camunda 8` planned for orchestration

### Data and Observability

- `PostgreSQL`
- `MongoDB`
- `Redis`
- `OpenTelemetry`
- `Prometheus + Grafana`
- `Jaeger` or `Tempo`

## Services

- `auth-service`
- `customer-service`
- `employee-service`
- `property-service`
- `event-gateway-service`
- `table-service`
- `catalog-service`
- `inventory-service`
- `order-service`
- `kitchen-service`
- `billing-service`
- `payment-service`
- `takeaway-service`
- `marketplace-integration-service`
- `audit-timeline-service`
- `review-feedback-service`
- `reporting-service`
- `operations-insights-service`

## UI Applications

- `admin-ui` for superuser login, property management, and employee or user access management
- `restaurant-ui` for operational user login, property selection, and dashboard selection
- `pos-ui` for floor control and dine-in operations
- `kitchen-ui` for ticket and preparation flow
- `inventory-ui` for stock and threshold operations
- `employees-ui` for property employee management
- `property-settings-ui` for table, floor-section, recipe, ingredient, and billing configuration
- `reports-ui` for operational and finance-side reporting

## Auth And Admin Highlights

- usernames and passwords are stored in the platform database
- `auth-service` issues the authenticated session and secure auth cookie
- every backend service validates the platform JWT before serving business APIs
- admin and restaurant users sign in through separate in-app login pages
- the local seeded admin identity is provisioned as:
  - `kingChef / SUPER@secret45`
- admin CRUD calls no longer trust `X-Auth-Username`; they require a validated platform auth cookie
- newly created operational users can be forced through first-login password change
- the admin console does not open restaurant dashboards
- restaurant users first choose a mapped property and then choose a dashboard card
- property creation and employee or user access management stay in the admin console
- there is no public sign-up page in the platform SPAs; login, logout, password reset, and future MFA remain platform-owned

## Tenant And Property Routing

The project now uses a hierarchical tenant/property convention:

- product slug: `chefy`
- default tenant id: `bikini-bottom`
- starter property: `krusty-krab`

The local starter business seed is:

- tenant/company: `Bikini Bottom`
- property/outlet: `Krusty Krab`
- seeded admin mapping: `kingChef -> bikini-bottom -> krusty-krab`

Canonical service URLs follow this pattern:

- tenant-scoped routes: `/{productSlug}/tenant/{tenantId}/api/...`
- property-scoped routes: `/{productSlug}/tenant/{tenantId}/property/{propertyId}/api/...`

Example operational route:

- `/chefy/tenant/bikini-bottom/property/krusty-krab/api/orders`

## Main Flow Services

The first implementation path is centered on:

- `property-service`
- `table-service`
- `employee-service`
- `catalog-service`
- `inventory-service`
- `order-service`
- `kitchen-service`
- `billing-service`
- `payment-service`
- `review-feedback-service`

## Dine-In Table Lifecycle

The dine-in table cycle now follows a fuller operational loop:

- `AVAILABLE`: the table is ready for a new walk-in
- `UNAVAILABLE`: the table is blocked from property settings and cannot be seated from the diner UI
- `RESERVED`: stores reservation party size and reservation time
- `OCCUPIED`: requires a valid guest count and assigned server, and validates the table capacity
- `NEEDS_CLEANING`: can be triggered immediately or scheduled after payment

Current diner workflow details:

- the floor-control UI filters by `floor` and `section`
- only waiters assigned to that floor-section appear in the server selector
- occupying a table validates the guest count against the configured table capacity
- reserved tables warn the user when the reservation is within `30 minutes`, and the operator can explicitly override
- after payment, the table is scheduled to move to `NEEDS_CLEANING` after `120 seconds`
- cleaners assigned to the same floor-section can be selected to return a table to `AVAILABLE`
- the standard cleaner cycle returns the table to `AVAILABLE` after `300 seconds`, with an immediate override still available in the UI

## Documentation Map

- [ARCHITECTURE-SUMMARY.md](./ARCHITECTURE-SUMMARY.md)
- [EVENT-DRIVEN-ARCHITECTURE.md](./EVENT-DRIVEN-ARCHITECTURE.md)
- [SERVICE-CATALOG.md](./SERVICE-CATALOG.md)
- [USE-CASE-DIAGRAM.md](./USE-CASE-DIAGRAM.md)
- [BPMN-DIAGRAMS.md](./BPMN-DIAGRAMS.md)
- [frontend/README.md](./frontend/README.md)
- [docs/auth-admin-flow.md](./docs/auth-admin-flow.md)
- [docs/main-flow.md](./docs/main-flow.md)
- [diagrams/dine-in-main-flow.bpmn](./diagrams/dine-in-main-flow.bpmn)
- [diagrams/marketplace-takeaway-flow.bpmn](./diagrams/marketplace-takeaway-flow.bpmn)

## Repository Structure

```text
restaurant-management-system/
  frontend/
    apps/admin-ui/
    apps/restaurant-ui/
  services/
    <service-name>/
  docs/
  diagrams/
  docker-compose.yml
```

## Build

Run from the parent folder:

```bash
mvn clean package
```

This project keeps [`.mvn/maven.config`](/Users/vkaran/Documents/New%20project/restaurant-management-system/.mvn/maven.config) minimal so Maven and IDEs use the standard local repository at `/Users/vkaran/.m2/repository`. That prevents service modules from resolving shared artifacts from a different repo than the Java language server.

## Local Runtime Notes

- PostgreSQL local dev port: `5433`
- `auth-service` port: `9001`
- `property-service` port: `9004`
- `admin-ui` port: `5175`
- `restaurant-ui` port: `5176`
- `pos-ui` port: `5173`
- `kitchen-ui` port: `5174`
- `inventory-ui` port: `5177`
- `employees-ui` port: `5178`
- `property-settings-ui` port: `5179`
- `reports-ui` port: `5180`

## Local Scripts

Application stack:

```bash
bash scripts/start-local-all-services.sh
bash scripts/status-local-all-services.sh
bash scripts/stop-local-all-services.sh
```

PostgreSQL local server:

```bash
bash scripts/start-local-postgres.sh
bash scripts/status-local-postgres.sh
bash scripts/stop-local-postgres.sh
```

The PostgreSQL scripts default to the local `Postgres.app` install at `~/Applications/Postgres.app`, the data directory at `~/Library/Application Support/Postgres/var-18`, and port `5433`. You can override those defaults with environment variables such as `POSTGRES_APP_BIN`, `PG_CTL_BIN`, `PGDATA`, `PGPORT`, and `PGLOG`.

The local auth flow is platform-owned: usernames and passwords live in the project database, `auth-service` issues the session cookie, and backend services validate the signed token carried in that cookie.

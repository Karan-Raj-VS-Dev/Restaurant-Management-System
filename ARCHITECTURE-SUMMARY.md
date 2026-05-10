# Architecture Summary

## Overview

This system is designed as a `microservice-based restaurant platform` with multiple dedicated `React.js` frontend applications and multiple `Spring Boot` backend services.

The architecture supports:

- dine-in restaurant operations
- kitchen operations
- takeaway operations
- third-party delivery integration
- staff and property management
- stock-aware menu exposure
- billing and payment
- audit tracking
- reporting and insights

## Frontend Architecture

All UI applications should be built with `React.js` so the full user experience stays component-driven and can support live operational updates.

Recommended React.js applications:

- `admin-ui`: standalone superuser console for property management and employee or user access control
- `restaurant-ui`: operational login, property selection, and dashboard selection
- `pos-ui`: waiter, cashier, and host operations
- `kitchen-ui`: kitchen ticket board and preparation updates
- `customer-ui`: QR menu, takeaway ordering, and review submission
- `analytics-ui`: reporting and operations dashboards

Recommended frontend stack:

- `React.js`
- `Vite` for the current implementation
- `Next.js` as the longer-term framework target
- `TypeScript`
- `Tailwind CSS`
- `TanStack Query`
- `WebSockets` or `SSE`
- `React Hook Form + Zod`

## Backend Architecture

The backend is split by business capability. Each service owns its domain and should eventually own its own persistence schema or database.

Core services:

- `auth-service`
- `customer-service`
- `employee-service`
- `property-service`
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

### Current Auth And Admin Layer

The current implementation now includes:

- platform-owned username/password login backed by the project database
- signed auth-cookie protection on backend APIs
- first-login password rotation inside the platform auth flow
- admin-only property management
- admin-only employee or user access management
- a separate restaurant application for operational users
- a restaurant landing page that routes to diner, inventory, kitchen, and report dashboards

The local seeded admin account is:

- `kingChef / SUPER@secret45`

## Communication Model

Use synchronous communication when a user action needs an immediate response:

- table assignment
- menu retrieval
- order creation
- bill finalization
- payment processing

Use asynchronous communication for cross-service propagation:

- order lifecycle events
- kitchen status updates
- stock adjustments
- payment completed events
- review request creation
- insight aggregation

Recommended transport split:

- `REST` for commands and direct reads
- `shared event envelope + event keys` for domain events
- `event-gateway-service` for local asynchronous event fan-out in development
- `Kafka` as the planned production broker replacement
- `Camunda 8` for orchestrated long-running flows

## Data Strategy

Use a `SQL-first` architecture because restaurant operations are highly transactional.

- `PostgreSQL` for orders, billing, payments, tables, customers, employees, properties, menu, recipes, and inventory
- `MongoDB` for audit history, reporting read models, and denormalized analytics views
- `Redis` for live state, availability caching, and high-speed operational reads

### Tenant And Property Hierarchy

The current local structure is organized like this:

- product slug: `chefy`
- tenant id: `bikini-bottom`
- property id: `krusty-krab`

Storage direction:

- `tenant_id` categorizes records by company tenant
- `property_id` categorizes the outlet or restaurant within that tenant
- operational tables should store both `tenant_id` and `property_id` where the data is property-scoped

Canonical route direction:

- tenant APIs: `/{productSlug}/tenant/{tenantId}/api/...`
- property APIs: `/{productSlug}/tenant/{tenantId}/property/{propertyId}/api/...`

## Live Update Strategy

All React.js UIs should support live operational updates for:

- table occupancy
- menu availability
- kitchen progress
- stock status
- payment status
- takeaway status

Recommended approach:

- `TanStack Query` for standard reads and cache invalidation
- `WebSockets` or `SSE` for event push to the React.js frontends

## Identity And Access Flow

1. Superuser logs in through `admin-ui`, while operational users log in through `restaurant-ui`.
2. The SPA submits the local username/password form to `auth-service`.
3. `auth-service` validates the internal user record, signs a JWT, and returns it through an `HttpOnly` cookie.
4. Every backend service validates that signed token before serving business APIs.
5. `GET /api/auth/session` resolves the internal tenant/property access record for the logged-in user.
6. Admin users can create employee or user access, which stores the operational identity directly in the platform database and maps it to one or more properties.
6. There is no public sign-up flow in the platform UIs.

## Main Dine-In Flow

1. Customer arrives and is assigned to a table.
2. Waiter/host gets assigned to the table.
3. Menu is displayed only for dishes whose recipe ingredients are available.
4. Waiter creates the order.
5. Kitchen receives and prepares the ticket.
6. Inventory reserves and issues ingredients.
7. Kitchen updates status in real time.
8. Waiter serves the food.
9. Bill is finalized.
10. Payment is completed.
11. Review request is created.
12. Table is cleaned and reset.

## Delivery And Integration Flow

1. External marketplace sends order.
2. `marketplace-integration-service` validates and maps the payload.
3. `takeaway-service` creates the internal takeaway order.
4. Stock and kitchen operations continue internally.
5. Status is sent back to the marketplace.

## Analytics And Insights

`operations-insights-service` is a read-only analytics service. It should consume events from:

- `inventory-service`
- `catalog-service`
- `order-service`
- `table-service`
- `employee-service`
- `kitchen-service`
- `billing-service`
- `payment-service`
- `property-service`

It should provide:

- daily order counts
- busiest table
- top-performing server
- gross sales
- kitchen throughput
- low stock alerts
- out-of-stock alerts
- ingredient consumption patterns
- stock impact on menu availability

## Next Engineering Steps

1. Add persistence for the core dine-in services.
2. Add shared contracts for events and IDs.
3. Add API gateway and service discovery strategy.
4. Keep separating superuser and operational UI boundaries as more applications are added.
5. Replace the HTTP event gateway with Kafka plus outbox processing for production.
6. Add Docker Compose for local infrastructure.

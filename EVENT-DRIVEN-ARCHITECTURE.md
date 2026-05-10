# Event-Driven Architecture

## Goal

This codebase now uses a shared event-driven structure so services communicate by domain events instead of direct service-to-service orchestration for the main operational flow.

## Current Transport

Because broker libraries were not available in the current environment, the project uses an internal transport layer for now:

- publishers create a shared `EventEnvelope`
- publishers send that envelope to `event-gateway-service`
- `event-gateway-service` forwards the raw event to subscriber inbox endpoints
- consumer services receive the event on `/api/internal/events`
- each consumer filters by `eventKey` and updates its own local state

This keeps the system event-driven today while remaining easy to replace with Kafka later.

## Shared Event Structure

All events use the shared `platform-eventing` module.

Core types:

- `EventEnvelope<T>`
- `EventKeys`
- `AggregateTypes`
- domain payload contracts under `com.restaurant.platform.eventing.contract`

Envelope fields include:

- `eventId`
- `eventKey`
- `aggregateType`
- `aggregateId`
- `partitionKey`
- `producer`
- `occurredAt`
- `correlationId`
- `causationId`
- `payload`

For property-scoped restaurant operations, the payload contracts also carry:

- `tenantId`
- `propertyId`

That lets consumers build correct per-property projections inside the same tenant.

## Event Keys

Current event keys:

- `table.assigned.v1`
- `table.status-changed.v1`
- `order.created.v1`
- `order.submitted-to-kitchen.v1`
- `kitchen.ticket-created.v1`
- `kitchen.status-updated.v1`
- `bill.drafted.v1`
- `bill.finalized.v1`
- `payment.succeeded.v1`
- `payment.failed.v1`
- `review.requested.v1`
- `review.submitted.v1`
- `stock.reserved.v1`
- `stock.low.v1`
- `stock.out.v1`
- `marketplace.order-received.v1`
- `takeaway.order-created.v1`

## Main Event Flow

### Dine-In

1. `table-service` emits table assignment and status changes.
2. `order-service` emits `order.created.v1`.
3. `billing-service` consumes that event and creates a draft bill.
4. `order-service` emits `order.submitted-to-kitchen.v1`.
5. `kitchen-service` consumes it and creates a kitchen ticket.
6. `inventory-service` consumes it and reserves ingredients.
7. `inventory-service` emits stock reservation and stock alert events.
8. `payment-service` emits `payment.succeeded.v1`.
9. `review-feedback-service` consumes payment success and creates a review request.
10. `audit-timeline-service` consumes all events and stores the timeline.
11. `operations-insights-service` consumes operational events and builds analytics views.

Every operational event in this flow is scoped to a single:

- product slug `chefy`
- `tenantId`
- `propertyId`

### Marketplace

1. `marketplace-integration-service` emits `marketplace.order-received.v1`.
2. `takeaway-service` consumes it and creates an internal takeaway order.
3. `takeaway-service` emits `takeaway.order-created.v1`.

## Database Strategy For Event-Driven Writes

For the current scaffold:

- services update their own local state first
- services emit a domain event after the local write

Database reads and projections should use the same scope:

- `tenant_id`
- `property_id`

For production, the recommended pattern is:

1. write business data and outbox event in the same local transaction
2. publish from the outbox asynchronously
3. mark outbox records as delivered

That is the right way to make database writes event-driven safely without cross-service distributed transactions.

## Production Upgrade Path

The business contracts in `platform-eventing` should stay the same. The transport can be upgraded from `event-gateway-service` to Kafka by:

1. replacing `HttpDomainEventPublisher` with a broker-backed publisher
2. replacing subscriber inbox controllers with broker consumers
3. adding outbox tables and outbox processors
4. adding retries and dead-letter queues

## Current Consumer Services

- `kitchen-service`
- `billing-service`
- `inventory-service`
- `takeaway-service`
- `review-feedback-service`
- `audit-timeline-service`
- `operations-insights-service`

# marketplace-integration-service

## Purpose

`marketplace-integration-service` is the anti-corruption layer for third-party ordering channels such as Swiggy and Zomato.

## Default Port

- `9013`

## Current APIs

- `POST /chefy/tenant/{tenantId}/property/{propertyId}/api/integrations/marketplace/orders`

## Current Scope

- incoming marketplace order scoped to tenant and property
- provider and external order mapping placeholder

## Planned Responsibilities

- webhook validation
- external-to-internal payload mapping
- idempotency and retry handling
- menu and status sync to marketplace channels
- raw payload archival
- forwarding accepted orders to `takeaway-service`

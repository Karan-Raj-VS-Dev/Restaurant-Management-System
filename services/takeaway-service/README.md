# takeaway-service

## Purpose

`takeaway-service` owns the internal lifecycle for takeaway and delivery-friendly restaurant orders.

## Default Port

- `9012`

## Current APIs

- `POST /chefy/tenant/{tenantId}/property/{propertyId}/api/takeaway/orders`
- `GET /chefy/tenant/{tenantId}/property/{propertyId}/api/takeaway/orders/{takeawayOrderId}`

## Current Scope

- property-scoped takeaway order creation
- property-scoped takeaway status lookup
- marketplace-originated takeaway flow support

## Planned Responsibilities

- takeaway order persistence
- preparation and handoff lifecycle
- direct customer pickup workflows
- delivery status coordination
- linkage to billing, payment, and kitchen

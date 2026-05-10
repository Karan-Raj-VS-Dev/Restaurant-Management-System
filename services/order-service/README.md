# order-service

## Purpose

`order-service` is the center of the dine-in order flow. It owns order creation and core order lifecycle transitions.

## Default Port

- `8088`

## Current APIs

- `POST /api/orders`
- `GET /api/orders/{orderId}`
- `PATCH /api/orders/{orderId}/submit-to-kitchen`

## Current Scope

- dine-in order creation placeholder
- sample order lookup
- submit-to-kitchen transition placeholder

## Planned Responsibilities

- order persistence
- split and merge order handling
- item add/remove flows
- state machine for dine-in and takeaway
- event publication to kitchen, billing, and analytics
- waiter and table correlation

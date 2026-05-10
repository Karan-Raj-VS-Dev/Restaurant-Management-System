# inventory-service

## Purpose

`inventory-service` is the source of truth for ingredient stock, stock health, and reservation of items needed for food preparation.

## Default Port

- `8087`

## Current APIs

- `GET /api/inventory/stock`
- `GET /api/inventory/availability/menu-items`
- `POST /api/inventory/ingredients/reserve`

## Current Scope

- stock snapshot placeholder
- menu availability projection placeholder
- ingredient reservation placeholder

## Planned Responsibilities

- ingredient stock persistence
- stock add, adjust, reserve, consume, release flows
- low-stock and out-of-stock alerts
- outlet-specific inventory handling
- expiry and wastage tracking
- event publishing for analytics and menu visibility

# billing-service

## Purpose

`billing-service` owns draft bill creation, tax calculation, and final bill generation.

## Default Port

- `8090`

## Current APIs

- `POST /api/bills/draft`
- `POST /api/bills/{billId}/finalize`
- `GET /api/bills/{billId}`

## Current Scope

- draft bill placeholder
- bill finalization placeholder
- sample subtotal, tax, and total calculation

## Planned Responsibilities

- bill persistence
- tax and discount policies
- split bill support
- service charge handling
- billing state machine
- event publication for payment and reporting

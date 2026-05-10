# table-service

## Purpose

`table-service` owns table lifecycle, occupancy state, reservation timing, cleaner handoff, and reset status for dine-in operations.

## Default Port

- `9005`

## Current APIs

- `GET /api/tables`
- `POST /api/tables/assign`
- `PATCH /api/tables/{tableId}/status`
- `PATCH /api/tables/{tableId}/needs-cleaning`
- `PATCH /api/tables/{tableId}/available`

## Current Scope

- table listing by property
- table assignment and occupancy validation
- reservation timing and override warning support
- scheduled `OCCUPIED -> NEEDS_CLEANING` transition after payment
- scheduled `NEEDS_CLEANING -> AVAILABLE` transition after cleaner assignment
- support for `UNAVAILABLE` from property settings

## Planned Responsibilities

- live table board
- deeper reservation and walk-in support
- persistent table sessions
- waiter association
- table cleaning SLA metrics
- event publishing for table status changes

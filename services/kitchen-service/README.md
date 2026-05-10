# kitchen-service

## Purpose

`kitchen-service` manages kitchen tickets and live food preparation status.

## Default Port

- `8089`

## Current APIs

- `GET /api/kitchen/tickets`
- `POST /api/kitchen/tickets`
- `PATCH /api/kitchen/tickets/{ticketId}/accept`
- `PATCH /api/kitchen/tickets/{ticketId}/ready`

## Current Scope

- ticket list placeholder
- ticket creation placeholder
- ticket accept and ready transitions

## Planned Responsibilities

- kitchen ticket persistence
- prep queue management
- cook assignment
- status streaming to React.js UIs
- timing metrics for preparation
- integration with inventory reservations and recipe lookups

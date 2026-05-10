# audit-timeline-service

## Purpose

`audit-timeline-service` stores operational history so the platform can reconstruct status changes and investigate problems.

## Default Port

- `9014`

## Current APIs

- `GET /chefy/tenant/{tenantId}/property/{propertyId}/api/audit/events`

## Current Scope

- tenant/property-scoped audit event lookup
- scope-aware event timeline capture

## Planned Responsibilities

- append-only event persistence
- order, bill, payment, and table history views
- troubleshooting support
- compliance-friendly retention
- operator-visible audit timelines in admin tools

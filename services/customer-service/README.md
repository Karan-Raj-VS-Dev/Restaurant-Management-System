# customer-service

## Purpose

`customer-service` manages customer profile data for dine-in and takeaway journeys.

## Default Port

- `9002`

## Current APIs

- `GET /chefy/tenant/{tenantId}/property/{propertyId}/api/customers`
- `GET /chefy/tenant/{tenantId}/property/{propertyId}/api/customers/{customerId}`
- `POST /chefy/tenant/{tenantId}/property/{propertyId}/api/customers`

## Current Scope

- tenant/property-scoped customer list endpoint
- tenant/property-scoped customer creation endpoint
- scoped walk-in customer retrieval

## Planned Responsibilities

- customer profile persistence
- loyalty membership
- visit history
- dining preferences
- saved delivery addresses
- marketing consent flags

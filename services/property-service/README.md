# property-service

## Purpose

`property-service` owns outlet and property metadata such as location, naming, and restaurant-level configuration.

## Default Port

- `9004`

## Current APIs

- `GET /api/properties`
- `GET /chefy/tenant/{tenantId}/api/properties`
- `GET /api/properties/{propertyId}`
- `GET /chefy/tenant/{tenantId}/api/properties/{propertyId}`
- `POST /chefy/tenant/{tenantId}/api/properties`
- `PUT /chefy/tenant/{tenantId}/api/properties/{propertyId}`
- `DELETE /chefy/tenant/{tenantId}/api/properties/{propertyId}`

## Current Scope

- tenant-aware sample property list
- property lookup placeholder
- default seeded tenant `bikini-bottom`
- default product slug `chefy`
- starter property `krusty-krab`

## Planned Responsibilities

- outlet persistence
- regional configuration
- tax and operating-hour defaults
- property-level menu and inventory scoping
- seat and floor metadata

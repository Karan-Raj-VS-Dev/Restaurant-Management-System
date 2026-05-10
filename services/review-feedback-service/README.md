# review-feedback-service

## Purpose

`review-feedback-service` owns customer ratings and feedback after order completion.

## Default Port

- `9015`

## Current APIs

- `POST /chefy/tenant/{tenantId}/property/{propertyId}/api/reviews`
- `GET /chefy/tenant/{tenantId}/property/{propertyId}/api/reviews/requests/{billId}`

## Current Scope

- property-scoped review submission
- bill-linked review request lookup
- star rating and comments capture

## Planned Responsibilities

- review persistence
- bill-linked review request generation
- customer, food, and service rating categories
- complaint escalation hooks
- analytics input for customer satisfaction metrics

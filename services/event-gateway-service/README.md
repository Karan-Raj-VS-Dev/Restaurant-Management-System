# event-gateway-service

## Purpose

`event-gateway-service` is the lightweight event transport for the current development environment. It accepts a domain event once and forwards it to subscriber service inbox endpoints.

## Default Port

- `8098`

## Current APIs

- `POST /api/events`

## Current Scope

- accepts raw event envelopes
- forwards them to configured subscriber endpoints
- supports the shared event-driven contract without requiring an external broker

## Planned Responsibilities

- replaceable transport layer before moving to Kafka or another broker
- retry and dead-letter handling
- delivery tracing
- routing by event key instead of broad fan-out

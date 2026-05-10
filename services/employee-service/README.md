# employee-service

## Purpose

`employee-service` manages restaurant staff metadata such as role, availability, and assignment lookup.

## Default Port

- `8083`

## Current APIs

- `GET /api/employees`
- `GET /api/employees/{employeeId}`
- `GET /api/employees/waiters/next`
- `GET /api/employees/cooks/next`

## Current Scope

- sample employee listing
- waiter assignment placeholder
- cook assignment placeholder

## Planned Responsibilities

- staff persistence
- shift roster management
- role eligibility rules
- server and cook assignment policies
- cleaner and cashier lookup
- performance aggregation input for analytics

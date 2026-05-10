# auth-service

## Purpose

`auth-service` is the identity boundary for the platform. It handles local username/password authentication, employee or user access administration, and the internal tenant/property mapping that sits behind authenticated identities.

## Current Scope

- local username/password login through `POST /api/auth/login`
- authenticated session resolution through `GET /api/auth/session`
- platform-owned first-login password rotation
- seeded admin user creation on startup
- admin CRUD for employee or user access

## Default Port

- `9001`

## Current APIs

- `GET /api/auth/session`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `POST /api/auth/password-reset/request`
- `POST /api/auth/password-reset/confirm`
- `POST /api/auth/password-change`
- `GET /api/auth/admin/users`
- `POST /api/auth/admin/users`
- `PUT /api/auth/admin/users/{userId}`
- `DELETE /api/auth/admin/users/{userId}`

## Local Seeded Admin Identity

- Username: `kingChef`
- Password: `SUPER@secret45`

## Main Tables

- `auth.app_users`
- `auth.user_property_access`

## Security Notes

- the SPAs sign in through local form POSTs owned by the platform
- every backend service validates the signed auth cookie through the shared `platform-security` module
- usernames and passwords stay in `auth.app_users`
- the current local security model uses a signed JWT in an `HttpOnly` cookie for browser-to-service requests

## Planned Responsibilities

- authenticated session lookup
- login, logout, and password-reset policy handling
- role and permission lookup
- tenant and property-level access control

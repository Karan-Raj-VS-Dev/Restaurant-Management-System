# Auth, Admin, And Restaurant UI

This document describes the split between the standalone admin console and the separate restaurant application, along with the auth and tenant access rules that connect them.

## Current Scope

The platform now includes:

- a single-page `admin-ui` React SPA
- a separate single-page `restaurant-ui` React SPA
- platform-owned username/password login
- signed auth-cookie validation on backend APIs
- first-login temporary-password rotation inside the platform flow
- admin-only tenant property management
- admin-only employee or user access management
- property selection after login
- property-aware dashboard selection after property selection
- restaurant dashboard-card navigation for operational users

There is intentionally **no public sign-up page**.

## Seeded Admin User

The local platform auth tables are seeded with a default admin identity:

- Username: `kingChef`
- Password: `SUPER@secret45`

Tenant routing defaults used in the current local setup:

- product slug: `chefy`
- tenant id: `bikini-bottom`
- property id: `krusty-krab`
- auth route base: `/chefy/tenant/bikini-bottom/api/auth`

This identity is represented in the internal auth mapping in [`Setup/Database/10-auth-service.sql`](../Setup/Database/10-auth-service.sql).

## Login Split

Admin and operational users now log in through different pages:

- Admin console: [`frontend/apps/admin-ui`](../frontend/apps/admin-ui)
- Restaurant application: [`frontend/apps/restaurant-ui`](../frontend/apps/restaurant-ui)

### Admin Login

The admin sign-in entry is implemented in [`frontend/apps/admin-ui/src/components/AuthFlow.tsx`](../frontend/apps/admin-ui/src/components/AuthFlow.tsx).

Behavior:

- submits the local username/password form
- receives the platform auth cookie from `auth-service`
- calls `GET /api/auth/session` with browser credentials
- rejects non-admin accounts
- keeps the admin inside the standalone control console

### Restaurant Login

The restaurant sign-in entry is implemented in [`frontend/apps/restaurant-ui/src/components/RestaurantAuthFlow.tsx`](../frontend/apps/restaurant-ui/src/components/RestaurantAuthFlow.tsx).

Behavior:

- submits the local username/password form
- receives the platform auth cookie from `auth-service`
- calls `GET /api/auth/session` with browser credentials
- rejects admin accounts
- handles first-login password rotation inside the SPA flow
- continues into property selection for mapped operational users

## Property Selection Flow

After restaurant-user login, the user is taken to the property-selection page implemented in [`frontend/apps/restaurant-ui/src/components/RestaurantPropertySelectionPage.tsx`](../frontend/apps/restaurant-ui/src/components/RestaurantPropertySelectionPage.tsx).

Behavior:

- only mapped operational properties are shown
- selecting a property stores the runtime scope as:
  - product slug
  - tenant id
  - property id

This keeps property selection and dashboard selection inside the restaurant SPA without mixing it into the admin console.

## Password Reset And First Login

Current direction:

1. user signs in through the local SPA form
2. if the account is marked `must_change_password`, the SPA collects the current temporary password and the new password
3. `auth-service` updates the password in the platform database
4. once the auth cookie is issued, the platform resolves the mapped tenant/property access through `GET /api/auth/session`

For the local scaffold, the legacy password-reset endpoints still exist in `auth-service`, but the SPAs no longer use them as the primary sign-in path.

## First Login Password Change

Operational users created by the admin console receive a temporary password in the platform database.

Behavior:

- admin creates the user in the admin console
- the user is stored in `auth.app_users`
- the user gets the internal `must_change_password` flag
- after the first successful password update, the platform clears the internal `must_change_password` marker

## Restaurant Dashboard Selection

After a property is selected, the user reaches the dashboard-card landing page implemented in [`frontend/apps/restaurant-ui/src/components/RestaurantLandingPage.tsx`](../frontend/apps/restaurant-ui/src/components/RestaurantLandingPage.tsx).

Current landing cards:

- Diner dashboard
- Inventory dashboard
- Kitchen dashboard
- Reports dashboard

The restaurant landing page intentionally does **not** include:

- property management
- employee or user creation
- employee management

Those are owned by the admin console only.

## Admin Console

The admin console is now a standalone SPA. After login, it stays on control-center views only and does not route into restaurant dashboards.

### Employee / User Access Page

The employee or user access experience is implemented in [`frontend/apps/admin-ui/src/components/AdminUsersView.tsx`](../frontend/apps/admin-ui/src/components/AdminUsersView.tsx).

Current capabilities:

- list employee or user accounts
- show access-count cards at the top
- add employee or user access
- edit employee or user access
- delete employee or user access
- map one or more properties to a user
- create temporary password
- capture country code + phone number
- store email
- store address
- capture latitude/longitude from browser geolocation

Layout behavior:

- main dashboard table uses the larger content area
- side panel uses roughly 30% of the page on desktop
- side panel contains add/edit/delete interactions

### Property Control Page

The property-management experience is implemented in [`frontend/apps/admin-ui/src/components/AdminPropertiesView.tsx`](../frontend/apps/admin-ui/src/components/AdminPropertiesView.tsx).

Current capabilities:

- list all tenant properties
- show total, active, and geo-tagged property counts
- add property
- edit property
- delete property
- store property location and geolocation
- keep the property table visible while the side panel takes roughly 30% of the page

## Employee / User Fields

The admin console currently manages these operational-user details:

- first name
- last name
- username
- temporary password
- phone country code
- phone number
- email
- address
- mapped properties
- latitude
- longitude
- status

## Auth-Service API

Implemented endpoints:

- `GET /api/auth/session`
- `GET /api/auth/admin/users`
- `POST /api/auth/admin/users`
- `PUT /api/auth/admin/users/{userId}`
- `DELETE /api/auth/admin/users/{userId}`
- `GET /api/properties`
- `POST /api/properties`
- `PUT /api/properties/{propertyId}`
- `DELETE /api/properties/{propertyId}`

Canonical tenant-scoped equivalents:

- `GET /chefy/tenant/{tenantId}/api/auth/session`
- `GET /chefy/tenant/{tenantId}/api/auth/admin/users`

The admin CRUD endpoints now require a valid platform auth cookie from an authenticated admin session.

## Database Model

Auth data lives in the `auth` schema inside `restaurantManagementSystem`.

Primary tables involved in this feature:

- `auth.app_users`
- `auth.user_property_access`
- `auth.password_reset_otps`
- `property.tenants`
- `property.properties`

Key auth columns in `auth.app_users`:

- `tenant_id`
- `property_id`
- `username`
- `email`
- `password`
- `first_name`
- `last_name`
- `phone_country_code`
- `phone_number`
- `phone_e164`
- `address_line`
- `latitude`
- `longitude`
- `status`
- `admin_user`
- `must_change_password`
- `last_login_at`

Key mapping columns in `auth.user_property_access`:

- `user_id`
- `tenant_id`
- `property_id`

## Local Seed Data

The current local seed data is designed for quick testing:

- tenant: `Bikini Bottom`
- tenant id: `bikini-bottom`
- property: `Krusty Krab`
- property id: `krusty-krab`
- seeded admin mapping: `kingChef -> bikini-bottom -> krusty-krab`

## Frontend Entry Points

- Admin console: [`frontend/apps/admin-ui`](../frontend/apps/admin-ui) -> `http://127.0.0.1:5175`
- Restaurant application: [`frontend/apps/restaurant-ui`](../frontend/apps/restaurant-ui) -> `http://127.0.0.1:5176`

## Notes

- The current frontend implementation uses `React + Vite` for lightweight SPA delivery.
- The earlier architecture documents still describe `Next.js` as a strategic target; the current implementation is intentionally simpler for rapid local development.
- Plain password storage is now being used because of the current local requirement. This should be reverted to hashed storage before any production deployment.

# Frontend Workspace

This workspace contains the first React.js frontend monorepo for the restaurant platform.

## Apps

- `@restaurant/admin-ui`: standalone superuser console for property management and employee or user access creation
- `@restaurant/restaurant-ui`: operational login, mapped-property selection, and dashboard selection
- `@restaurant/pos-ui`: POS and floor-control UI for hosts, waiters, and cashiers
- `@restaurant/kitchen-ui`: kitchen ticket board and stock health UI
- `@restaurant/inventory-ui`: dedicated inventory dashboard
- `@restaurant/employees-ui`: dedicated employee dashboard
- `@restaurant/property-settings-ui`: dedicated property-settings workspace
- `@restaurant/reports-ui`: dedicated reporting dashboard

## Admin UI Highlights

The `admin-ui` SPA now includes:

- platform-owned username/password login
- admin-only access enforcement after token validation
- property management
- employee or user access management
- property-to-user mapping

## Restaurant UI Highlights

The `restaurant-ui` SPA now includes:

- platform-owned username/password login
- rejection of admin accounts
- property-mapped operational access after token validation
- mapped-property selection
- dashboard cards that launch the split operational UIs

## Operational Dashboard Split

The restaurant launcher now routes into focused dashboards on separate local ports:

- `pos-ui` for live floor control and table lifecycle management
- `kitchen-ui` for expedite lanes and live ingredient stock health
- `inventory-ui` for stock threshold management
- `employees-ui` for property employee management
- `property-settings-ui` for tables, floor-sections, recipes, ingredients, supplies, taxes, and templates
- `reports-ui` for reporting

The split dashboards do not persist their own long-lived local login state. They require a valid platform cookie and a launcher handoff from `restaurant-ui`.

Seeded local admin credentials:

- Username: `kingChef`
- Password: `SUPER@secret45`

The browser login flow remains inside the SPAs. The backend issues an authenticated cookie, and the shared API package always calls backend services with browser credentials included.

## Packages

- `@restaurant/ui`: shared React.js components and styling tokens
- `@restaurant/api`: shared API client and domain types

## Development

Install dependencies:

```bash
npm install
```

Run the POS UI:

```bash
npm run dev:pos
```

Run the admin UI:

```bash
npm run dev:admin
```

Run the restaurant UI:

```bash
npm run dev:restaurant
```

Run the kitchen UI:

```bash
npm run dev:kitchen
```

Run the inventory UI:

```bash
npm run dev:inventory
```

Run the employee UI:

```bash
npm run dev:employees
```

Run the property settings UI:

```bash
npm run dev:property-settings
```

Run the reports UI:

```bash
npm run dev:reports
```

Build all apps:

```bash
npm run build
```

## Backend Assumptions

The frontend apps are wired to the local Spring Boot services through Vite development proxies. They are refreshable and live-update-ready through repeated polling, and can later be upgraded to SSE or WebSockets without changing the screen structure.

Current local frontend ports:

- `admin-ui` -> `5175`
- `restaurant-ui` -> `5176`
- `pos-ui` -> `5173`
- `kitchen-ui` -> `5174`
- `inventory-ui` -> `5177`
- `employees-ui` -> `5178`
- `property-settings-ui` -> `5179`
- `reports-ui` -> `5180`

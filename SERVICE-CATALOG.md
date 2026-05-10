# Service Catalog

## Domain Services

| Service | Responsibility | Primary Data | Current Starter API |
| --- | --- | --- | --- |
| `auth-service` | Local authentication, authenticated session lookup, and employee or user access administration | PostgreSQL | `/chefy/tenant/{tenantId}/api/auth` |
| `customer-service` | Customer profiles and contact data | PostgreSQL | `/chefy/tenant/{tenantId}/property/{propertyId}/api/customers` |
| `employee-service` | Staff directory and role lookup | PostgreSQL | `/chefy/tenant/{tenantId}/property/{propertyId}/api/employees` |
| `property-service` | Outlet and property metadata | PostgreSQL | `/chefy/tenant/{tenantId}/api/properties` |
| `event-gateway-service` | Local event transport and fan-out | n/a | `/api/events` |
| `table-service` | Table occupancy and cleaning lifecycle | PostgreSQL, Redis | `/chefy/tenant/{tenantId}/property/{propertyId}/api/tables` |
| `catalog-service` | Menu items, pricing, recipes | PostgreSQL | `/chefy/tenant/{tenantId}/property/{propertyId}/api/menu/items` |
| `inventory-service` | Ingredient stock and availability | PostgreSQL, Redis | `/chefy/tenant/{tenantId}/property/{propertyId}/api/inventory` |
| `order-service` | Dine-in order lifecycle | PostgreSQL | `/chefy/tenant/{tenantId}/property/{propertyId}/api/orders` |
| `kitchen-service` | Kitchen tickets and prep status | PostgreSQL, Redis | `/chefy/tenant/{tenantId}/property/{propertyId}/api/kitchen/tickets` |
| `billing-service` | Draft and final bill calculation | PostgreSQL | `/chefy/tenant/{tenantId}/property/{propertyId}/api/bills` |
| `payment-service` | Payment processing and settlement | PostgreSQL | `/chefy/tenant/{tenantId}/property/{propertyId}/api/payments` |
| `takeaway-service` | Internal takeaway order lifecycle | PostgreSQL | `/chefy/tenant/{tenantId}/property/{propertyId}/api/takeaway/orders` |
| `marketplace-integration-service` | External channel integration | PostgreSQL, optional MongoDB | `/chefy/tenant/{tenantId}/property/{propertyId}/api/integrations/marketplace/orders` |
| `audit-timeline-service` | Status and history timeline | MongoDB | `/chefy/tenant/{tenantId}/property/{propertyId}/api/audit/events` |
| `review-feedback-service` | Ratings and customer comments | PostgreSQL | `/chefy/tenant/{tenantId}/property/{propertyId}/api/reviews` |
| `reporting-service` | Report-oriented summaries | MongoDB or reporting schema | `/chefy/tenant/{tenantId}/property/{propertyId}/api/reports/daily-summary` |
| `operations-insights-service` | Cross-service analytics and stock insights | MongoDB or analytics schema | `/chefy/tenant/{tenantId}/property/{propertyId}/api/insights` |

## Event Backbone

The current codebase uses:

- a shared `EventEnvelope`
- versioned `EventKeys`
- service-local `/api/internal/events` inbox endpoints
- `event-gateway-service` as the development transport

This keeps the business flow event-driven without blocking progress on external broker setup.

## Main Dine-In Flow Ownership

| Step | Owning Service |
| --- | --- |
| Table assignment | `table-service` |
| Waiter lookup | `employee-service` |
| Menu retrieval | `catalog-service` |
| Stock-aware visibility | `inventory-service` + `catalog-service` |
| Order creation | `order-service` |
| Kitchen ticket lifecycle | `kitchen-service` |
| Ingredient reservation | `inventory-service` |
| Bill generation | `billing-service` |
| Payment | `payment-service` |
| Review collection | `review-feedback-service` |
| Table cleanup state | `table-service` |

## Event Producers And Consumers

| Service | Produces | Consumes |
| --- | --- | --- |
| `inventory-service` | `StockReserved`, `StockConsumed`, `StockLow`, `StockOut` | `OrderCreated`, `KitchenTicketAccepted` |
| `order-service` | `OrderCreated`, `OrderSubmittedToKitchen`, `OrderClosed` | `TableAssigned`, `MenuValidated` |
| `kitchen-service` | `KitchenTicketCreated`, `KitchenStatusChanged`, `KitchenTicketReady` | `OrderSubmittedToKitchen`, `StockReserved` |
| `billing-service` | `BillDrafted`, `BillFinalized`, `BillPaid` | `OrderCreated`, `OrderServed` |
| `payment-service` | `PaymentSucceeded`, `PaymentFailed` | `BillFinalized` |
| `review-feedback-service` | `ReviewSubmitted` | `PaymentSucceeded` |
| `takeaway-service` | `TakeawayOrderCreated`, `TakeawayStatusChanged` | `MarketplaceOrderAccepted` |
| `marketplace-integration-service` | `MarketplaceOrderAccepted`, `MarketplaceStatusPushed` | external webhooks |
| `operations-insights-service` | analytics projections | all operational events |

## Suggested Frontend Mapping

| UI App | Main Users | Primary Backend Services |
| --- | --- | --- |
| `pos-ui` | host, waiter, cashier | property, table, employee, catalog, inventory, order, billing, payment |
| `kitchen-ui` | cook, kitchen supervisor | kitchen, inventory, catalog, order |
| `admin-ui` | superuser, tenant admin | auth, property |
| `restaurant-ui` | operational user | auth, property, inventory, reporting, operations insights |
| `customer-ui` | guest, takeaway customer | catalog, takeaway, review |
| `analytics-ui` | management | reporting, operations insights |

## Auth-Service Notes

`auth-service` now implements:

- `GET /api/auth/session`
- `GET /api/auth/admin/users`
- `POST /api/auth/admin/users`
- `PUT /api/auth/admin/users/{userId}`
- `DELETE /api/auth/admin/users/{userId}`

Important rules:

- users are created by admin flow only
- no UI sign-up page exists
- newly created operational users can be forced through first-login password rotation
- browser sign-in, logout, and password-policy actions stay inside the platform auth flow

## Current Default Scope

- product slug: `chefy`
- tenant id: `bikini-bottom`
- starter property: `krusty-krab`

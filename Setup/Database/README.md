# Database Setup

This folder contains the PostgreSQL setup pack for the restaurant management system.

## Layout

- `00-create-databases.sql` creates one PostgreSQL database per service
- `00-create-single-database.sql` creates the unified PostgreSQL database `restaurantManagementSystem`
- `10-*.sql` through `26-*.sql`, plus `14-event-gateway-service.sql`, contain the DDL for each service
- `apply-local.sh` applies the legacy database-per-service setup to the local PostgreSQL instance on `localhost:5433`
- `apply-single-database.sh` applies the single-database, schema-per-service setup to `restaurantManagementSystem` on `localhost:5433`

## Database Names

- `rms_auth_db`
- `rms_customer_db`
- `rms_employee_db`
- `rms_property_db`
- `rms_event_gateway_db`
- `rms_table_db`
- `rms_catalog_db`
- `rms_inventory_db`
- `rms_order_db`
- `rms_kitchen_db`
- `rms_billing_db`
- `rms_payment_db`
- `rms_takeaway_db`
- `rms_marketplace_integration_db`
- `rms_audit_db`
- `rms_review_db`
- `rms_reporting_db`
- `rms_operations_insights_db`

## Single Database Name

- `restaurantManagementSystem`

## Single Database Schema Mapping

- `auth-service` -> `auth`
- `customer-service` -> `customer`
- `employee-service` -> `employee`
- `property-service` -> `property`
- `event-gateway-service` -> `event_gateway`
- `table-service` -> `table_mgmt`
- `catalog-service` -> `catalog`
- `inventory-service` -> `inventory`
- `order-service` -> `ordering`
- `kitchen-service` -> `kitchen`
- `billing-service` -> `billing`
- `payment-service` -> `payment`
- `takeaway-service` -> `takeaway`
- `marketplace-integration-service` -> `marketplace_integration`
- `audit-timeline-service` -> `audit`
- `review-feedback-service` -> `review`
- `reporting-service` -> `reporting`
- `operations-insights-service` -> `operations_insights`

## Auth Schema Notes

The `auth` schema now stores both authentication and admin user-management data.

Main auth tables:

- `auth.app_users`
- `auth.user_property_access`
- `auth.password_reset_otps`

The local setup also seeds the default admin account directly into `auth.app_users`:

- Username: `kingChef`
- Password: `SUPER@secret45`
- Tenant: `bikini-bottom`
- Default property: `krusty-krab`

## Tenant And Property Categorization

The local default hierarchy is:

- product slug: `chefy`
- tenant id: `bikini-bottom`
- property id: `krusty-krab`

The `property` schema owns the company and outlet metadata:

- `property.tenants` stores company-level tenant records
- `property.properties` stores restaurant/outlet records under each tenant

Operational schemas now store `tenant_id` together with `property_id` for property-scoped records so data can be filtered by tenant first and property second.

The auth schema follows the same model:

- `auth.app_users.property_id` stores the user’s default property
- `auth.user_property_access` stores all property mappings available to the user

## How To Apply

Use the helper script:

```bash
bash Setup/Database/apply-local.sh
```

To use the new single-database setup:

```bash
bash Setup/Database/apply-single-database.sh
```

Make sure the local PostgreSQL server is running first. The project scripts folder includes:

```bash
bash scripts/start-local-postgres.sh
bash scripts/status-local-postgres.sh
bash scripts/stop-local-postgres.sh
```

Or apply manually with `psql` in this order:

1. Run `00-create-databases.sql` against a maintenance database such as `postgres`
2. Run the service-specific DDL file against its matching database

## Notes

- Primary keys automatically create PostgreSQL indexes
- Additional secondary indexes are included for common lookup and event-driven processing paths
- Each service schema includes `outbox_events` and `inbox_events` tables to support event-driven integration
- The single-database setup is now the recommended default for local development
- The auth schema includes `must_change_password`, phone, email, address, and location fields for tenant user control
- The auth schema currently uses a plain `password` column because of the current local-development requirement
- Starter property row is seeded for `krusty-krab` under tenant `bikini-bottom`

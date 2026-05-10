#!/usr/bin/env bash
set -euo pipefail

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
PSQL_BIN="${PSQL_BIN:-$HOME/Applications/Postgres.app/Contents/Versions/latest/bin/psql}"
PGHOST="${PGHOST:-localhost}"
PGPORT="${PGPORT:-5433}"
PGUSER="${PGUSER:-$USER}"
MAINTENANCE_DB="${MAINTENANCE_DB:-postgres}"

if [[ ! -x "$PSQL_BIN" ]]; then
  echo "psql not found at $PSQL_BIN" >&2
  exit 1
fi

"$PSQL_BIN" -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$MAINTENANCE_DB" -v ON_ERROR_STOP=1 -f "$BASE_DIR/00-create-databases.sql"

for mapping in \
  "10-auth-service.sql:rms_auth_db" \
  "11-customer-service.sql:rms_customer_db" \
  "12-employee-service.sql:rms_employee_db" \
  "13-property-service.sql:rms_property_db" \
  "14-event-gateway-service.sql:rms_event_gateway_db" \
  "14-table-service.sql:rms_table_db" \
  "15-catalog-service.sql:rms_catalog_db" \
  "16-inventory-service.sql:rms_inventory_db" \
  "17-order-service.sql:rms_order_db" \
  "18-kitchen-service.sql:rms_kitchen_db" \
  "19-billing-service.sql:rms_billing_db" \
  "20-payment-service.sql:rms_payment_db" \
  "21-takeaway-service.sql:rms_takeaway_db" \
  "22-marketplace-integration-service.sql:rms_marketplace_integration_db" \
  "23-audit-timeline-service.sql:rms_audit_db" \
  "24-review-feedback-service.sql:rms_review_db" \
  "25-reporting-service.sql:rms_reporting_db" \
  "26-operations-insights-service.sql:rms_operations_insights_db"
do
  file="${mapping%%:*}"
  db_name="${mapping#*:}"
  echo "Applying $file to $db_name"
  "$PSQL_BIN" -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$db_name" -v ON_ERROR_STOP=1 -f "$BASE_DIR/$file"
done

echo "Database setup completed successfully."

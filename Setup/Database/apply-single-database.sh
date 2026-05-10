#!/usr/bin/env bash
set -euo pipefail

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
PSQL_BIN="${PSQL_BIN:-$HOME/Applications/Postgres.app/Contents/Versions/latest/bin/psql}"
PGHOST="${PGHOST:-localhost}"
PGPORT="${PGPORT:-5433}"
PGUSER="${PGUSER:-$USER}"
MAINTENANCE_DB="${MAINTENANCE_DB:-postgres}"
RMS_DB_NAME="${RMS_DB_NAME:-restaurantManagementSystem}"

if [[ ! -x "$PSQL_BIN" ]]; then
  echo "psql not found at $PSQL_BIN" >&2
  exit 1
fi

"$PSQL_BIN" -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$MAINTENANCE_DB" -v ON_ERROR_STOP=1 -f "$BASE_DIR/00-create-single-database.sql"

for mapping in \
  "auth:10-auth-service.sql" \
  "customer:11-customer-service.sql" \
  "employee:12-employee-service.sql" \
  "property:13-property-service.sql" \
  "event_gateway:14-event-gateway-service.sql" \
  "table_mgmt:14-table-service.sql" \
  "catalog:15-catalog-service.sql" \
  "inventory:16-inventory-service.sql" \
  "ordering:17-order-service.sql" \
  "kitchen:18-kitchen-service.sql" \
  "billing:19-billing-service.sql" \
  "payment:20-payment-service.sql" \
  "takeaway:21-takeaway-service.sql" \
  "marketplace_integration:22-marketplace-integration-service.sql" \
  "audit:23-audit-timeline-service.sql" \
  "review:24-review-feedback-service.sql" \
  "reporting:25-reporting-service.sql" \
  "operations_insights:26-operations-insights-service.sql"
do
  schema_name="${mapping%%:*}"
  file_name="${mapping#*:}"
  echo "Applying $file_name to schema $schema_name in $RMS_DB_NAME"
  "$PSQL_BIN" -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$RMS_DB_NAME" -v ON_ERROR_STOP=1 -c "CREATE SCHEMA IF NOT EXISTS $schema_name"
  PGOPTIONS="-c search_path=$schema_name" "$PSQL_BIN" -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$RMS_DB_NAME" -v ON_ERROR_STOP=1 -f "$BASE_DIR/$file_name"
done

echo "Single database setup completed successfully."

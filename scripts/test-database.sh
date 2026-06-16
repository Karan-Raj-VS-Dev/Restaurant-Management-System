#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "$ROOT_DIR/scripts/postgres-common.sh"

ensure_postgres_tools
if ! postgres_ready; then
  echo "PostgreSQL is not ready on ${PGHOST}:${PGPORT}. Start it first, then rerun." >&2
  exit 1
fi

run_scalar() {
  "$PSQL_BIN" -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$RMS_DB_NAME" -At -c "$1"
}

assert_equals() {
  local expected="$1"
  local actual="$2"
  local message="$3"
  if [[ "$actual" != "$expected" ]]; then
    echo "FAIL: $message (expected '$expected', got '$actual')" >&2
    exit 1
  fi
  echo "PASS: $message"
}

assert_column_exists() {
  local schema="$1"
  local table="$2"
  local column="$3"
  local result
  result="$(run_scalar "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='${schema}' AND table_name='${table}' AND column_name='${column}';")"
  assert_equals "1" "$result" "${schema}.${table}.${column} exists"
}

assert_column_missing() {
  local schema="$1"
  local table="$2"
  local column="$3"
  local result
  result="$(run_scalar "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='${schema}' AND table_name='${table}' AND column_name='${column}';")"
  assert_equals "0" "$result" "${schema}.${table}.${column} removed"
}

assert_table_missing() {
  local schema="$1"
  local table="$2"
  local result
  result="$(run_scalar "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${schema}' AND table_name='${table}';")"
  assert_equals "0" "$result" "${schema}.${table} removed"
}

assert_zero_query() {
  local query="$1"
  local message="$2"
  local result
  result="$(run_scalar "$query")"
  assert_equals "0" "$result" "$message"
}

echo "Validating current RMS schema and relational data..."
assert_column_exists "ordering" "order_status_history" "status"
assert_column_exists "ordering" "order_status_history" "status_trail"
assert_column_missing "ordering" "order_status_history" "previous_status"
assert_column_missing "ordering" "order_status_history" "new_status"
assert_column_missing "ordering" "orders" "customer_id"
assert_column_missing "ordering" "orders" "guest_count"
assert_column_missing "ordering" "orders" "subtotal_amount"
assert_column_missing "ordering" "orders" "tax_amount"
assert_column_missing "ordering" "orders" "discount_amount"
assert_column_missing "ordering" "orders" "total_amount"

assert_column_exists "billing" "bills" "session_id"
assert_column_exists "billing" "bills" "last_order_id"
assert_column_missing "billing" "bills" "order_id"
assert_column_exists "billing" "bill_orders" "order_id"
assert_column_missing "billing" "bill_items" "order_item_id"

assert_column_exists "table_mgmt" "table_sessions" "order_id"
assert_column_exists "table_mgmt" "table_sessions" "customer_id"

assert_table_missing "customer" "customer_addresses"
assert_table_missing "customer" "customer_tags"
assert_column_missing "customer" "customers" "loyalty_number"
assert_column_missing "customer" "customers" "email"
assert_column_missing "customer" "customers" "notes"

assert_zero_query \
  "SELECT COUNT(*) FROM billing.bill_orders bo LEFT JOIN ordering.orders o ON o.order_id = bo.order_id WHERE o.order_id IS NULL;" \
  "bill_orders rows reference existing orders"
assert_zero_query \
  "SELECT COUNT(*) FROM billing.bills b LEFT JOIN ordering.orders o ON o.order_id = b.last_order_id WHERE b.last_order_id IS NOT NULL AND o.order_id IS NULL;" \
  "bills.last_order_id values reference existing orders"
assert_zero_query \
  "SELECT COUNT(*) FROM billing.bills b LEFT JOIN table_mgmt.table_sessions ts ON ts.session_id = b.session_id WHERE b.session_id IS NOT NULL AND ts.session_id IS NULL;" \
  "bills.session_id values reference existing table sessions"
assert_zero_query \
  "SELECT COUNT(*) FROM table_mgmt.table_sessions ts LEFT JOIN ordering.orders o ON o.order_id = ts.order_id WHERE ts.order_id IS NOT NULL AND o.order_id IS NULL;" \
  "table_sessions.order_id values reference existing orders"
assert_zero_query \
  "SELECT COUNT(*) FROM ordering.order_status_history osh LEFT JOIN ordering.orders o ON o.order_id = osh.order_id WHERE o.order_id IS NULL;" \
  "order_status_history rows reference existing orders"

echo
echo "Database validation completed successfully."

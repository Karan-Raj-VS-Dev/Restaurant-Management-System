#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
RUN_DIR="$ROOT_DIR/.local/run"
LOG_DIR="$RUN_DIR/logs"
JAVA_BIN="${JAVA_BIN:-/opt/homebrew/opt/openjdk@17/bin/java}"

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5433}"
RMS_DB_NAME="${RMS_DB_NAME:-restaurantManagementSystem}"
EVENT_GATEWAY_URL="${EVENT_GATEWAY_URL:-http://localhost:9018}"

mkdir -p "$LOG_DIR"

if ! lsof -iTCP:"$DB_PORT" -sTCP:LISTEN -nP | grep -q postgres; then
  echo "PostgreSQL is not listening on ${DB_HOST}:${DB_PORT}." >&2
  echo "Start PostgreSQL first, then rerun this script." >&2
  exit 1
fi

if [[ ! -x "$JAVA_BIN" ]]; then
  echo "Java runtime not found at $JAVA_BIN" >&2
  exit 1
fi

start_process() {
  local name="$1"
  shift
  local pid_file="$RUN_DIR/${name}.pid"
  local log_file="$LOG_DIR/${name}.log"

  if [[ -f "$pid_file" ]]; then
    local existing_pid
    existing_pid="$(cat "$pid_file")"
    if kill -0 "$existing_pid" >/dev/null 2>&1; then
      echo "$name is already running (pid $existing_pid)"
      return
    fi
    rm -f "$pid_file"
  fi

  nohup "$@" >"$log_file" 2>&1 &
  local pid=$!
  echo "$pid" >"$pid_file"
  echo "Started $name (pid $pid) -> $log_file"
}

ensure_file_exists() {
  local path="$1"
  if [[ ! -f "$path" ]]; then
    echo "Required file not found: $path" >&2
    echo "Build the project first, then rerun this script." >&2
    exit 1
  fi
}

export DB_HOST
export DB_PORT
export RMS_DB_NAME
export EVENT_GATEWAY_URL

ensure_file_exists "$ROOT_DIR/services/event-gateway-service/target/event-gateway-service-0.0.1-SNAPSHOT.jar"
ensure_file_exists "$ROOT_DIR/services/auth-service/target/auth-service-0.0.1-SNAPSHOT.jar"
ensure_file_exists "$ROOT_DIR/services/employee-service/target/employee-service-0.0.1-SNAPSHOT.jar"
ensure_file_exists "$ROOT_DIR/services/property-service/target/property-service-0.0.1-SNAPSHOT.jar"
ensure_file_exists "$ROOT_DIR/services/table-service/target/table-service-0.0.1-SNAPSHOT.jar"
ensure_file_exists "$ROOT_DIR/services/catalog-service/target/catalog-service-0.0.1-SNAPSHOT.jar"
ensure_file_exists "$ROOT_DIR/services/inventory-service/target/inventory-service-0.0.1-SNAPSHOT.jar"
ensure_file_exists "$ROOT_DIR/services/order-service/target/order-service-0.0.1-SNAPSHOT.jar"
ensure_file_exists "$ROOT_DIR/services/kitchen-service/target/kitchen-service-0.0.1-SNAPSHOT.jar"
ensure_file_exists "$ROOT_DIR/services/billing-service/target/billing-service-0.0.1-SNAPSHOT.jar"
ensure_file_exists "$ROOT_DIR/services/payment-service/target/payment-service-0.0.1-SNAPSHOT.jar"
ensure_file_exists "$ROOT_DIR/services/takeaway-service/target/takeaway-service-0.0.1-SNAPSHOT.jar"
ensure_file_exists "$ROOT_DIR/services/audit-timeline-service/target/audit-timeline-service-0.0.1-SNAPSHOT.jar"
ensure_file_exists "$ROOT_DIR/services/review-feedback-service/target/review-feedback-service-0.0.1-SNAPSHOT.jar"
ensure_file_exists "$ROOT_DIR/services/operations-insights-service/target/operations-insights-service-0.0.1-SNAPSHOT.jar"
ensure_file_exists "$ROOT_DIR/frontend/package.json"

start_process "event-gateway-service" \
  "$JAVA_BIN" -jar "$ROOT_DIR/services/event-gateway-service/target/event-gateway-service-0.0.1-SNAPSHOT.jar"

sleep 3

start_process "auth-service" \
  "$JAVA_BIN" -jar "$ROOT_DIR/services/auth-service/target/auth-service-0.0.1-SNAPSHOT.jar"
start_process "employee-service" \
  "$JAVA_BIN" -jar "$ROOT_DIR/services/employee-service/target/employee-service-0.0.1-SNAPSHOT.jar"
start_process "property-service" \
  "$JAVA_BIN" -jar "$ROOT_DIR/services/property-service/target/property-service-0.0.1-SNAPSHOT.jar"
start_process "table-service" \
  "$JAVA_BIN" -jar "$ROOT_DIR/services/table-service/target/table-service-0.0.1-SNAPSHOT.jar"
start_process "catalog-service" \
  "$JAVA_BIN" -jar "$ROOT_DIR/services/catalog-service/target/catalog-service-0.0.1-SNAPSHOT.jar"
start_process "inventory-service" \
  "$JAVA_BIN" -jar "$ROOT_DIR/services/inventory-service/target/inventory-service-0.0.1-SNAPSHOT.jar"
start_process "order-service" \
  "$JAVA_BIN" -jar "$ROOT_DIR/services/order-service/target/order-service-0.0.1-SNAPSHOT.jar"
start_process "kitchen-service" \
  "$JAVA_BIN" -jar "$ROOT_DIR/services/kitchen-service/target/kitchen-service-0.0.1-SNAPSHOT.jar"
start_process "billing-service" \
  "$JAVA_BIN" -jar "$ROOT_DIR/services/billing-service/target/billing-service-0.0.1-SNAPSHOT.jar"
start_process "payment-service" \
  "$JAVA_BIN" -jar "$ROOT_DIR/services/payment-service/target/payment-service-0.0.1-SNAPSHOT.jar"
start_process "takeaway-service" \
  "$JAVA_BIN" -jar "$ROOT_DIR/services/takeaway-service/target/takeaway-service-0.0.1-SNAPSHOT.jar"
start_process "audit-timeline-service" \
  "$JAVA_BIN" -jar "$ROOT_DIR/services/audit-timeline-service/target/audit-timeline-service-0.0.1-SNAPSHOT.jar"
start_process "review-feedback-service" \
  "$JAVA_BIN" -jar "$ROOT_DIR/services/review-feedback-service/target/review-feedback-service-0.0.1-SNAPSHOT.jar"
start_process "operations-insights-service" \
  "$JAVA_BIN" -jar "$ROOT_DIR/services/operations-insights-service/target/operations-insights-service-0.0.1-SNAPSHOT.jar"

start_process "pos-ui" \
  npm --prefix "$ROOT_DIR/frontend" run dev --workspace @restaurant/pos-ui -- --host 127.0.0.1 --port 5173
start_process "kitchen-ui" \
  npm --prefix "$ROOT_DIR/frontend" run dev --workspace @restaurant/kitchen-ui -- --host 127.0.0.1 --port 5174
start_process "admin-ui" \
  npm --prefix "$ROOT_DIR/frontend" run dev --workspace @restaurant/admin-ui -- --host 127.0.0.1 --port 5175
start_process "restaurant-ui" \
  npm --prefix "$ROOT_DIR/frontend" run dev --workspace @restaurant/restaurant-ui -- --host 127.0.0.1 --port 5176
start_process "inventory-ui" \
  npm --prefix "$ROOT_DIR/frontend" run dev --workspace @restaurant/inventory-ui -- --host 127.0.0.1 --port 5177
start_process "employees-ui" \
  npm --prefix "$ROOT_DIR/frontend" run dev --workspace @restaurant/employees-ui -- --host 127.0.0.1 --port 5178
start_process "property-settings-ui" \
  npm --prefix "$ROOT_DIR/frontend" run dev --workspace @restaurant/property-settings-ui -- --host 127.0.0.1 --port 5179
start_process "reports-ui" \
  npm --prefix "$ROOT_DIR/frontend" run dev --workspace @restaurant/reports-ui -- --host 127.0.0.1 --port 5180

echo
echo "Local main-flow stack started."
echo "Use scripts/status-local-main-flow.sh to inspect status."
echo "Use scripts/stop-local-main-flow.sh to stop the stack."

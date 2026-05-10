#!/usr/bin/env bash
set -euo pipefail

POSTGRES_APP_BIN="${POSTGRES_APP_BIN:-$HOME/Applications/Postgres.app/Contents/Versions/latest/bin}"
PG_CTL_BIN="${PG_CTL_BIN:-$POSTGRES_APP_BIN/pg_ctl}"
PG_ISREADY_BIN="${PG_ISREADY_BIN:-$POSTGRES_APP_BIN/pg_isready}"
PSQL_BIN="${PSQL_BIN:-$POSTGRES_APP_BIN/psql}"

PGHOST="${PGHOST:-localhost}"
PGPORT="${PGPORT:-5433}"
PGUSER="${PGUSER:-$USER}"
PGDATA="${PGDATA:-$HOME/Library/Application Support/Postgres/var-18}"
PGLOG="${PGLOG:-$HOME/Library/Application Support/Postgres/server-${PGPORT}.log}"
PGDATABASE="${PGDATABASE:-postgres}"
RMS_DB_NAME="${RMS_DB_NAME:-restaurantManagementSystem}"

require_postgres_bin() {
  local path="$1"
  local name="$2"
  if [[ ! -x "$path" ]]; then
    echo "${name} not found at ${path}" >&2
    echo "Update POSTGRES_APP_BIN or ${name}_BIN and rerun." >&2
    exit 1
  fi
}

ensure_postgres_tools() {
  require_postgres_bin "$PG_CTL_BIN" "pg_ctl"
  require_postgres_bin "$PG_ISREADY_BIN" "pg_isready"
}

postgres_running() {
  "$PG_CTL_BIN" -D "$PGDATA" status >/dev/null 2>&1
}

postgres_ready() {
  "$PG_ISREADY_BIN" -h "$PGHOST" -p "$PGPORT" >/dev/null 2>&1
}

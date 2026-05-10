#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/postgres-common.sh"

ensure_postgres_tools

echo "PostgreSQL local status"
echo "Host: $PGHOST"
echo "Port: $PGPORT"
echo "Data directory: $PGDATA"
echo "Log file: $PGLOG"

if postgres_running; then
  echo "Process state: running"
else
  echo "Process state: stopped"
  exit 0
fi

if postgres_ready; then
  echo "Readiness: accepting connections"
else
  echo "Readiness: not accepting connections yet"
  exit 1
fi

if [[ -x "$PSQL_BIN" ]]; then
  db_exists="$("$PSQL_BIN" -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" -At -c "SELECT 1 FROM pg_database WHERE datname = '$RMS_DB_NAME';" 2>/dev/null || true)"
  if [[ "$db_exists" == "1" ]]; then
    echo "Project database: $RMS_DB_NAME exists"
  else
    echo "Project database: $RMS_DB_NAME not found"
  fi
fi

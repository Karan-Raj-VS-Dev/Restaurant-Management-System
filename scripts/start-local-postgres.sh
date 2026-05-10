#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/postgres-common.sh"

ensure_postgres_tools

if postgres_running; then
  echo "PostgreSQL is already running."
  echo "Host: $PGHOST"
  echo "Port: $PGPORT"
  echo "Data directory: $PGDATA"
  exit 0
fi

mkdir -p "$(dirname "$PGLOG")"

"$PG_CTL_BIN" -D "$PGDATA" -l "$PGLOG" -o "-p $PGPORT" start

if postgres_ready; then
  echo "PostgreSQL started successfully."
  echo "Host: $PGHOST"
  echo "Port: $PGPORT"
  echo "Data directory: $PGDATA"
  echo "Log file: $PGLOG"
else
  echo "PostgreSQL start was requested, but readiness check did not pass yet." >&2
  echo "Check the server log at $PGLOG" >&2
  exit 1
fi

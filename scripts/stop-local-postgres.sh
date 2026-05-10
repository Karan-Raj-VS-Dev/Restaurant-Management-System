#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/postgres-common.sh"

ensure_postgres_tools

if ! postgres_running; then
  echo "PostgreSQL is not running."
  exit 0
fi

PG_STOP_MODE="${PG_STOP_MODE:-fast}"
"$PG_CTL_BIN" -D "$PGDATA" -m "$PG_STOP_MODE" stop

echo "PostgreSQL stopped."
echo "Stop mode: $PG_STOP_MODE"
echo "Data directory: $PGDATA"

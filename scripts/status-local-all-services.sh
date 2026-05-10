#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
RUN_DIR="$ROOT_DIR/.local/all-services-run"
LOG_DIR="$RUN_DIR/logs"

if [[ ! -d "$RUN_DIR" ]]; then
  echo "No all-services stack has been started yet."
else
  for pid_file in "$RUN_DIR"/*.pid; do
    [[ -e "$pid_file" ]] || continue
    name="$(basename "$pid_file" .pid)"
    pid="$(cat "$pid_file")"
    if kill -0 "$pid" >/dev/null 2>&1; then
      echo "$name: running (pid $pid) log=$LOG_DIR/${name}.log"
    else
      echo "$name: stopped (stale pid $pid) log=$LOG_DIR/${name}.log"
    fi
  done
fi

#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FRONTEND_DIR="$ROOT_DIR/frontend"

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <workspace-or-path>" >&2
  echo "Examples:" >&2
  echo "  $0 pos-ui" >&2
  echo "  $0 kitchen-ui" >&2
  echo "  $0 packages/ui" >&2
  exit 1
fi

target="$1"
if [[ -d "$FRONTEND_DIR/apps/$target" ]]; then
  test_path="apps/$target"
elif [[ -d "$FRONTEND_DIR/$target" ]]; then
  test_path="$target"
else
  echo "Unable to resolve frontend workspace path for '$target'." >&2
  exit 1
fi

cd "$FRONTEND_DIR"
echo "Running frontend tests for $test_path ..."
./node_modules/.bin/vitest run --coverage --coverage.include="$test_path/src/**/*.{ts,tsx}" "$test_path"

echo
echo "Frontend coverage report: $FRONTEND_DIR/coverage/index.html"

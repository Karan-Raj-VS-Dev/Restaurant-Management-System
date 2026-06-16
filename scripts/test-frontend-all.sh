#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FRONTEND_DIR="$ROOT_DIR/frontend"

cd "$FRONTEND_DIR"
echo "Running frontend test suite with coverage..."
npm run test:coverage

echo
echo "Frontend coverage report: $FRONTEND_DIR/coverage/index.html"

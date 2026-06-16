#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

"$ROOT_DIR/scripts/test-backend-all.sh"
"$ROOT_DIR/scripts/test-database.sh"
"$ROOT_DIR/scripts/test-frontend-all.sh"

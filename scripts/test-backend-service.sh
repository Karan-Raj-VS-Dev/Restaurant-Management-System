#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MAVEN_REPO_LOCAL="${MAVEN_REPO_LOCAL:-$HOME/.m2/repository}"

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <service-module>" >&2
  echo "Examples:" >&2
  echo "  $0 order-service" >&2
  echo "  $0 services/order-service" >&2
  echo "  $0 platform/platform-eventing" >&2
  exit 1
fi

target="$1"
if [[ -d "$ROOT_DIR/services/$target" ]]; then
  module="services/$target"
elif [[ -d "$ROOT_DIR/$target" ]]; then
  module="$target"
else
  echo "Unable to resolve module path for '$target'." >&2
  exit 1
fi

cd "$ROOT_DIR"
echo "Running backend tests for $module ..."
mvn -B -Pcoverage -pl "$module" -am verify -DfailIfNoTests=false -Dmaven.repo.local="$MAVEN_REPO_LOCAL"

echo
echo "Module report: $ROOT_DIR/$module/target/site/jacoco/index.html"

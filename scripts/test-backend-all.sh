#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MAVEN_REPO_LOCAL="${MAVEN_REPO_LOCAL:-$HOME/.m2/repository}"

cd "$ROOT_DIR"
echo "Running backend test suite across all Maven modules..."
mvn -B -Pcoverage verify -DfailIfNoTests=false -Dmaven.repo.local="$MAVEN_REPO_LOCAL"

echo
echo "JaCoCo reports are available under each module's target/site/jacoco directory."

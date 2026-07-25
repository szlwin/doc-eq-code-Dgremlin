#!/bin/sh
set -u
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
STAMP=${P0_EVIDENCE_STAMP:-$(date -u +%Y%m%dT%H%M%SZ)}
OUT=${P0_EVIDENCE_DIR:-$ROOT/docs/remediation/P0/evidence/dynamic-$STAMP}
mkdir -p "$OUT"
SUMMARY="$OUT/summary.txt"
: > "$SUMMARY"

run_step() {
  number=$1
  name=$2
  shift 2
  log="$OUT/${number}-${name}.log"
  echo "[$number] $name" | tee -a "$SUMMARY"
  echo "+ $*" | tee "$log"
  set +e
  "$@" >> "$log" 2>&1
  status=$?
  set -e
  echo "$status" > "$OUT/${number}-${name}.exit"
  echo "exit=$status log=$log" | tee -a "$SUMMARY"
  return "$status"
}

set -e
{
  echo "timestamp=$(date -u +%Y-%m-%dT%H:%M:%SZ)"
  echo "root=$ROOT"
  echo "git_head=$(git -C "$ROOT" rev-parse HEAD 2>/dev/null || echo unknown)"
  echo "java=$(java -version 2>&1 | head -1 || true)"
  echo "wrapper_url=$(sed -n 's/^distributionUrl=//p' "$ROOT/.mvn/wrapper/maven-wrapper.properties")"
} > "$OUT/00-environment.txt"

failed=0
run_step 01 mvnw-version "$ROOT/mvnw" --version || failed=1
if [ "$failed" -eq 0 ]; then
  run_step 02 bootstrap-legacy "$ROOT/scripts/remediation/bootstrap_legacy_dependencies.sh" || failed=1
fi
if [ "$failed" -eq 0 ]; then
  run_step 03 clean-verify "$ROOT/mvnw" --batch-mode --no-transfer-progress clean verify || failed=1
fi
if [ "$failed" -eq 0 ]; then
  run_step 04 failure-gate "$ROOT/scripts/remediation/prove_test_failure_gate.sh" || failed=1
fi
run_step 05 static-validation python3 "$ROOT/scripts/remediation/validate_p0.py" || failed=1

if [ "$failed" -ne 0 ]; then
  echo "P0 dynamic verification did not complete; inspect $SUMMARY and step logs." >&2
  exit 1
fi

echo "P0 local dynamic verification PASSED. Evidence: $OUT"

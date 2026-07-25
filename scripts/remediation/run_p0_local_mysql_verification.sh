#!/bin/sh
set -u

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
STAMP=${P0_EVIDENCE_STAMP:-$(date -u +%Y%m%dT%H%M%SZ)}
OUT=${P0_MYSQL_EVIDENCE_DIR:-$ROOT/docs/remediation/P0/evidence/local-mysql-$STAMP}
REQUIRE_CLEAN=${P0_REQUIRE_CLEAN_WORKTREE:-1}

require_env() {
  name=$1
  eval "value=\${$name-}"
  if [ -z "$value" ]; then
    echo "Required environment variable is missing: $name" >&2
    return 1
  fi
}

failed=0
require_env DEC_MYSQL_URL || failed=1
require_env DEC_MYSQL_USER || failed=1
require_env DEC_MYSQL_PASSWORD || failed=1
[ "$failed" -eq 0 ] || exit 2

status_before=$(git -C "$ROOT" status --porcelain 2>/dev/null || true)
if [ "$REQUIRE_CLEAN" = "1" ] && [ -n "$status_before" ]; then
  echo "Formal P0 verification requires a clean Git worktree." >&2
  echo "Commit or restore current changes, or set P0_REQUIRE_CLEAN_WORKTREE=0 for a non-formal diagnostic run." >&2
  printf '%s\n' "$status_before" >&2
  exit 3
fi

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
  echo "git_branch=$(git -C "$ROOT" branch --show-current 2>/dev/null || echo unknown)"
  echo "git_worktree_clean=$([ -z "$status_before" ] && echo true || echo false)"
  echo "java=$(java -version 2>&1 | head -1 || true)"
  echo "wrapper_url=$(sed -n 's/^distributionUrl=//p' "$ROOT/.mvn/wrapper/maven-wrapper.properties")"
  echo "DEC_MYSQL_URL=$DEC_MYSQL_URL"
  echo "DEC_MYSQL_USER=$DEC_MYSQL_USER"
  echo "DEC_MYSQL_PASSWORD=SET"
} > "$OUT/00-environment.txt"
printf '%s\n' "$status_before" > "$OUT/00-git-status-before.txt"

failed=0
run_step 01 mvnw-version "$ROOT/mvnw" --version || failed=1
if [ "$failed" -eq 0 ]; then
  run_step 02 bootstrap-legacy "$ROOT/scripts/remediation/bootstrap_legacy_dependencies.sh" || failed=1
fi
if [ "$failed" -eq 0 ]; then
  run_step 03 mysql-it "$ROOT/mvnw" --batch-mode --no-transfer-progress -Pmysql-it clean verify || failed=1
fi

end_head=$(git -C "$ROOT" rev-parse HEAD 2>/dev/null || echo unknown)
start_head=$(sed -n 's/^git_head=//p' "$OUT/00-environment.txt")
if [ "$start_head" != "$end_head" ]; then
  echo "Git HEAD changed during verification: $start_head -> $end_head" | tee -a "$SUMMARY" >&2
  failed=1
fi

if command -v shasum >/dev/null 2>&1; then
  find "$OUT" -maxdepth 1 -type f ! -name checksums.sha256 -print | sort \
    | while IFS= read -r file; do shasum -a 256 "$file"; done \
    > "$OUT/checksums.sha256"
elif command -v sha256sum >/dev/null 2>&1; then
  find "$OUT" -maxdepth 1 -type f ! -name checksums.sha256 -print | sort \
    | xargs sha256sum > "$OUT/checksums.sha256"
fi

if [ "$failed" -ne 0 ]; then
  echo "P0 local MySQL verification FAILED. Inspect $SUMMARY and step logs." >&2
  exit 1
fi

echo "P0 local MySQL verification PASSED. Evidence: $OUT"

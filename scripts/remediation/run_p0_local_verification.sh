#!/bin/sh
set -u

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
STAMP=${P0_EVIDENCE_STAMP:-$(date -u +%Y%m%dT%H%M%SZ)}
OUT=${P0_LOCAL_EVIDENCE_DIR:-$ROOT/docs/remediation/P0/evidence/local-full-$STAMP}
REQUIRE_CLEAN=${P0_REQUIRE_CLEAN_WORKTREE:-1}

for name in DEC_MYSQL_URL DEC_MYSQL_USER DEC_MYSQL_PASSWORD; do
  eval "value=\${$name-}"
  if [ -z "$value" ]; then
    echo "Required environment variable is missing: $name" >&2
    exit 2
  fi
done

status_before=$(git -C "$ROOT" status --porcelain 2>/dev/null || true)
if [ "$REQUIRE_CLEAN" = "1" ] && [ -n "$status_before" ]; then
  echo "Formal P0 verification requires a clean Git worktree." >&2
  echo "Commit or restore current changes, or set P0_REQUIRE_CLEAN_WORKTREE=0 for a non-formal diagnostic run." >&2
  printf '%s\n' "$status_before" >&2
  exit 3
fi

mkdir -p "$OUT"
START_HEAD=$(git -C "$ROOT" rev-parse HEAD 2>/dev/null || echo unknown)
{
  echo "timestamp=$(date -u +%Y-%m-%dT%H:%M:%SZ)"
  echo "root=$ROOT"
  echo "git_head=$START_HEAD"
  echo "git_branch=$(git -C "$ROOT" branch --show-current 2>/dev/null || echo unknown)"
  echo "git_worktree_clean=$([ -z "$status_before" ] && echo true || echo false)"
  echo "DEC_MYSQL_URL=$DEC_MYSQL_URL"
  echo "DEC_MYSQL_USER=$DEC_MYSQL_USER"
  echo "DEC_MYSQL_PASSWORD=SET"
} > "$OUT/00-environment.txt"
printf '%s\n' "$status_before" > "$OUT/00-git-status-before.txt"

failed=0
set +e
(
  P0_EVIDENCE_DIR="$OUT/core" \
  P0_REQUIRE_CLEAN_WORKTREE=0 \
    "$ROOT/scripts/remediation/run_p0_dynamic_verification.sh"
  echo "$?" > "$OUT/01-core-runner.exit.tmp"
) 2>&1 | tee "$OUT/01-core-runner.log"
core_status=$(cat "$OUT/01-core-runner.exit.tmp")
rm -f "$OUT/01-core-runner.exit.tmp"
echo "$core_status" > "$OUT/01-core-runner.exit"
set -e
[ "$core_status" -eq 0 ] || failed=1

if [ "$failed" -eq 0 ]; then
  set +e
  (
    P0_MYSQL_EVIDENCE_DIR="$OUT/mysql" \
    P0_REQUIRE_CLEAN_WORKTREE=0 \
      "$ROOT/scripts/remediation/run_p0_local_mysql_verification.sh"
    echo "$?" > "$OUT/02-mysql-runner.exit.tmp"
  ) 2>&1 | tee "$OUT/02-mysql-runner.log"
  mysql_status=$(cat "$OUT/02-mysql-runner.exit.tmp")
  rm -f "$OUT/02-mysql-runner.exit.tmp"
  echo "$mysql_status" > "$OUT/02-mysql-runner.exit"
  set -e
  [ "$mysql_status" -eq 0 ] || failed=1
else
  echo "skipped because core verification failed" | tee "$OUT/02-mysql-runner.log"
  echo "125" > "$OUT/02-mysql-runner.exit"
fi

END_HEAD=$(git -C "$ROOT" rev-parse HEAD 2>/dev/null || echo unknown)
if [ "$START_HEAD" != "$END_HEAD" ]; then
  echo "Git HEAD changed during verification: $START_HEAD -> $END_HEAD" >> "$OUT/summary.txt"
  failed=1
fi

{
  echo "core_exit=$(cat "$OUT/01-core-runner.exit")"
  echo "mysql_exit=$(cat "$OUT/02-mysql-runner.exit")"
  echo "start_head=$START_HEAD"
  echo "end_head=$END_HEAD"
  echo "result=$([ "$failed" -eq 0 ] && echo PASSED || echo FAILED)"
} > "$OUT/summary.txt"

if command -v shasum >/dev/null 2>&1; then
  find "$OUT" -type f ! -name checksums.sha256 -print | sort \
    | while IFS= read -r file; do shasum -a 256 "$file"; done \
    > "$OUT/checksums.sha256"
elif command -v sha256sum >/dev/null 2>&1; then
  find "$OUT" -type f ! -name checksums.sha256 -print | sort \
    | xargs sha256sum > "$OUT/checksums.sha256"
fi

cat "$OUT/01-core-runner.log"
cat "$OUT/02-mysql-runner.log"

if [ "$failed" -ne 0 ]; then
  echo "P0 formal local verification FAILED. Evidence: $OUT" >&2
  exit 1
fi

echo "P0 formal local verification PASSED. Evidence: $OUT"

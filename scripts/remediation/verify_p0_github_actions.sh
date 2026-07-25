#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
WORKFLOW=${1:-p0-build.yml}
BRANCH=${2:-dev_all}
OUT=${P0_GITHUB_EVIDENCE_DIR:-$ROOT/docs/remediation/P0/evidence/github-actions}
mkdir -p "$OUT"

command -v gh >/dev/null 2>&1 || {
  echo "GitHub CLI (gh) is required: https://cli.github.com/" >&2
  exit 2
}
gh auth status

echo "Triggering $WORKFLOW on $BRANCH"
gh workflow run "$WORKFLOW" --ref "$BRANCH"

run_id=""
count=0
while [ -z "$run_id" ] && [ "$count" -lt 30 ]; do
  sleep 2
  run_id=$(gh run list --workflow "$WORKFLOW" --branch "$BRANCH" --event workflow_dispatch --limit 1 \
    --json databaseId --jq '.[0].databaseId // empty')
  count=$((count + 1))
done
[ -n "$run_id" ] || { echo "Unable to locate triggered workflow run" >&2; exit 3; }

echo "$run_id" > "$OUT/run-id.txt"
gh run watch "$run_id" --exit-status 2>&1 | tee "$OUT/run-watch.log"
gh run view "$run_id" --json databaseId,headSha,status,conclusion,url,jobs > "$OUT/run.json"

core_conclusion=$(gh run view "$run_id" --json jobs --jq '.jobs[] | select(.name=="core-verify") | .conclusion')
[ "$core_conclusion" = "success" ] || {
  echo "core-verify conclusion is '$core_conclusion', expected success" >&2
  gh run view "$run_id" --log-failed > "$OUT/failed-jobs.log" 2>&1 || true
  exit 4
}

gh run download "$run_id" --name surefire-and-jacoco-reports --dir "$OUT/artifacts" || true
echo "GitHub Actions core-verify PASSED. Evidence: $OUT/run.json"

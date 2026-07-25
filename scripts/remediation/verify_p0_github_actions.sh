#!/bin/sh
set -eu

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
WORKFLOW=${1:-p0-build.yml}
BRANCH=${2:-dev_all}
OUT=${P0_GITHUB_EVIDENCE_DIR:-$ROOT/docs/remediation/P0/evidence/github-actions}
REQUIRE_FULL_WORKFLOW=${P0_GITHUB_REQUIRE_FULL_WORKFLOW:-0}
mkdir -p "$OUT"

command -v gh >/dev/null 2>&1 || {
  echo "GitHub CLI (gh) is required: https://cli.github.com/" >&2
  exit 2
}
gh auth status

repo=$(gh repo view --json nameWithOwner --jq '.nameWithOwner')
remote_sha=$(gh api "repos/$repo/commits/$BRANCH" --jq '.sha')
previous_run_id=$(gh run list --workflow "$WORKFLOW" --branch "$BRANCH" --event workflow_dispatch --limit 1 \
  --json databaseId --jq '.[0].databaseId // empty')

echo "AUXILIARY ONLY: GitHub Actions is not a P0 exit gate."
echo "Triggering $WORKFLOW on $BRANCH at $remote_sha"
gh workflow run "$WORKFLOW" --ref "$BRANCH"

run_id=""
count=0
while [ -z "$run_id" ] && [ "$count" -lt 60 ]; do
  sleep 2
  candidate=$(gh run list --workflow "$WORKFLOW" --branch "$BRANCH" --event workflow_dispatch --limit 5 \
    --json databaseId,headSha --jq ".[] | select(.databaseId != ${previous_run_id:-0} and .headSha == \"$remote_sha\") | .databaseId" \
    | head -1)
  run_id=${candidate:-}
  count=$((count + 1))
done
[ -n "$run_id" ] || { echo "Unable to locate the newly triggered workflow run" >&2; exit 3; }

echo "$run_id" > "$OUT/run-id.txt"
set +e
gh run watch "$run_id" --exit-status > "$OUT/run-watch.log" 2>&1
watch_status=$?
set -e
cat "$OUT/run-watch.log"
echo "$watch_status" > "$OUT/run-watch.exit"

gh run view "$run_id" --json databaseId,headSha,status,conclusion,url,jobs > "$OUT/run.json"
core_conclusion=$(gh run view "$run_id" --json jobs --jq '.jobs[] | select(.name=="core-verify") | .conclusion')
overall_conclusion=$(gh run view "$run_id" --json conclusion --jq '.conclusion')

[ "$core_conclusion" = "success" ] || {
  echo "Auxiliary core-verify conclusion is '$core_conclusion', expected success" >&2
  gh run view "$run_id" --log-failed > "$OUT/failed-jobs.log" 2>&1 || true
  exit 4
}

if [ "$REQUIRE_FULL_WORKFLOW" = "1" ] && [ "$overall_conclusion" != "success" ]; then
  echo "Auxiliary full workflow conclusion is '$overall_conclusion', expected success" >&2
  gh run view "$run_id" --log-failed > "$OUT/failed-jobs.log" 2>&1 || true
  exit 5
fi

if [ "$overall_conclusion" != "success" ]; then
  echo "Auxiliary core-verify PASSED, but overall workflow conclusion is '$overall_conclusion'."
  echo "This result is recorded for diagnosis and does not determine the P0 exit gate."
  gh run view "$run_id" --log-failed > "$OUT/failed-jobs.log" 2>&1 || true
fi

gh run download "$run_id" --name surefire-and-jacoco-reports --dir "$OUT/artifacts" || true
echo "GitHub Actions auxiliary core-verify PASSED. Evidence: $OUT/run.json"

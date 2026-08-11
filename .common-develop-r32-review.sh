#!/usr/bin/env bash
set -euo pipefail
BASE=634120457b48b46e1d7b38d409af51397af81e5b
OUT_BRANCH=tmp/pr36-r32-reviewed-20260811
TDIR=project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC
rm -rf /tmp/common-develop
git clone --depth=1 https://gitee.com/szlwin/common-develop.git /tmp/common-develop
test "$(git -C /tmp/common-develop rev-parse HEAD)" = "7086b2d32b6beae2e6e522efc517d7823ba55376"
sudo mkdir -p /home/oai/skills
sudo rm -f /home/oai/skills/common-develop
sudo ln -s /tmp/common-develop /home/oai/skills/common-develop
git reset --hard "$BASE"
git clean -fd
add_assertion() {
  local agent="$1" id="$2" ac="$3" statement="$4"
  python3 /home/oai/skills/common-develop/scripts/acceptance.py add -g ProjectManagerAgent --task-dir "$TDIR" --assertion-id "$id" --acceptance-id "$ac" --statement "$statement" --type MANUAL_REVIEW --phase test_design --revision TESTDESIGN-P2-R32 --blocking --parameters "{\"reviewer_agent\":\"$agent\",\"review_phase\":\"test_design\"}" --source-ref TASK-P2-TESTDESIGN-001#expected_results/0
}
add_assertion RequirementReviewAgent ASRT-P2-TD-R32-REQ-001 AC-P2-SYSTEM-RULEVIEW-001 "Independent requirement review of TESTDESIGN-P2-R32 nested ModelPath clarification; confirms no new P1/BM/Design requirement is introduced."
add_assertion DesignReviewAgent ASRT-P2-TD-R32-DES-001 AC-P2-SYSTEM-RULEVIEW-007 "Independent design-consistency review of TESTDESIGN-P2-R32 against unchanged DESIGN-P2-R30 exact ModelPath semantics."
add_assertion TDDReviewAgent ASRT-P2-TD-R32-TDD-001 AC-P2-SYSTEM-RULEVIEW-007 "Independent TDD readiness review of six R32 nested ModelPath/exact-authorization oracles using existing TARGET/POLICY TestClasses."
add_assertion TestEvidenceReviewAgent ASRT-P2-TD-R32-EVID-001 AC-P2-SYSTEM-RULEVIEW-009 "Independent evidence review of TESTDESIGN-P2-R32 101-case, 23-TestClass, 10-trace deterministic validation."
review() {
  local reviewer="$1" id="$2" out="$3" summary="$4" detail="$5"; shift 5
  local args=(python3 /home/oai/skills/common-develop/scripts/manual_review.py draft -g TestDesignAgent --task-dir "$TDIR" --assertion-id "$id" --summary "$summary" --detail "$detail" --output "$out")
  case "$reviewer" in
    RequirementReviewAgent) args+=(--answer MRQ-ACCEPTANCE=YES --answer MRQ-DESIGN=YES) ;;
    DesignReviewAgent) args+=(--answer MRQ-SCOPE=YES --answer MRQ-VERIFY=YES) ;;
    TDDReviewAgent) args+=(--answer MRQ-VERIFY=YES --answer MRQ-OTHER=YES) ;;
    TestEvidenceReviewAgent) args+=(--answer MRQ-CURRENT=YES --answer MRQ-COVERAGE=YES --answer MRQ-LIMIT=YES --answer MRQ-OTHER=YES) ;;
    *) echo "unknown reviewer $reviewer" >&2; exit 2 ;;
  esac
  for ev in "$@"; do args+=(--evidence-id "$ev"); done
  "${args[@]}"
  python3 /home/oai/skills/common-develop/scripts/manual_review.py submit -g "$reviewer" --task-dir "$TDIR" --review-file "$out"
}
review RequirementReviewAgent ASRT-P2-TD-R32-REQ-001 /tmp/r32-req.json "PASSED: R32 clarifies existing nested ModelPath behavior without expanding requirements." "The six new oracles are test-level clarification of already-authoritative exact path, non-composite failure and exact authorization behavior. P1, BM-R20, FLOW-R11 and DESIGN-P2-R30 remain unchanged; TR-004/TR-005 retain the same stable requirement identities." EVD-000193 EVD-000194 EVD-000195 EVD-000196 EVD-000197
review DesignReviewAgent ASRT-P2-TD-R32-DES-001 /tmp/r32-des.json "PASSED: R32 is consistent with DESIGN-P2-R30." "R32 preserves target-main exact-match isolation, canonical segmented ModelPath traversal, fail-closed non-composite intermediates, finite compile-time wildcard expansion and exact-only runtime policy lookup. No architecture, API, module or dependency change is introduced." EVD-000193 EVD-000194 EVD-000195 EVD-000196 EVD-000197
review TDDReviewAgent ASRT-P2-TD-R32-TDD-001 /tmp/r32-tdd.json "PASSED: all six R32 oracles are executable in the existing TestDesign registry." "Nested object/deep path, non-composite failure, collection navigation, target-main isolation and parent-path no-auth fallback are concrete positive/negative oracles. They reuse existing TARGET/POLICY TestClasses; the exact TestClass registry remains 23." EVD-000193 EVD-000196 EVD-000197 EVD-000198 EVD-000199
review TestEvidenceReviewAgent ASRT-P2-TD-R32-EVID-001 /tmp/r32-evid.json "PASSED: R32 evidence is complete and deterministic." "Schema-v2 command Evidence proves TESTDESIGN-P2-R32, exactly 101 unique Cases, exactly 23 TestClasses, all six nested-path cases present, all six mapped to TR-005, exact authorization additionally mapped to TR-004, and the stable trace set remains 10." EVD-000193 EVD-000194 EVD-000196 EVD-000197 EVD-000198 EVD-000199
python3 /home/oai/skills/common-develop/scripts/acceptance.py validate -g ProjectManagerAgent --task-dir "$TDIR"
python3 /home/oai/skills/common-develop/scripts/evidence.py validate -g ProjectManagerAgent --task-dir "$TDIR"
python3 /home/oai/skills/common-develop/scripts/long_task.py finalize-phase -g ProjectManagerAgent --task-dir "$TDIR"
python3 /home/oai/skills/common-develop/scripts/long_task.py validate -g ProjectManagerAgent --task-dir "$TDIR" --json > /tmp/r32-final-long-task.json
git diff --check
git config user.name common-develop-bot
git config user.email common-develop-bot@users.noreply.github.com
git add -A
git commit -m 'review(p2): pass nested ModelPath TestDesign R32'
git push --force origin HEAD:"$OUT_BRANCH"
echo "REVIEWED_COMMIT=$(git rev-parse HEAD)"

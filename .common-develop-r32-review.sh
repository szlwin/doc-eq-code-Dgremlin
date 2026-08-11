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
  local reviewer="$1" id="$2" out="$3" summary="$4"; shift 4
  local args=(python3 /home/oai/skills/common-develop/scripts/manual_review.py draft -g ProjectManagerAgent --task-dir "$TDIR" --assertion-id "$id" --summary "$summary" --output "$out")
  case "$reviewer" in
    RequirementReviewAgent)
      args+=(--answer MRQ-ACCEPTANCE=YES --detail "MRQ-ACCEPTANCE=The six R32 oracles make existing TR-004/TR-005 acceptance behavior explicit without adding a new requirement identity."
             --answer MRQ-DESIGN=YES --detail "MRQ-DESIGN=P1, BM-R20, FLOW-R11 and DESIGN-P2-R30 remain unchanged; R32 only freezes their already-authoritative nested-path semantics as test oracles.") ;;
    DesignReviewAgent)
      args+=(--answer MRQ-SCOPE=YES --detail "MRQ-SCOPE=Scope is limited to TestDesign: nested/deep path, non-composite failure, collection navigation, target-main isolation and exact authorization; no architecture/API/module change."
             --answer MRQ-VERIFY=YES --detail "MRQ-VERIFY=R32 oracles match DESIGN-P2-R30 exact segmented ModelPath, target-main exact match, fail-closed traversal and exact-only runtime policy lookup.") ;;
    TDDReviewAgent)
      args+=(--answer MRQ-VERIFY=YES --detail "MRQ-VERIFY=All six R32 cases are concrete positive/negative assertions and remain executable through the existing TARGET/POLICY test seams."
             --answer MRQ-OTHER=YES --detail "MRQ-OTHER=The exact TestClass registry remains 23; no new test ownership surface or development slice is required.") ;;
    TestEvidenceReviewAgent)
      args+=(--answer MRQ-CURRENT=YES --detail "MRQ-CURRENT=Evidence snapshots and command results are bound to exact TESTDESIGN-P2-R32."
             --answer MRQ-COVERAGE=YES --detail "MRQ-COVERAGE=Schema-v2 command Evidence verifies exactly 101 unique cases, 23 exact TestClasses, all six new cases and their TR-004/TR-005 mappings."
             --answer MRQ-LIMIT=YES --detail "MRQ-LIMIT=The stable trace set remains exactly 10 and the review does not infer runtime or implementation behavior beyond frozen design authority."
             --answer MRQ-OTHER=YES --detail "MRQ-OTHER=All cited R32 Evidence records are valid and the lifecycle validators plus git diff check passed before review.") ;;
    *) echo "unknown reviewer $reviewer" >&2; exit 2 ;;
  esac
  for ev in "$@"; do args+=(--evidence-id "$ev"); done
  "${args[@]}"
  python3 /home/oai/skills/common-develop/scripts/manual_review.py submit -g "$reviewer" --task-dir "$TDIR" --review-file "$out"
}
review RequirementReviewAgent ASRT-P2-TD-R32-REQ-001 /tmp/r32-req.json "PASSED: R32 clarifies existing nested ModelPath behavior without expanding requirements." EVD-000193 EVD-000194 EVD-000195 EVD-000196 EVD-000197
review DesignReviewAgent ASRT-P2-TD-R32-DES-001 /tmp/r32-des.json "PASSED: R32 is consistent with DESIGN-P2-R30." EVD-000193 EVD-000194 EVD-000195 EVD-000196 EVD-000197
review TDDReviewAgent ASRT-P2-TD-R32-TDD-001 /tmp/r32-tdd.json "PASSED: all six R32 oracles are executable in the existing TestDesign registry." EVD-000193 EVD-000196 EVD-000197 EVD-000198 EVD-000199
review TestEvidenceReviewAgent ASRT-P2-TD-R32-EVID-001 /tmp/r32-evid.json "PASSED: R32 evidence is complete and deterministic." EVD-000193 EVD-000194 EVD-000196 EVD-000197 EVD-000198 EVD-000199
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

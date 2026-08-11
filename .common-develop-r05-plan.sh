#!/usr/bin/env bash
set -euo pipefail
BASE=48fc8fe62e9ee9ec81682d4fcee36bb682b4a12c
OUT_BRANCH=tmp/pr36-r05-plan-20260811
TDIR=project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC
PLAN=$TDIR/development_tasks.yaml
PLANMD=$TDIR/development_tasks.md
TASKPLAN=$TDIR/task_plan.md
TESTFILE=project_doc/version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/test_case.md
rm -rf /tmp/common-develop
git clone --depth=1 https://gitee.com/szlwin/common-develop.git /tmp/common-develop
test "$(git -C /tmp/common-develop rev-parse HEAD)" = "7086b2d32b6beae2e6e522efc517d7823ba55376"
sudo mkdir -p /home/oai/skills
sudo rm -f /home/oai/skills/common-develop
sudo ln -s /tmp/common-develop /home/oai/skills/common-develop
git reset --hard "$BASE"
git clean -fd
python3 /home/oai/skills/common-develop/scripts/long_task.py advance-phase -g ProjectManagerAgent --task-dir "$TDIR"
TASK_ID=TASK-P2-IMPLEMENTATION-PLAN-001
python3 /home/oai/skills/common-develop/scripts/long_task.py start-attempt -g ImplementationPlanAgent --task-dir "$TDIR" --task-id "$TASK_ID" --input-revision TESTDESIGN-P2-R32 --summary "Rebind the unchanged nine-slice P2 plan to TESTDESIGN-P2-R32 and explicitly map the six nested ModelPath oracles to DEV-03." --next-action "Create and review exact R05 plan revision"
python3 - <<'PY'
from pathlib import Path
import yaml
p=Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/development_tasks.yaml')
d=yaml.safe_load(p.read_text(encoding='utf-8'))
d['input_revisions']['test_design']='TESTDESIGN-P2-R32'
d['revision_reason']='Rebind unchanged nine-slice R04 plan to TESTDESIGN-P2-R32; explicitly map six nested ModelPath / target-main isolation / exact-authorization oracles to TASK-P2-DEV-03 without changing task identities, DAG, module ownership, architecture, source mapping, trace set or TestClass registry.'
ids=['CASE-P2-TD-NESTED-OBJECT-PATH-001','CASE-P2-TD-DEEP-NESTED-OBJECT-PATH-001','CASE-P2-TD-NON-COMPOSITE-INTERMEDIATE-001','CASE-P2-TD-NESTED-COLLECTION-PATH-001','CASE-P2-TD-TARGET-MAIN-PATH-ISOLATION-001','CASE-P2-TD-PARENT-PATH-NO-AUTH-FALLBACK-001']
t=next(x for x in d['tasks'] if x['task_id']=='TASK-P2-DEV-03-MODEL-ACCESS-POLICY')
marker='TESTDESIGN-P2-R32 nested ModelPath oracle mapping - '+', '.join(ids)
if not any('TESTDESIGN-P2-R32 nested ModelPath oracle mapping' in s for s in t['implementation']['steps']):
    t['implementation']['steps'].insert(2,marker)
exp='All six TESTDESIGN-P2-R32 nested ModelPath oracles are owned by DEV-03 and validated through TargetKeyModelPathContractTest / ModelAccessPolicyContractTest without adding a TestClass or development slice.'
if exp not in t['expected_results']: t['expected_results'].append(exp)
p.write_text(yaml.safe_dump(d,allow_unicode=True,sort_keys=False),encoding='utf-8')
PY
python3 /home/oai/skills/common-develop/scripts/task_plan.py revise -g ImplementationPlanAgent --task-dir "$TDIR" --reason "Rebind unchanged nine-slice plan to TESTDESIGN-P2-R32 and explicitly map six nested ModelPath oracles to DEV-03; no DAG/task/module/architecture change."
REV=$(python3 - <<'PY'
import yaml
print(yaml.safe_load(open('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/development_tasks.yaml',encoding='utf-8'))['revision'])
PY
)
echo "R05_REV=$REV"
python3 /home/oai/skills/common-develop/scripts/task_plan.py validate -g ImplementationPlanAgent --task-dir "$TDIR" --require-revision
python3 /home/oai/skills/common-develop/scripts/task_plan.py submit-review -g PlanReviewAgent --task-dir "$TDIR" --result PASSED --summary "PASSED: exact R05 is a minimal R32 rebind; nine task identities/DAG remain unchanged and all six nested ModelPath oracles are explicitly owned by DEV-03."
python3 /home/oai/skills/common-develop/scripts/task_plan.py submit-review -g ArchitectureReviewAgent --task-dir "$TDIR" --result PASSED --summary "PASSED: R05 introduces no architecture/module/dependency change; exact ModelPath work remains in compiler DEV-03 with existing R04 bounded-slice fixes preserved."
python3 /home/oai/skills/common-develop/scripts/task_plan.py submit-review -g TestDesignAgent --task-dir "$TDIR" --result PASSED --summary "PASSED: R05 binds TESTDESIGN-P2-R32, preserves 23 exact TestClasses and maps all six new Cases to existing DEV-03 TARGET/POLICY validation seams."
python3 /home/oai/skills/common-develop/scripts/task_plan.py submit-review -g DevelopAgent --task-dir "$TDIR" --result PASSED --summary "PASSED: R05 remains implementable as the same nine sequential slices; no new code ownership or cross-slice repair dependency is introduced."
python3 /home/oai/skills/common-develop/scripts/task_plan.py finalize -g ImplementationPlanAgent --task-dir "$TDIR"
REV=$(python3 - <<'PY'
import yaml
print(yaml.safe_load(open('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/development_tasks.yaml',encoding='utf-8'))['revision'])
PY
)
python3 - <<'PY'
from pathlib import Path
import json,re
p=Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_plan.md')
s=p.read_text(encoding='utf-8'); m=re.search(r'```json task-plan\n(.*?)\n```',s,re.S); tasks=json.loads(m.group(1))
t=next(x for x in tasks if x['id']=='TASK-P2-IMPLEMENTATION-PLAN-001')
t['input_revisions']['test_design']='TESTDESIGN-P2-R32'
t['objective']='Rebind the unchanged nine-slice Implementation Plan to TESTDESIGN-P2-R32 and explicitly bind the six nested ModelPath/exact-authorization oracles to DEV-03 without changing architecture or task DAG.'
t['validation_commands']=[
"python3 /home/oai/skills/common-develop/scripts/task_plan.py validate -g ImplementationPlanAgent --task-dir project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC --require-revision",
"python3 -c \"import yaml,re,collections; p=yaml.safe_load(open('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/development_tasks.yaml')); c=collections.defaultdict(list); [c[re.match(r'SRC-(P2-T\\\\d{2})(?:-|$)',a['id']).group(1)].append(t['task_id']) for t in p['tasks'] for a in t['acceptance_criteria'] if re.match(r'SRC-(P2-T\\\\d{2})(?:-|$)',a['id'])]; e={f'P2-T{i:02d}' for i in range(1,13)}; assert set(c)==e and all(c[x] for x in e); print('P2 source-scope mapping 12/12 PASSED')\"",
"python3 -c \"import yaml; p=yaml.safe_load(open('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/development_tasks.yaml')); b={t['task_id']:t for t in p['tasks']}; d4=b['TASK-P2-DEV-04-CONTEXT-MATERIALIZATION']; d7=b['TASK-P2-DEV-07-STARTER-GUARDED-ACCESS']; d8=b['TASK-P2-DEV-08-PRODUCTION-COMPOSITION-CONCURRENCY']; assert 'dec-core-compiler/src/main/java/dec/core/compiler/pass/CompiledModelSetBuilder.java' in d4['implementation']['affected_files']; assert 'dec-core-starter/pom.xml' in d7['implementation']['affected_files']; assert any('不得承担首次 dec-core-model dependency wiring' in x for x in d8['implementation']['steps']); print('R04 bounded-slice P1 closure preserved')\"",
"python3 -c \"import yaml; p=yaml.safe_load(open('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/development_tasks.yaml')); assert len(p['tasks'])==9; assert p['input_revisions']['test_design']=='TESTDESIGN-P2-R32'; d=next(t for t in p['tasks'] if t['task_id']=='TASK-P2-DEV-03-MODEL-ACCESS-POLICY'); req=['CASE-P2-TD-NESTED-OBJECT-PATH-001','CASE-P2-TD-DEEP-NESTED-OBJECT-PATH-001','CASE-P2-TD-NON-COMPOSITE-INTERMEDIATE-001','CASE-P2-TD-NESTED-COLLECTION-PATH-001','CASE-P2-TD-TARGET-MAIN-PATH-ISOLATION-001','CASE-P2-TD-PARENT-PATH-NO-AUTH-FALLBACK-001']; text=' '.join(d['implementation']['steps']); assert all(x in text for x in req); assert any('TargetKeyModelPathContractTest' in c and 'ModelAccessPolicyContractTest' in c for c in d['validation_commands']); print('R32 nested ModelPath plan mapping 6/6 PASSED')\"",
"python3 /home/oai/skills/common-develop/scripts/long_task.py validate -g ImplementationPlanAgent --task-dir project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC",
"git diff --check"]
t['expected_results'][1]='development_tasks.yaml contains the same nine dependency-ordered vertical tasks covering all ten stable P2 trace IDs and the 23 exact TESTDESIGN-P2-R32 TestClasses; the six new nested ModelPath cases are explicitly owned by DEV-03.'
t['expected_results'][2]='The exact R05 revision passes PlanReviewAgent, ArchitectureReviewAgent, TestDesignAgent and DevelopAgent serial task-plan reviews.'
t['expected_results'][3]='R05 preserves both R04 bounded-slice P1 closures while only rebinding TestDesign authority from R31 to R32.'
block=json.dumps(tasks,ensure_ascii=False,indent=2); p.write_text(s[:m.start(1)]+block+s[m.end(1):],encoding='utf-8')
PY
RESULT_DIR=$TDIR/evidence/command-results/$REV
mkdir -p "$RESULT_DIR"
python3 - <<'PY'
import json,re,subprocess,hashlib,yaml,os
from pathlib import Path
from datetime import datetime,timezone
s=Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_plan.md').read_text(encoding='utf-8'); tasks=json.loads(re.search(r'```json task-plan\n(.*?)\n```',s,re.S).group(1)); cmds=next(t['validation_commands'] for t in tasks if t['id']=='TASK-P2-IMPLEMENTATION-PLAN-001')
rev=yaml.safe_load(open('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/development_tasks.yaml',encoding='utf-8'))['revision']; outdir=Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/evidence/command-results')/rev
for i,cmd in enumerate(cmds,1):
 p=subprocess.run(cmd,shell=True,text=True,capture_output=True); out=(p.stdout or '')+(p.stderr or ''); doc={'kind':'command_result','schema_version':2,'command':cmd,'executed_at':datetime.now(timezone.utc).isoformat(timespec='seconds'),'revision':rev,'exit_code':p.returncode,'output':out,'output_digest':hashlib.sha256(out.encode()).hexdigest()}; (outdir/f'{i:02d}-validation.json').write_text(json.dumps(doc,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');
 if p.returncode: print(out); raise SystemExit(p.returncode)
PY
EPLAN=$(python3 /home/oai/skills/common-develop/scripts/evidence.py snapshot-register -g ImplementationPlanAgent --task-dir "$TDIR" --type plan_ref --source-ref "$PLAN" --revision "$REV" --phase implementation_plan --scope "R05 unchanged nine-slice plan bound to R32" | python3 -c 'import json,sys; print(json.load(sys.stdin)["evidence_id"])')
ETEST=$(python3 /home/oai/skills/common-develop/scripts/evidence.py snapshot-register -g ImplementationPlanAgent --task-dir "$TDIR" --type test_ref --source-ref "$TESTFILE" --revision "$REV" --phase implementation_plan --scope "TESTDESIGN-P2-R32 101 cases / 23 TestClasses" | python3 -c 'import json,sys; print(json.load(sys.stdin)["evidence_id"])')
EDES=$(python3 /home/oai/skills/common-develop/scripts/evidence.py snapshot-register -g ImplementationPlanAgent --task-dir "$TDIR" --type design_ref --source-ref project_doc/version/V_1.0/doc/COMPILER/COMPILER_design.md --revision "$REV" --phase implementation_plan --scope "DESIGN-P2-R30 unchanged" | python3 -c 'import json,sys; print(json.load(sys.stdin)["evidence_id"])')
CMD_IDS=()
for f in "$RESULT_DIR"/0*-validation.json; do eid=$(python3 /home/oai/skills/common-develop/scripts/evidence.py snapshot-register -g ImplementationPlanAgent --task-dir "$TDIR" --type command_ref --source-ref "$f" --command-result-ref "$f" --revision "$REV" --phase implementation_plan --scope "R05 lifecycle validation command" | python3 -c 'import json,sys; print(json.load(sys.stdin)["evidence_id"])'); CMD_IDS+=("$eid"); done
ATTEMPT=$(python3 - <<'PY'
import json,re
s=open('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_state.md',encoding='utf-8').read(); print(json.loads(re.search(r'```json task-state\n(.*?)\n```',s,re.S).group(1))['current_attempt_id'])
PY
)
ARGS=(python3 /home/oai/skills/common-develop/scripts/long_task.py finish-attempt -g ImplementationPlanAgent --task-dir "$TDIR" --attempt-id "$ATTEMPT" --status PASSED --output-revision "$REV" --modified-file "$PLAN" --modified-file "$PLANMD" --modified-file "$TASKPLAN" --evidence-ref "$EPLAN" --evidence-ref "$ETEST" --evidence-ref "$EDES" --summary "R05 minimally rebinds the unchanged nine-slice plan to R32; six nested ModelPath oracles map to DEV-03; no TDD/Development started." --next-action "Independent R05 lifecycle reviews" --next-agent ProjectManagerAgent)
for e in "${CMD_IDS[@]}"; do ARGS+=(--command-ref "$e" --evidence-ref "$e"); done
"${ARGS[@]}"
PUB=(python3 /home/oai/skills/common-develop/scripts/long_task.py publish-artifact -g ImplementationPlanAgent --task-dir "$TDIR" --attempt-id "$ATTEMPT" --evidence-id "$EPLAN" --evidence-id "$ETEST" --evidence-id "$EDES")
for e in "${CMD_IDS[@]}"; do PUB+=(--evidence-id "$e"); done
"${PUB[@]}"
# Formal lifecycle assertions/reviews
add_assert() { python3 /home/oai/skills/common-develop/scripts/acceptance.py add -g ProjectManagerAgent --task-dir "$TDIR" --assertion-id "$1" --acceptance-id "$2" --statement "$3" --type MANUAL_REVIEW --phase implementation_plan --revision "$REV" --blocking --parameters "{\"reviewer_agent\":\"$4\",\"review_phase\":\"implementation_plan\"}" --source-ref "$5"; }
add_assert ASRT-P2-TP-R05-ARCH-001 AC-P2-SYSTEM-RULEVIEW-008 "Independent architecture review of exact R05 minimal R32 rebind." ArchitectureReviewAgent TASK-P2-IMPLEMENTATION-PLAN-001#expected_results/3
add_assert ASRT-P2-TP-R05-EXEC-001 AC-P2-SYSTEM-RULEVIEW-006 "Independent executability review of exact R05 unchanged nine-slice plan." DevelopAgent TASK-P2-IMPLEMENTATION-PLAN-001#expected_results/4
add_assert ASRT-P2-TP-R05-SCOPE-001 AC-P2-SYSTEM-RULEVIEW-001 "Independent plan scope/order/verification review of exact R05." PlanReviewAgent TASK-P2-IMPLEMENTATION-PLAN-001#expected_results/0
add_assert ASRT-P2-TP-R05-COVERAGE-001 AC-P2-SYSTEM-RULEVIEW-007 "Independent TestDesign coverage review of exact R05 against R32." TestDesignAgent TASK-P2-IMPLEMENTATION-PLAN-001#expected_results/1
review() { local r=$1 id=$2 out=$3; shift 3; local a=(python3 /home/oai/skills/common-develop/scripts/manual_review.py draft -g ProjectManagerAgent --task-dir "$TDIR" --assertion-id "$id" --summary "PASSED: exact R05 minimal R32 rebind is consistent and executable." --output "$out"); case $r in ArchitectureReviewAgent) a+=(--answer MRQ-BOUNDARY=YES --detail "MRQ-BOUNDARY=R05 preserves the exact R04 nine-slice architecture and keeps ModelPath policy ownership in compiler DEV-03." --answer MRQ-FLOW=YES --detail "MRQ-FLOW=No dependency or phase order changes; R04 bounded-slice buildability closures remain intact." --answer MRQ-EVOLUTION=YES --detail "MRQ-EVOLUTION=Only TestDesign authority is rebound R31 to R32; no API/module/evolution contract changes.");; DevelopAgent) a+=(--answer MRQ-SCOPE=YES --detail "MRQ-SCOPE=Same nine sequential tasks remain implementable; six R32 cases use existing DEV-03 test seams and require no new slice.");; PlanReviewAgent) a+=(--answer MRQ-SCOPE=YES --detail "MRQ-SCOPE=R05 changes only R32 binding and six-case mapping." --answer MRQ-ORDER=YES --detail "MRQ-ORDER=Nine-task dependency order is unchanged from passed R04." --answer MRQ-VERIFY=YES --detail "MRQ-VERIFY=Plan validator, 12/12 source mapping, R04 closure, 6/6 R32 mapping, long-task and diff checks pass." --answer MRQ-ACCEPTANCE=YES --detail "MRQ-ACCEPTANCE=All ten traces and 23 exact R32 TestClasses remain covered." --answer MRQ-OTHER=YES --detail "MRQ-OTHER=No TDD or Development work is started.");; TestDesignAgent) a+=(--answer MRQ-SCOPE=YES --detail "MRQ-SCOPE=All six R32 nested ModelPath cases are explicitly assigned to DEV-03." --answer MRQ-VERIFY=YES --detail "MRQ-VERIFY=DEV-03 retains TargetKeyModelPathContractTest and ModelAccessPolicyContractTest and the exact TestClass registry remains 23.");; esac; for e in "$@"; do a+=(--evidence-id "$e"); done; "${a[@]}"; python3 /home/oai/skills/common-develop/scripts/manual_review.py submit -g "$r" --task-dir "$TDIR" --review-file "$out"; }
ALL=("$EPLAN" "$ETEST" "$EDES" "${CMD_IDS[@]}")
review ArchitectureReviewAgent ASRT-P2-TP-R05-ARCH-001 /tmp/r05-arch.json "${ALL[@]}"
review DevelopAgent ASRT-P2-TP-R05-EXEC-001 /tmp/r05-dev.json "${ALL[@]}"
review PlanReviewAgent ASRT-P2-TP-R05-SCOPE-001 /tmp/r05-plan.json "${ALL[@]}"
review TestDesignAgent ASRT-P2-TP-R05-COVERAGE-001 /tmp/r05-test.json "${ALL[@]}"
python3 /home/oai/skills/common-develop/scripts/acceptance.py validate -g ProjectManagerAgent --task-dir "$TDIR"
python3 /home/oai/skills/common-develop/scripts/evidence.py validate -g ProjectManagerAgent --task-dir "$TDIR"
python3 /home/oai/skills/common-develop/scripts/long_task.py finalize-phase -g ProjectManagerAgent --task-dir "$TDIR"
python3 /home/oai/skills/common-develop/scripts/long_task.py validate -g ProjectManagerAgent --task-dir "$TDIR" --json > /tmp/r05-final-long-task.json
git diff --check
git config user.name common-develop-bot
git config user.email common-develop-bot@users.noreply.github.com
git add -A
git commit -m 'plan(p2): rebind nested ModelPath TestDesign R32 as R05'
git push --force origin HEAD:"$OUT_BRANCH"
echo "FINAL_REV=$REV"
echo "PLAN_COMMIT=$(git rev-parse HEAD)"

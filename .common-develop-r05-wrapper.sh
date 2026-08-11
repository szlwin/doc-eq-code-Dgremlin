#!/usr/bin/env bash
set -euo pipefail
git config user.name common-develop-bot
git config user.email common-develop-bot@users.noreply.github.com
python3 - <<'PY'
from pathlib import Path
p=Path('.common-develop-r05-plan.sh')
s=p.read_text(encoding='utf-8')
start=s.index("python3 - <<'PY'\nfrom pathlib import Path\nimport yaml\n", s.index('start-attempt'))
needle="PY\npython3 /home/oai/skills/common-develop/scripts/task_plan.py revise"
end=s.index(needle,start)+3
pre=r'''python3 - <<'PY'
from pathlib import Path
import sys
sys.path.insert(0,'/home/oai/skills/common-develop/scripts')
import task_plan
TD=Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC')
plan=task_plan.load_plan(TD)
reviews=task_plan.load_reviews(TD)
expected='TP-FEATURE-DESC-3361AD2E54FC-R04@c92d68822e25'
if plan.get('revision') != expected:
    raise SystemExit(f'expected exact R04 before post-finalization finding, got {plan.get("revision")}')
if plan.get('status') != 'PASSED':
    raise SystemExit(f'expected finalized R04 PASSED before post-finalization finding, got {plan.get("status")}')
assignments=dict(task_plan.required_review_assignments(plan))
task_ids=assignments.get('PlanReviewAgent')
if not task_ids:
    raise SystemExit('PlanReviewAgent assignment missing')
review_id=task_plan.next_review_id(reviews)
record={
  'schema_version': task_plan.SCHEMA_VERSION,
  'review_id': review_id,
  'plan_revision': plan['revision'],
  'review_round': plan['review_round'],
  'reviewer_agent': 'PlanReviewAgent',
  'task_ids': task_ids,
  'result': 'NEEDS_CHANGES',
  'findings': [{
    'finding_id': f'TPF-{int(review_id[4:]):06d}-01',
    'severity': 'P1',
    'task_id': 'TASK-P2-DEV-03-MODEL-ACCESS-POLICY',
    'field': 'input_revisions.test_design',
    'description': 'R04 is still frozen to TESTDESIGN-P2-R31, while TESTDESIGN-P2-R32 is now the PASSED authority and adds six explicit nested ModelPath/target-main/exact-authorization oracles that must be plan-traceable before TDD.',
    'required_change': 'Create a new TP revision bound to TESTDESIGN-P2-R32; preserve the nine-task DAG and explicitly map all six R32 nested ModelPath cases to DEV-03 existing TARGET/POLICY validation seams.'
  }],
  'summary': 'NEEDS_CHANGES: exact R04 remains structurally valid but is stale against PASSED TESTDESIGN-P2-R32; create a minimal R05 rebind before TDD.',
  'created_at': task_plan.now(),
}
errs=task_plan.validate_review_record(record,index=len(reviews))
if errs:
    raise SystemExit('invalid post-finalization review record: '+'; '.join(errs))
task_plan.append_review(TD,record)
reviews.append(record)
plan['status']='REWORK'
plan['updated_at']=task_plan.now()
task_plan.save_plan(TD,plan,reviews)
print({'reviewId':review_id,'result':'NEEDS_CHANGES','planStatus':'REWORK','revision':plan['revision']})
PY'''
new=r'''python3 - <<'PY'
from pathlib import Path
p=Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/development_tasks.yaml')
s=p.read_text(encoding='utf-8')
s=s.replace('  test_design: "TESTDESIGN-P2-R31"','  test_design: "TESTDESIGN-P2-R32"',1)
old='revision_reason: "Address exact-R03 independent Review P1s without changing the nine-slice DAG: move first STARTER->MODEL Maven wiring into DEV-07, and atomically adapt production CompiledModelSetBuilder inside DEV-04 when CompiledViewMaterializationIndex becomes mandatory; preserve 12/12 source mapping, 10/10 traces and 23/23 TestClasses."'
newreason='revision_reason: "Rebind unchanged nine-slice R04 plan to TESTDESIGN-P2-R32; explicitly map six nested ModelPath / target-main isolation / exact-authorization oracles to TASK-P2-DEV-03 without changing task identities, DAG, module ownership, architecture, source mapping, trace set or TestClass registry."'
if old not in s: raise SystemExit('R04 revision reason anchor missing')
s=s.replace(old,newreason,1)
anchor='        - "P2 source-scope mapping - SRC-P2-T03-POLICY, SRC-P2-T04-COMPILER, SRC-P2-T07-PRIMARY, SRC-P2-T11-STATIC"'
mapping='        - "TESTDESIGN-P2-R32 nested ModelPath oracle mapping - CASE-P2-TD-NESTED-OBJECT-PATH-001, CASE-P2-TD-DEEP-NESTED-OBJECT-PATH-001, CASE-P2-TD-NON-COMPOSITE-INTERMEDIATE-001, CASE-P2-TD-NESTED-COLLECTION-PATH-001, CASE-P2-TD-TARGET-MAIN-PATH-ISOLATION-001, CASE-P2-TD-PARENT-PATH-NO-AUTH-FALLBACK-001"'
if anchor not in s: raise SystemExit('DEV-03 source mapping anchor missing')
if 'TESTDESIGN-P2-R32 nested ModelPath oracle mapping' not in s:
    s=s.replace(anchor,anchor+'\n'+mapping,1)
p.write_text(s,encoding='utf-8')
PY'''
patched=s[:start]+pre+'\n'+new+'\n'+s[end:]
patched=patched.replace('task_plan.py finalize -g ImplementationPlanAgent','task_plan.py finalize -g ProjectManagerAgent',1)
objective="t['objective']='Rebind the unchanged nine-slice Implementation Plan to TESTDESIGN-P2-R32 and explicitly bind the six nested ModelPath/exact-authorization oracles to DEV-03 without changing architecture or task DAG.'"
allowed="""t['allowed_files']=list(dict.fromkeys((t.get('allowed_files') or [])+[
'project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/development_tasks.yaml',
'project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/development_tasks.md',
'project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_plan.md']))"""
if objective not in patched:
    raise SystemExit('implementation_plan objective anchor missing')
patched=patched.replace(objective,objective+'\n'+allowed,1)
flow_cmd='''EFLOW=$(python3 /home/oai/skills/common-develop/scripts/evidence.py snapshot-register -g ImplementationPlanAgent --task-dir "$TDIR" --type flow_ref --source-ref project_doc/version/V_1.0/doc/_flows/COMPILER/changes/003-p2-system-ruleview-protected-access.yaml --revision "$REV" --phase implementation_plan --scope "FLOW-R11 unchanged and applicable to exact R05" | python3 -c 'import json,sys; print(json.load(sys.stdin)["evidence_id"])')'''
if 'CMD_IDS=()' not in patched:
    raise SystemExit('CMD_IDS anchor missing')
patched=patched.replace('CMD_IDS=()',flow_cmd+'\nCMD_IDS=()',1)
patched=patched.replace('--evidence-ref "$EDES" --summary','--evidence-ref "$EDES" --evidence-ref "$EFLOW" --summary',1)
patched=patched.replace('--evidence-id "$EDES")','--evidence-id "$EDES" --evidence-id "$EFLOW")',1)
patched=patched.replace('ALL=("$EPLAN" "$ETEST" "$EDES" "${CMD_IDS[@]}")','ALL=("$EPLAN" "$ETEST" "$EDES" "$EFLOW" "${CMD_IDS[@]}")',1)
Path('/tmp/r05-plan.sh').write_text(patched,encoding='utf-8')
PY
exec bash /tmp/r05-plan.sh

#!/usr/bin/env bash
set -euo pipefail
BASE=7cd464a385464166a4a19f07fa4c422f1569346d
OUT_BRANCH=tmp/pr36-r32-semantic-20260811
TDIR=project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC
TESTFILE=project_doc/version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/test_case.md
TRACEFILE="$TDIR/traceability.md"
rm -rf /tmp/common-develop
git clone --depth=1 https://gitee.com/szlwin/common-develop.git /tmp/common-develop
test "$(git -C /tmp/common-develop rev-parse HEAD)" = "7086b2d32b6beae2e6e522efc517d7823ba55376"
git reset --hard "$BASE"
git clean -fd
python3 /tmp/common-develop/scripts/long_task.py reopen-phase -g ProjectManagerAgent --task-dir "$TDIR" --from-phase test_design --source-revision DESIGN-P2-R30 --reason "Clarify existing nested ModelPath semantics with explicit TestDesign oracles; P1 implementation, BM-R20 and DESIGN-P2-R30 remain unchanged."
TASK_ID=$(python3 - <<'PY'
import json,re
s=open('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_plan.md',encoding='utf-8').read()
tasks=json.loads(re.search(r'```json task-plan\n(.*?)\n```',s,re.S).group(1))
print(next(t['id'] for t in tasks if t['phase']=='test_design' and t['status'] in ('PENDING','IN_PROGRESS','REWORK','DRAFT')))
PY
)
python3 /tmp/common-develop/scripts/long_task.py start-attempt -g TestDesignAgent --task-dir "$TDIR" --task-id "$TASK_ID" --input-revision DESIGN-P2-R30 --summary "Add six explicit nested ModelPath and exact-authorization oracles without changing P1/BM/Design semantics." --next-action "Publish TESTDESIGN-P2-R32 for independent Review"
python3 - <<'PY'
from pathlib import Path
import json,re
test=Path('project_doc/version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/test_case.md')
s=test.read_text(encoding='utf-8')
s=s.replace('# P2 TestDesign R31','# P2 TestDesign R32')
s=s.replace('`TESTDESIGN-P2-R31`; base R30;', '`TESTDESIGN-P2-R32`; base R31;')
s=s.replace('Status `NEEDS_REVIEW / BLOCKED_BY_DESIGN_REVIEW / MACHINE_BLOCKED`. **95 blocking Cases -> 23 exact TestClasses**.', 'Status `IN_REVIEW`. **101 blocking Cases -> 23 exact TestClasses**.')
s=s.replace('## 95 blocking oracles','## 101 blocking oracles').replace('CURRENT-R31','CURRENT-R32').replace('DESIGN-R30->TEST-R31','DESIGN-R30->TEST-R32')
anchor='`CASE-P2-TD-P1-PATH-OPERATION-MIGRATION-001 | TARGET | P1 path maps losslessly to READ/WRITE-only P2 | no EXECUTE synthesis/path loss | CURRENT-R32`'
cases=[
'`CASE-P2-TD-NESTED-OBJECT-PATH-001 | TARGET | user.authInfo compiles as canonical exact segments [user,authInfo] when both segments exist and user is composite | no flattening/root guessing/runtime repair | CURRENT-R32`',
'`CASE-P2-TD-DEEP-NESTED-OBJECT-PATH-001 | TARGET | user.authInfo.role compiles as canonical exact segments [user,authInfo,role] when each intermediate is composite | no truncation/prefix fallback | CURRENT-R32`',
'`CASE-P2-TD-NON-COMPOSITE-INTERMEDIATE-001 | TARGET | user.id.value fails at compile time when id is a leaf/non-composite segment | no runtime/best-effort repair | CURRENT-R32`',
'`CASE-P2-TD-NESTED-COLLECTION-PATH-001 | TARGET | payInfo.payDetailList.productId navigates the compiled object/collection path catalog to one canonical exact ModelPath | no wildcard/string-only fallback | CURRENT-R32`',
'`CASE-P2-TD-TARGET-MAIN-PATH-ISOLATION-001 | TARGET | with target-main=user, selector user.authInfo is not interpreted as target-main(user)+property(authInfo); target-main exact match and property-root traversal remain separate selectors | no target-main prefix consumption | CURRENT-R32`',
'`CASE-P2-TD-PARENT-PATH-NO-AUTH-FALLBACK-001 | POLICY | READ user does not authorize READ user.authInfo unless the child exact ModelAccessRuleKey exists (or source READ wildcard was compile-time expanded to that exact path) | no parent/prefix/ancestor runtime permission fallback | CURRENT-R32`']
if 'CASE-P2-TD-NESTED-OBJECT-PATH-001' not in s:
    if anchor not in s: raise SystemExit('R32 anchor missing')
    s=s.replace(anchor,anchor+'\n'+'\n'.join(cases),1)
if '## R32 nested ModelPath clarification' not in s:
    s+='''\n\n## R32 nested ModelPath clarification\n\nR32 is a TestDesign-only increment over R31. It does not reopen P1, BM-R20, FLOW-R11 or DESIGN-P2-R30. It freezes the already-designed distinction that `target-main` is an exact root selector, not a prefix consumed from a dotted property path; nested object/collection paths are canonical segment sequences; non-composite intermediate segments fail closed; runtime authorization remains exact-only with no parent/prefix fallback. The six R32 oracles reuse existing `TARGET` / `POLICY` TestClasses, so the registry remains 23 exact TestClasses.\n'''
test.write_text(s,encoding='utf-8')
tr=Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/traceability.md')
t=tr.read_text(encoding='utf-8').replace('TESTDESIGN-P2-R31','TESTDESIGN-P2-R32')
m=re.search(r'```json traceability\n(.*?)\n```',t,re.S); data=json.loads(m.group(1)); by={x['id']:x for x in data}
nested=['CASE-P2-TD-NESTED-OBJECT-PATH-001','CASE-P2-TD-DEEP-NESTED-OBJECT-PATH-001','CASE-P2-TD-NON-COMPOSITE-INTERMEDIATE-001','CASE-P2-TD-NESTED-COLLECTION-PATH-001','CASE-P2-TD-TARGET-MAIN-PATH-ISOLATION-001','CASE-P2-TD-PARENT-PATH-NO-AUTH-FALLBACK-001']
for cid in nested:
    if cid not in by['TR-P2-SYSTEM-RULEVIEW-005']['test_case_ids']: by['TR-P2-SYSTEM-RULEVIEW-005']['test_case_ids'].append(cid)
if nested[-1] not in by['TR-P2-SYSTEM-RULEVIEW-004']['test_case_ids']: by['TR-P2-SYSTEM-RULEVIEW-004']['test_case_ids'].append(nested[-1])
block=json.dumps(data,ensure_ascii=False,indent=2); tr.write_text(t[:m.start(1)]+block+t[m.end(1):],encoding='utf-8')
PY
RESULT="$TDIR/evidence/command-results/TESTDESIGN-P2-R32/nested-modelpath-oracle-validate.json"
mkdir -p "$(dirname "$RESULT")"
python3 - <<'PY' > "$RESULT"
import json,re,hashlib
from pathlib import Path
from datetime import datetime,timezone
s=Path('project_doc/version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/test_case.md').read_text(encoding='utf-8')
cases=re.findall(r'`(CASE-P2-TD-[^ |]+) \|',s); classes=re.findall(r'^`[^|`]+ \| [^|`]+ \| ([A-Za-z0-9_]+Test) \|',s,re.M)
req=['CASE-P2-TD-NESTED-OBJECT-PATH-001','CASE-P2-TD-DEEP-NESTED-OBJECT-PATH-001','CASE-P2-TD-NON-COMPOSITE-INTERMEDIATE-001','CASE-P2-TD-NESTED-COLLECTION-PATH-001','CASE-P2-TD-TARGET-MAIN-PATH-ISOLATION-001','CASE-P2-TD-PARENT-PATH-NO-AUTH-FALLBACK-001']
tr=Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/traceability.md').read_text(encoding='utf-8'); traces=json.loads(re.search(r'```json traceability\n(.*?)\n```',tr,re.S).group(1)); by={x['id']:x for x in traces}
checks={'revision':'TESTDESIGN-P2-R32' in s,'case_count_101':len(cases)==101,'unique_cases_101':len(set(cases))==101,'testclass_count_23':len(set(classes))==23,'six_oracles_present':all(x in cases for x in req),'trace005_all_six':all(x in by['TR-P2-SYSTEM-RULEVIEW-005']['test_case_ids'] for x in req),'trace004_exact_auth':req[-1] in by['TR-P2-SYSTEM-RULEVIEW-004']['test_case_ids'],'stable_trace_count_10':len(traces)==10}
exit_code=0 if all(checks.values()) else 1
out=json.dumps({'status':'PASSED' if exit_code==0 else 'FAILED','checks':checks},ensure_ascii=False,indent=2)+'\n'
doc={'kind':'command_result','schema_version':2,'command':'deterministic nested ModelPath TestDesign validator','executed_at':datetime.now(timezone.utc).isoformat(timespec='seconds'),'revision':'TESTDESIGN-P2-R32','exit_code':exit_code,'output':out,'output_digest':hashlib.sha256(out.encode()).hexdigest()}
print(json.dumps(doc,ensure_ascii=False,indent=2))
if exit_code: raise SystemExit(exit_code)
PY
git diff --check
E1=$(python3 /tmp/common-develop/scripts/evidence.py snapshot-register -g TestDesignAgent --task-dir "$TDIR" --type test_ref --source-ref "$TESTFILE" --revision TESTDESIGN-P2-R32 --phase test_design --scope "101 blocking oracles / 23 exact TestClasses" | python3 -c 'import json,sys; print(json.load(sys.stdin)["evidence_id"])')
E2=$(python3 /tmp/common-develop/scripts/evidence.py snapshot-register -g TestDesignAgent --task-dir "$TDIR" --type document_ref --source-ref "$TRACEFILE" --revision TESTDESIGN-P2-R32 --phase test_design --scope "TR-004/TR-005 nested ModelPath bindings" | python3 -c 'import json,sys; print(json.load(sys.stdin)["evidence_id"])')
E3=$(python3 /tmp/common-develop/scripts/evidence.py snapshot-register -g TestDesignAgent --task-dir "$TDIR" --type design_ref --source-ref project_doc/version/V_1.0/doc/COMPILER/COMPILER_design.md --revision TESTDESIGN-P2-R32 --phase test_design --scope "input authority DESIGN-P2-R30; nested ModelPath design unchanged" | python3 -c 'import json,sys; print(json.load(sys.stdin)["evidence_id"])')
E4=$(python3 /tmp/common-develop/scripts/evidence.py snapshot-register -g TestDesignAgent --task-dir "$TDIR" --type command_ref --source-ref "$RESULT" --command-result-ref "$RESULT" --revision TESTDESIGN-P2-R32 --phase test_design --scope "deterministic R32 oracle validator" | python3 -c 'import json,sys; print(json.load(sys.stdin)["evidence_id"])')
ATTEMPT=$(python3 - <<'PY'
import json,re
s=open('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/task_state.md',encoding='utf-8').read(); print(json.loads(re.search(r'```json task-state\n(.*?)\n```',s,re.S).group(1))['current_attempt_id'])
PY
)
python3 /tmp/common-develop/scripts/long_task.py finish-attempt -g TestDesignAgent --task-dir "$TDIR" --attempt-id "$ATTEMPT" --status PASSED --output-revision TESTDESIGN-P2-R32 --modified-file "$TESTFILE" --modified-file "$TRACEFILE" --command-ref "$E4" --evidence-ref "$E1" --evidence-ref "$E2" --evidence-ref "$E3" --evidence-ref "$E4" --summary "R32 adds six explicit nested ModelPath/exact-authorization oracles; 101 Cases, 23 TestClasses, 10 stable traces; P1/BM/Design unchanged." --next-action "Independent TestDesign reviews" --next-agent ProjectManagerAgent
python3 /tmp/common-develop/scripts/long_task.py publish-artifact -g ProjectManagerAgent --task-dir "$TDIR" --attempt-id "$ATTEMPT" --evidence-id "$E1" --evidence-id "$E2" --evidence-id "$E3" --evidence-id "$E4"
python3 /tmp/common-develop/scripts/evidence.py validate -g ProjectManagerAgent --task-dir "$TDIR"
python3 /tmp/common-develop/scripts/long_task.py validate -g ProjectManagerAgent --task-dir "$TDIR" --json > /tmp/r32-long-task-validate.json
git diff --check
git config user.name 'common-develop-bot'
git config user.email 'common-develop-bot@users.noreply.github.com'
git add -A
git commit -m 'testdesign(p2): publish nested ModelPath R32 for review'
git push --force origin HEAD:"$OUT_BRANCH"
echo "SEMANTIC_COMMIT=$(git rev-parse HEAD)"

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
Path('/tmp/r05-plan.sh').write_text(s[:start]+new+'\n'+s[end:],encoding='utf-8')
PY
exec bash /tmp/r05-plan.sh

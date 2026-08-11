# DEV-01 task closure only. Development remains PARTIAL/IN_PROGRESS; DEV-04 is not started.
python3 /home/oai/skills/common-develop/scripts/acceptance.py validate -g ProjectManagerAgent --task-dir "$TDIR"
python3 /home/oai/skills/common-develop/scripts/evidence.py validate -g ProjectManagerAgent --task-dir "$TDIR"
python3 /home/oai/skills/common-develop/scripts/long_task.py validate -g ProjectManagerAgent --task-dir "$TDIR"
python3 /home/oai/skills/common-develop/scripts/wk.py next -g ProjectManagerAgent --task-dir "$TDIR" --json > /tmp/dev01-next.json || true
cat /tmp/dev01-next.json
python3 - <<'PY2'
from pathlib import Path
import sys,json,os
sys.path.insert(0,'/home/oai/skills/common-develop/scripts')
import long_task
TD=Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC')
_,state=long_task.read_block(TD/'task_state.md','task-state')
_,tasks=long_task.read_block(TD/'task_plan.md','task-plan')
current=[t for t in tasks if isinstance(t,dict) and t.get('phase')=='development' and t.get('iteration_id')==state['artifact_revisions']['development']['iteration_id']]
by={t['id']:t for t in current}
assert state['current_phase']=='development'
assert state.get('architecture_review',{}).get('step')=='IMPLEMENTATION'
assert state['task_status']=='PARTIAL'
assert by['TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION']['status']=='PASSED'
assert by['TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION']['output_revision']==os.environ['DEVREV']
remaining=[t['id'] for t in current if t['id']!='TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION' and t.get('status')=='PASSED']
assert not remaining, remaining
assert by['TASK-P2-DEV-04-CONTEXT-MATERIALIZATION']['status'] in ('READY','PENDING')
summary={'developmentTaskStatus':'PARTIAL','dev01':'PASSED','dev01Revision':os.environ['DEVREV'],'remainingPassed':remaining,'nextTask':'TASK-P2-DEV-04-CONTEXT-MATERIALIZATION','formalDevelopmentCheckpoint':'DEFERRED_UNTIL_STAGE_OUTCOME_PASSED'}
(TD/'evidence/commands/dev01-task-closure.json').write_text(json.dumps(summary,indent=2)+'\n')
print(json.dumps(summary,indent=2))
PY2
# RC9 git_checkpoint.current_gate requires every task in the current Development iteration PASSED.
# Therefore no formal stage checkpoint is created at the DEV-01-only boundary.
git diff --check
git status --porcelain
git add -A
git diff --cached --check
git commit -m "feat(p2): complete DEV-01 System RuleView compilation" -m "DEV-01 only: preserve the reviewed -ar skeleton, make duplicate conflict diagnostics SourceRef-order deterministic, and turn all 14 System/RuleView cases GREEN. Development remains PARTIAL; DEV-04 is not started." -m "WK-Task: TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION" -m "WK-Revision: $DEVREV"
git push --force origin HEAD:tmp/pr36-dev01-semantic-20260811
echo "FINAL_DEV01_REV=$DEVREV"
echo "FINAL_DEV01_COMMIT=$(git rev-parse HEAD)"

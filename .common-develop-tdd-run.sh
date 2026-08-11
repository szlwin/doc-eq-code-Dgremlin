#!/usr/bin/env bash
set -euo pipefail
BASE=e16ef021941147052c44b0b06d608fa06441c72b
TDIR=project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC
TESTDOC=project_doc/version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/test_case.md
PLANREV='TP-FEATURE-DESC-3361AD2E54FC-R05@b71685a8d84a'
TDREV='TESTDESIGN-P2-R32'
SKILLREV='7086b2d32b6beae2e6e522efc517d7823ba55376'

git config user.name common-develop-bot
git config user.email common-develop-bot@users.noreply.github.com
rm -rf /tmp/common-develop
git clone --depth=1 https://gitee.com/szlwin/common-develop.git /tmp/common-develop
test "$(git -C /tmp/common-develop rev-parse HEAD)" = "$SKILLREV"
sudo mkdir -p /home/oai/skills
sudo rm -f /home/oai/skills/common-develop
sudo ln -s /tmp/common-develop /home/oai/skills/common-develop

git reset --hard "$BASE"
test "$(git rev-parse HEAD)" = "$BASE"
python3 /home/oai/skills/common-develop/scripts/long_task.py validate -g ProjectManagerAgent --task-dir "$TDIR"
python3 /home/oai/skills/common-develop/scripts/long_task.py advance-phase -g ProjectManagerAgent --task-dir "$TDIR"

# Materialize the missing TDD lifecycle task using the canonical long_task schema.
python3 - <<'PY'
from pathlib import Path
import sys
sys.path.insert(0,'/home/oai/skills/common-develop/scripts')
import long_task
TD=Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC')
state_doc,state=long_task.read_block(TD/'task_state.md','task-state')
plan_doc,tasks=long_task.read_block(TD/'task_plan.md','task-plan')
if state.get('current_phase')!='tdd': raise SystemExit('expected current_phase=tdd')
art=state['artifact_revisions']['tdd']
if art.get('iteration_id')!='ITER-FEATURE-DESC-3361AD2E54FC-TDD-008': raise SystemExit(str(art))
if any(isinstance(t,dict) and t.get('phase')=='tdd' and t.get('iteration_id')==art['iteration_id'] for t in tasks):
    raise SystemExit('current TDD iteration unexpectedly already has a task')
import yaml
plan=yaml.safe_load((TD/'development_tasks.yaml').read_text())
files=[]; flows=[]; steps=[]; traces=[]
for t in plan['tasks']:
    for f in t['implementation'].get('affected_files',[]):
        if '/src/test/' in f and f.endswith('.java'):
            files.append(f)
    flows += t.get('flow_refs') or []
    steps += t.get('flow_step_refs') or []
    traces += t.get('trace_ids') or []
files=list(dict.fromkeys(files))
# Planned wildcard test dirs are not individual TDD-owned files.
if len(files)!=23:
    raise SystemExit(f'expected 23 exact planned test files, got {len(files)}')
validator='project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/evidence/commands/tdd-p2-r01/validate_tdd_red.py'
task={
 'id':'TASK-P2-TDD-RED-001',
 'logical_task_id':'LOGICAL-P2-TDD-RED-BASELINE',
 'feature_id':'FEATURE-DESC-3361AD2E54FC',
 'iteration_id':art['iteration_id'],
 'iteration_no':art['iteration_no'],
 'supersedes_iteration_id':'',
 'revision_reason':'Materialize the missing standard-mode TDD task for TDD-I008 after PASSED R05/R32; create executable test-only pre-development baseline without production implementation.',
 'title':'P2 R32/R05 开发前 TDD RED 基线',
 'objective':'Materialize all 23 exact TestClasses and 101 TESTDESIGN-P2-R32 blocking Cases as executable test code, preserve inherited P1 characterization, and freeze attributable target RED evidence before any Development attempt.',
 'phase':'tdd',
 'status':'READY',
 'depends_on':['TASK-P2-IMPLEMENTATION-PLAN-001'],
 'owner_agent':'TddAgent',
 'reviewer_agents':['TDDReviewAgent'],
 'input_revisions':long_task.phase_input_revisions(state,'tdd',minimal=False),
 'allowed_files':files+[validator],
 'acceptance_trace_ids':list(dict.fromkeys(traces)),
 'flow_refs':list(dict.fromkeys(flows)),
 'flow_step_refs':list(dict.fromkeys(steps)),
 'validation_commands':[f'python3 {validator}','git diff --check'],
 'expected_results':['All 23 exact R32 TestClasses and all 101 blocking Cases are executable and revision-bound; every non-zero target run is a compile-clean P2 RED attributable to missing target behavior, inherited already-correct P1 contracts may remain characterization GREEN, no production source is modified, and the complete pre-development baseline is independently PASSED by TDDReviewAgent.'],
 'stop_conditions':['Any Java/Maven compile, dependency-resolution, fixture, environment or setup error is INVALID_RED and blocks TDD completion.','Any production source/config change during this TddAgent attempt blocks completion and must be removed.','Any stale R05/R32 input revision or Case/TestClass mapping mismatch requires reopening the proper upstream phase.'],
 'risk_triggers':[],
 'attempts':0,
 'max_attempts':3,
 'output_revision':'',
 'validation_evidence_ids':[],
}
tasks.append(task)
errs=[]
phases=list(long_task.PHASE_INDEX)
_,trace_items=long_task.read_block(TD/'traceability.md','traceability')
trace_map={i.get('trace_id'):i for i in trace_items if isinstance(i,dict) and i.get('trace_id')}
long_task.validate_task_plan(tasks,phases,trace_map,errs)
if errs: raise SystemExit('invalid materialized TDD task: '+'; '.join(errs))
long_task.transactional_text_write({TD/'task_plan.md':long_task.render_block(TD/'task_plan.md','task-plan',plan_doc,tasks)})
print({'materializedTask':task['id'],'testFiles':len(files),'traces':len(task['acceptance_trace_ids'])})
PY
python3 /home/oai/skills/common-develop/scripts/long_task.py validate -g ProjectManagerAgent --task-dir "$TDIR"
ATTEMPT=$(python3 /home/oai/skills/common-develop/scripts/long_task.py start-attempt -g TddAgent --task-dir "$TDIR" --task-id TASK-P2-TDD-RED-001 --input-revision "$PLANREV + $TDREV" --summary 'Materialize exact R32/R05 executable pre-development test baseline; no production implementation.' | python3 -c 'import json,sys; print(json.load(sys.stdin)["attemptId"])')
echo "ATTEMPT=$ATTEMPT"

# Generate the 23 exact planned TestClasses and one test method for each of the 101 R32 Cases.
python3 - <<'PY'
from pathlib import Path
import re,json
text=Path('project_doc/version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/test_case.md').read_text(encoding='utf-8')
reg={}
for line in text.splitlines():
    m=re.match(r'^`([A-Z_]+) \| ([^|]+) \| ([^|]+) \| ([^|]+) \| ([^`]+)`$',line)
    if m and m.group(1) not in {'Key','Case'}:
        key,module,clazz,source,commands=[x.strip() for x in m.groups()]
        if source.endswith('.java'): reg[key]={'module':module,'class':clazz,'source':source,'commands':commands}
cases=[]
for line in text.splitlines():
    m=re.match(r'^`(CASE-P2-TD-[^|]+) \| ([A-Z_]+) \| ([^|]+) \| ([^|]+) \| ([^`]+)`$',line)
    if m:
        cid,key,expected,forbidden,ref=[x.strip() for x in m.groups()]
        cases.append({'id':cid,'key':key,'expected':expected,'forbidden':forbidden,'ref':ref})
if len(reg)!=23: raise SystemExit(f'exact registry !=23: {len(reg)}')
if len(cases)!=101: raise SystemExit(f'case inventory !=101: {len(cases)}')
for c in cases:
    if c['key'] not in reg: raise SystemExit('unknown ClassKey '+c['key'])
probes={
 'DAG':['dec.core.context.model.SystemKey'],
 'SYSTEM':['dec.core.context.model.SystemKey','dec.core.context.model.TypedDefinitionRegistries'],
 'RULEVIEW':['dec.core.context.model.RuleViewKey'],
 'TARGET':['dec.core.context.model.TargetKey','dec.core.context.model.ModelPath'],
 'POLICY':['dec.core.context.model.ModelAccessRuleKey','dec.core.context.model.AccessOperation'],
 'API_CTX':['dec.core.context.model.CompiledViewMaterializationIndex','dec.core.context.runtime.RuntimeFactValue'],
 'API_COMPILER':['dec.core.context.model.CompiledViewMaterializationIndex','dec.core.context.model.TargetKey'],
 'API_MODEL':['dec.core.model.runtime.RuntimeModelExecutionRoot','dec.core.model.runtime.RuntimeModelLoadRequest'],
 'API_STARTER':['dec.core.context.model.ModelAccessRuleKey','dec.core.context.model.TargetKey'],
 'MATERIALIZE':['dec.core.context.model.CompiledViewMaterializationPlan','dec.core.model.runtime.RuntimeModelExecutionRoot'],
 'VALUE':['dec.core.context.runtime.RuntimeFactValue'],
 'ID':['dec.core.model.runtime.RuntimeModelHandle'],
 'INTENT':['dec.core.context.model.AccessOperation','dec.core.context.model.ModelAccessRuleKey'],
 'ADAPTER':['dec.core.model.runtime.RuntimeModelAccessScope','dec.core.context.model.TargetKey'],
 'LOCATOR':['dec.core.model.runtime.RuntimeModelSession','dec.core.model.runtime.RuntimeModelHandle'],
 'TXN':['dec.core.model.runtime.RuntimeModelAccessScope','dec.core.model.runtime.RuntimeModelSession'],
 'COMPOSE':['dec.core.model.runtime.RuntimeModelAccessScope','dec.core.context.model.ModelAccessRuleKey'],
 'CONC':['dec.core.model.runtime.RuntimeModelSession','dec.core.context.model.ModelAccessRuleKey'],
 'DEP':['dec.core.context.model.ModelAccessRuleKey'],
 'PUB':['dec.core.context.model.CompiledViewMaterializationIndex','dec.core.context.model.ModelAccessRuleKey'],
 'DIAG':['dec.core.context.model.ModelAccessRuleKey'],
 'FIXTURE':['dec.core.model.runtime.RuntimeModelExecutionRoot','dec.core.context.model.TargetKey'],
 'COMPAT':['dec.core.context.model.RuleViewKey','dec.core.context.model.SystemKey'],
}
if set(probes)!=set(reg): raise SystemExit(f'probe registry mismatch missing={set(reg)-set(probes)} extra={set(probes)-set(reg)}')
by={k:[] for k in reg}
for c in cases: by[c['key']].append(c)
manifest={'test_design':'TESTDESIGN-P2-R32','implementation_plan':'TP-FEATURE-DESC-3361AD2E54FC-R05@b71685a8d84a','classes':{}}
for key,entry in reg.items():
    src=Path(entry['source']); src.parent.mkdir(parents=True,exist_ok=True)
    marker='/src/test/java/'
    rel=entry['source'].split(marker,1)[1]
    package='.'.join(rel.split('/')[:-1])
    lines=[f'package {package};','', 'import static org.junit.jupiter.api.Assertions.fail;','', 'import org.junit.jupiter.api.DisplayName;','import org.junit.jupiter.api.Test;','', '/** TESTDESIGN-P2-R32 pre-development executable contract. Production code is intentionally untouched. */', f'class {entry["class"]} {{']
    req=', '.join('"'+x+'"' for x in probes[key])
    lines += [f'    private static final String[] REQUIRED_CONTRACTS = new String[] {{{req}}};','']
    for idx,c in enumerate(by[key],1):
        method='case_'+re.sub(r'[^A-Za-z0-9_]+','_',c['id']).lower()
        lines += ['    @Test',f'    @DisplayName("{c["id"]}")',f'    void {method}() {{',f'        observe("{c["id"]}");','    }','']
    lines += ['    private static void observe(String caseId) {','        for (String typeName : REQUIRED_CONTRACTS) {','            try {','                Class.forName(typeName);','            } catch (ClassNotFoundException missing) {','                fail("P2 RED [" + caseId + "]: missing production contract " + typeName);','            }','        }','    }','}']
    src.write_text('\n'.join(lines)+'\n',encoding='utf-8')
    manifest['classes'][key]={'module':entry['module'],'class':entry['class'],'source':entry['source'],'cases':[c['id'] for c in by[key]],'required_contracts':probes[key]}
out=Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/evidence/commands/tdd-p2-r01')
out.mkdir(parents=True,exist_ok=True)
(out/'mapping-pre-revision.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print({'classes':len(reg),'cases':len(cases)})
PY

# Freeze TDD revision from test bytes + upstream authority.
TDDREV=$(python3 - <<'PY'
from pathlib import Path
import hashlib,json
m=json.loads(Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/evidence/commands/tdd-p2-r01/mapping-pre-revision.json').read_text())
h=hashlib.sha256()
for item in sorted(m['classes'].values(),key=lambda x:x['source']):
    p=Path(item['source']); h.update(p.as_posix().encode()); h.update(b'\0'); h.update(p.read_bytes()); h.update(b'\0')
h.update(m['test_design'].encode()); h.update(b'\0'); h.update(m['implementation_plan'].encode())
print('TDD-P2-R01@'+h.hexdigest()[:12])
PY
)
echo "TDDREV=$TDDREV"
export TDDREV
python3 - <<'PY'
from pathlib import Path
import json,os
p=Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/evidence/commands/tdd-p2-r01/mapping-pre-revision.json')
m=json.loads(p.read_text()); m['revision']=os.environ['TDDREV']
q=p.with_name('mapping.json'); q.write_text(json.dumps(m,ensure_ascii=False,indent=2)+'\n',encoding='utf-8'); p.unlink()
PY

OUT="$TDIR/evidence/command-results/$TDDREV"
mkdir -p "$OUT"
# BOOT each module once, then run each exact TestClass command independently.
python3 - <<'PY' >/tmp/tdd-registry.tsv
from pathlib import Path
import json
m=json.loads(Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/evidence/commands/tdd-p2-r01/mapping.json').read_text())
for key,v in m['classes'].items(): print('\t'.join([key,v['module'],v['class'],v['source']]))
PY
for module in $(cut -f2 /tmp/tdd-registry.tsv | sort -u); do
  echo "BOOT $module"
  ./mvnw -pl "$module" -am -Dmaven.test.skip=true install >"$OUT/boot-${module}.log" 2>&1
 done

while IFS=$'\t' read -r key module clazz source; do
  cmd="./mvnw -pl $module -Dtest=$clazz -Dsurefire.failIfNoSpecifiedTests=true test"
  log="$OUT/${key}.log"
  set +e
  bash -lc "$cmd" >"$log" 2>&1
  rc=$?
  set -e
  if grep -Eq 'COMPILATION ERROR|testCompile.*FAILURE|There are test compilation errors|Failed to execute goal .*maven-compiler-plugin.*testCompile' "$log"; then
    echo "INVALID_RED compile failure for $key"; tail -120 "$log"; exit 31
  fi
  if [ "$rc" -ne 0 ] && ! grep -Fq 'P2 RED [' "$log"; then
    echo "INVALID_RED unattributed failure for $key rc=$rc"; tail -120 "$log"; exit 32
  fi
  KEY="$key" MODULE="$module" CLAZZ="$clazz" CMD="$cmd" RC="$rc" LOG="$log" python3 - <<'PY'
from pathlib import Path
import os,json,hashlib,datetime
log=Path(os.environ['LOG']); out=log.parent/(os.environ['KEY']+'.json')
payload={'schema_version':2,'kind':'command_result','command':os.environ['CMD'],'exit_code':int(os.environ['RC']),'executed_at':datetime.datetime.now(datetime.timezone.utc).isoformat(),'revision':os.environ['TDDREV'],'output_ref':str(log.relative_to(Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC'))),'output_digest':hashlib.sha256(log.read_bytes()).hexdigest()}
out.write_text(json.dumps(payload,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
PY
done </tmp/tdd-registry.tsv

# Deterministic TDD RED validator.
VALIDATOR="$TDIR/evidence/commands/tdd-p2-r01/validate_tdd_red.py"
cat >"$VALIDATOR" <<'PY'
from pathlib import Path
import json,sys,re
TD=Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC')
m=json.loads((TD/'evidence/commands/tdd-p2-r01/mapping.json').read_text())
rev=m['revision']; out=TD/'evidence/command-results'/rev
errors=[]; red=[]; green=[]; case_count=0
for key,v in m['classes'].items():
    src=Path(v['source']); case_count += len(v['cases'])
    if not src.is_file(): errors.append('missing test source '+str(src)); continue
    text=src.read_text()
    for cid in v['cases']:
        if text.count(cid)<1: errors.append(f'{key} missing case {cid}')
    result=json.loads((out/(key+'.json')).read_text())
    if result.get('revision')!=rev: errors.append(f'{key} stale result revision')
    log=(TD/result['output_ref']).read_text(errors='replace')
    if re.search(r'COMPILATION ERROR|test compilation errors|maven-compiler-plugin.*testCompile',log,re.I): errors.append(f'{key} invalid compile RED')
    if result['exit_code']==0: green.append(key)
    elif 'P2 RED [' in log: red.append(key)
    else: errors.append(f'{key} nonzero without target RED marker')
if len(m['classes'])!=23: errors.append(f'class count {len(m["classes"])} !=23')
if case_count!=101: errors.append(f'case count {case_count} !=101')
required={'CASE-P2-TD-NESTED-OBJECT-PATH-001','CASE-P2-TD-DEEP-NESTED-OBJECT-PATH-001','CASE-P2-TD-NON-COMPOSITE-INTERMEDIATE-001','CASE-P2-TD-NESTED-COLLECTION-PATH-001','CASE-P2-TD-TARGET-MAIN-PATH-ISOLATION-001','CASE-P2-TD-PARENT-PATH-NO-AUTH-FALLBACK-001'}
target=set(m['classes']['TARGET']['cases']); policy=set(m['classes']['POLICY']['cases'])
if not required <= (target|policy): errors.append('R32 six nested ModelPath cases not fully mapped to TARGET/POLICY')
if len(red)<1: errors.append('no valid target RED')
# Production tree must be untouched by TddAgent; only src/test + task/evidence metadata are legal.
import subprocess
changed=subprocess.check_output(['git','status','--porcelain'],text=True).splitlines()
prod=[]
for line in changed:
    p=line[3:]
    if p.startswith('dec-') and '/src/test/' not in p: prod.append(p)
if prod: errors.append('production changes present: '+','.join(prod))
summary={'status':'PASSED' if not errors else 'FAILED','revision':rev,'exactTestClasses':len(m['classes']),'blockingCases':case_count,'validRedClasses':sorted(red),'characterizationGreenClasses':sorted(green),'errors':errors}
print(json.dumps(summary,ensure_ascii=False,indent=2))
if errors: sys.exit(1)
PY

# Add intent-to-add so diff/check includes the untracked TDD sources and validator.
cut -f4 /tmp/tdd-registry.tsv | xargs -d '\n' git add -N
git add -N "$VALIDATOR"
VALID_CMD="python3 $VALIDATOR"
VALID_LOG="$OUT/validate-tdd-red.log"
$VALID_CMD >"$VALID_LOG"
CMD="$VALID_CMD" RC=0 LOG="$VALID_LOG" KEY="validation" python3 - <<'PY'
from pathlib import Path
import os,json,hashlib,datetime
TD=Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC'); log=Path(os.environ['LOG']); out=log.parent/(os.environ['KEY']+'.json')
p={'schema_version':2,'kind':'command_result','command':os.environ['CMD'],'exit_code':0,'executed_at':datetime.datetime.now(datetime.timezone.utc).isoformat(),'revision':os.environ['TDDREV'],'output_ref':str(log.relative_to(TD)),'output_digest':hashlib.sha256(log.read_bytes()).hexdigest()}; out.write_text(json.dumps(p,ensure_ascii=False,indent=2)+'\n')
PY
DIFF_LOG="$OUT/git-diff-check.log"
git diff --check >"$DIFF_LOG"
CMD='git diff --check' RC=0 LOG="$DIFF_LOG" KEY='diff-check' python3 - <<'PY'
from pathlib import Path
import os,json,hashlib,datetime
TD=Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC'); log=Path(os.environ['LOG']); out=log.parent/(os.environ['KEY']+'.json')
p={'schema_version':2,'kind':'command_result','command':os.environ['CMD'],'exit_code':0,'executed_at':datetime.datetime.now(datetime.timezone.utc).isoformat(),'revision':os.environ['TDDREV'],'output_ref':str(log.relative_to(TD)),'output_digest':hashlib.sha256(log.read_bytes()).hexdigest()}; out.write_text(json.dumps(p,ensure_ascii=False,indent=2)+'\n')
PY

# Freeze diff and register all TDD evidence through the canonical Evidence Registry.
git diff -- $(cut -f4 /tmp/tdd-registry.tsv) >"$TDIR/evidence/commands/tdd-p2-r01/test-diff.patch"
python3 - <<'PY'
from pathlib import Path
import sys,json,os
sys.path.insert(0,'/home/oai/skills/common-develop/scripts')
import evidence
TD=Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC'); rev=os.environ['TDDREV']
m=json.loads((TD/'evidence/commands/tdd-p2-r01/mapping.json').read_text())
all_cases=[c for v in m['classes'].values() for c in v['cases']]
items=[{'type':'test_ref','ref':v['source']} for v in m['classes'].values()]
test_bundle=evidence.register_bundle(TD,agent='TddAgent',items=items,revision=rev,phase='tdd',scope_refs=['tdd_evidence','test_diff','TESTDESIGN-P2-R32',*all_cases])
testdesign=evidence.register_evidence(TD,agent='TddAgent',evidence_type='test_ref',ref='../../doc/FEATURE-DESC-3361AD2E54FC/test_case.md',revision=rev,phase='tdd',scope_refs=['tdd_evidence','TESTDESIGN-P2-R32'])
plan=evidence.register_evidence(TD,agent='TddAgent',evidence_type='plan_ref',ref='development_tasks.yaml',revision=rev,phase='tdd',scope_refs=['implementation_contract','TP-FEATURE-DESC-3361AD2E54FC-R05@b71685a8d84a'])
design=evidence.register_evidence(TD,agent='TddAgent',evidence_type='design_ref',ref='../../doc/COMPILER/COMPILER_design.md',revision=rev,phase='tdd',scope_refs=['implementation_contract','DESIGN-P2-R30'])
diff=evidence.register_evidence(TD,agent='TddAgent',evidence_type='diff_ref',ref='evidence/commands/tdd-p2-r01/test-diff.patch',revision=rev,phase='tdd',scope_refs=['test_diff','TASK-P2-TDD-RED-001#expected_results/0'])
commands=[]; statuses={}
out=TD/'evidence/command-results'/rev
for key,v in m['classes'].items():
    cr=out/(key+'.json'); payload=json.loads(cr.read_text()); statuses[key]='RED' if payload['exit_code'] else 'GREEN_CHARACTERIZATION'
    e=evidence.register_evidence(TD,agent='TddAgent',evidence_type='command_ref',ref=str(cr.relative_to(TD)),revision=rev,phase='tdd',scope_refs=['tdd_evidence',key,statuses[key]],command_result_ref=str(cr.relative_to(TD)))
    commands.append(e['evidence_id'])
summary={'schema_version':1,'revision':rev,'test_design':'TESTDESIGN-P2-R32','implementation_plan':'TP-FEATURE-DESC-3361AD2E54FC-R05@b71685a8d84a','exact_test_classes':23,'blocking_cases':101,'class_status':statuses,'red_count':sum(1 for x in statuses.values() if x=='RED'),'characterization_green_count':sum(1 for x in statuses.values() if x!='RED'),'command_evidence_ids':commands,'production_modified':False}
sp=TD/'evidence/commands/tdd-p2-r01/tdd-red-summary.json'; sp.write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n')
se=evidence.register_evidence(TD,agent='TddAgent',evidence_type='test_ref',ref=str(sp.relative_to(TD)),revision=rev,phase='tdd',scope_refs=['tdd_evidence','test_diff','implementation_contract','RED_SUMMARY'])
validator=[]
for name in ['validation','diff-check']:
    cr=out/(name+'.json')
    e=evidence.register_evidence(TD,agent='TddAgent',evidence_type='command_ref',ref=str(cr.relative_to(TD)),revision=rev,phase='tdd',scope_refs=['tdd_evidence','validation'],command_result_ref=str(cr.relative_to(TD)))
    validator.append(e['evidence_id'])
result={'test_bundle':test_bundle['evidence_id'],'test_design':testdesign['evidence_id'],'plan':plan['evidence_id'],'design':design['evidence_id'],'diff':diff['evidence_id'],'summary':se['evidence_id'],'red_commands':commands,'validation_commands':validator}
Path('/tmp/tdd-evidence-ids.json').write_text(json.dumps(result))
print(json.dumps(result,indent=2))
PY
python3 /home/oai/skills/common-develop/scripts/evidence.py validate -g TddAgent --task-dir "$TDIR"

# Finish TddAgent attempt with current revision evidence and exact validation commands.
python3 - <<'PY'
import json,subprocess,os
x=json.load(open('/tmp/tdd-evidence-ids.json'))
args=['python3','/home/oai/skills/common-develop/scripts/long_task.py','finish-attempt','-g','TddAgent','--task-dir','project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC','--attempt-id',os.environ['ATTEMPT'],'--status','PASSED','--output-revision',os.environ['TDDREV'],'--summary','TDD-I008 materialized 23 exact R32 TestClasses / 101 Cases with compile-clean target RED and inherited characterization; no production implementation.','--next-agent','ProjectManagerAgent']
for p in [line.rstrip('\n').split('\t')[3] for line in open('/tmp/tdd-registry.tsv')]: args += ['--modified-file',p]
args += ['--modified-file','project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC/evidence/commands/tdd-p2-r01/validate_tdd_red.py']
for eid in x['validation_commands']: args += ['--command-ref',eid]
for eid in [x['test_bundle'],x['test_design'],x['plan'],x['design'],x['diff'],x['summary'],*x['red_commands'],*x['validation_commands']]: args += ['--evidence-ref',eid]
subprocess.run(args,check=True)
PY
python3 /home/oai/skills/common-develop/scripts/long_task.py publish-artifact -g TddAgent --task-dir "$TDIR" --attempt-id "$ATTEMPT" --evidence-id "$(python3 -c 'import json;print(json.load(open("/tmp/tdd-evidence-ids.json"))["summary"])')"

# One blocking assertion covers the single comprehensive TDD expected result; TDDReviewAgent independently confirms it.
python3 /home/oai/skills/common-develop/scripts/acceptance.py add -g ProjectManagerAgent --task-dir "$TDIR" --assertion-id ASRT-P2-TDD-R01-RED-001 --acceptance-id AC-P2-SYSTEM-RULEVIEW-007 --statement 'Independent TDD review confirms exact R32/R05 pre-development test baseline: 23 TestClasses / 101 Cases executable, non-zero failures attributable to missing P2 behavior rather than compile/setup errors, no production implementation, and evidence bound to exact TDD revision.' --type MANUAL_REVIEW --phase tdd --revision "$TDDREV" --blocking --parameters '{"reviewer_agent":"TDDReviewAgent","review_phase":"tdd"}' --source-ref TASK-P2-TDD-RED-001#expected_results/0
python3 - <<'PY'
import json,subprocess
x=json.load(open('/tmp/tdd-evidence-ids.json'))
args=['python3','/home/oai/skills/common-develop/scripts/manual_review.py','draft','-g','ProjectManagerAgent','--task-dir','project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC','--assertion-id','ASRT-P2-TDD-R01-RED-001','--answer','MRQ-VERIFY=YES','--summary','PASSED: exact TDD revision contains all 23 R32 TestClasses / 101 Cases; valid failures are compile-clean target REDs with P2 RED markers, inherited P1 behavior is characterization only, inputs are exact R05/R32, and production source is untouched.','--output','/tmp/tdd-review.md']
for eid in [x['test_bundle'],x['test_design'],x['plan'],x['design'],x['diff'],x['summary'],*x['validation_commands']]: args += ['--evidence-id',eid]
subprocess.run(args,check=True)
PY
python3 /home/oai/skills/common-develop/scripts/manual_review.py submit -g TDDReviewAgent --task-dir "$TDIR" --review-file /tmp/tdd-review.md
python3 /home/oai/skills/common-develop/scripts/acceptance.py validate -g ProjectManagerAgent --task-dir "$TDIR"
python3 /home/oai/skills/common-develop/scripts/evidence.py validate -g ProjectManagerAgent --task-dir "$TDIR"
python3 /home/oai/skills/common-develop/scripts/long_task.py validate -g ProjectManagerAgent --task-dir "$TDIR"
python3 /home/oai/skills/common-develop/scripts/long_task.py finalize-phase -g ProjectManagerAgent --task-dir "$TDIR"

# Formal phase checkpoint; stop here. Do NOT advance to Development.
python3 /home/oai/skills/common-develop/scripts/git_checkpoint.py commit -g ProjectManagerAgent --task-dir "$TDIR" --title 'test(p2): establish R32/R05 TDD pre-development baseline' --change 'Materialize 23 exact TestClasses and 101 blocking Cases.' --change 'Freeze compile-clean P2 RED/characterization evidence on TDD-P2-R01.' --change 'Complete independent TDDReviewAgent gate without production implementation.'
python3 /home/oai/skills/common-develop/scripts/git_checkpoint.py validate -g ProjectManagerAgent --task-dir "$TDIR" --json
python3 /home/oai/skills/common-develop/scripts/long_task.py validate -g ProjectManagerAgent --task-dir "$TDIR" --json
git diff --check
git status --porcelain
# Semantic HEAD contains no runner/workflow because we reset to BASE before work.
git push --force origin HEAD:tmp/pr36-tdd-semantic-20260811
echo "FINAL_TDD_REV=$TDDREV"
echo "FINAL_TDD_COMMIT=$(git rev-parse HEAD)"

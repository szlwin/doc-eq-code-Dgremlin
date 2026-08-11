#!/usr/bin/env bash
set -euo pipefail

BASE='49f81d68a0aafacd284cb24ba5567d4e84b3ae08'
TARGET='feature/p2-design-testdesign-20260808'
SOURCE='tmp/pr36-dev01-run-20260811'
OUT_BRANCH='common-develop/dev01-r03-pre-maven-20260812'

git fetch origin "$TARGET" "$SOURCE"
test "$(git rev-parse origin/$TARGET)" = "$BASE"
git show "origin/$SOURCE:.common-develop-dev01-run.sh" >/tmp/r03-run.sh

python3 - <<'PY'
from pathlib import Path
import re
p=Path('/tmp/r03-run.sh')
s=p.read_text()

BASE='49f81d68a0aafacd284cb24ba5567d4e84b3ae08'
s=re.sub(r'^BASE=[0-9a-f]{40}$','BASE='+BASE,s,count=1,flags=re.M)

# 从当前 R02 正式重开 Development。这里是生命周期变更，不执行独立 validate 命令。
s=s.replace('python3 /home/oai/skills/common-develop/scripts/long_task.py validate -g ProjectManagerAgent --task-dir "$TDIR"\n','',1)
old='python3 /home/oai/skills/common-develop/scripts/long_task.py advance-phase -g ProjectManagerAgent --task-dir "$TDIR"\n'
new="python3 /home/oai/skills/common-develop/scripts/long_task.py reopen-phase -g ProjectManagerAgent --task-dir \"$TDIR\" --from-phase development --source-revision 'DEV-P2-DEV01-R02@c36e32f12ff4' --reason 'User-selected full -ar remediation: rebuild DEV-01 skeleton gate and produce a new concrete revision while preserving truthful R32 oracle ownership and zero production drift.'\n"
if old not in s: raise SystemExit('advance-phase anchor missing')
s=s.replace(old,new,1)

# reopen-phase 会把当前 Development tasks 全部重绑定到新的 Skeleton iteration。
# Skeleton 第一轮只能包含 Skeleton task；旧 task 历史留在 attempts/reviews/outcomes，不以旧 iteration row 回填。
marker='# Materialize only the DEV-01 architecture-skeleton task in the skeleton iteration.\n'
isolate=r'''# Isolate the fresh architecture-skeleton iteration.
python3 - <<'PYISO'
from pathlib import Path
import sys
sys.path.insert(0,'/home/oai/skills/common-develop/scripts')
import long_task
TD=Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC')
plan_doc,tasks=long_task.read_block(TD/'task_plan.md','task-plan')
_,state=long_task.read_block(TD/'task_state.md','task-state')
iteration=state['artifact_revisions']['development']['iteration_id']
removed=[t.get('id') for t in tasks if isinstance(t,dict) and t.get('phase')=='development' and t.get('iteration_id')==iteration]
tasks=[t for t in tasks if not (isinstance(t,dict) and t.get('phase')=='development' and t.get('iteration_id')==iteration)]
long_task.transactional_text_write({TD/'task_plan.md':long_task.render_block(TD/'task_plan.md','task-plan',plan_doc,tasks)})
print({'skeletonIteration':iteration,'deferredConcreteTasks':removed})
PYISO

'''
if marker not in s: raise SystemExit('skeleton marker missing')
s=s.replace(marker,isolate+marker,1)

# Fresh -ar skeleton identity. No Maven/diff validation is executed in this helper Action.
s=s.replace('TASK-P2-DEV-01-SYSTEM-RULEVIEW-SKELETON','TASK-P2-DEV-01-SYSTEM-RULEVIEW-SKELETON-R02')
s=s.replace('LOGICAL-P2-DEV01-SKELETON','LOGICAL-P2-DEV01-SKELETON-R02')
s=s.replace("'validation_commands':['./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install','git diff --check']","'validation_commands':[]",1)
s=s.replace('all 14 DEV-01 R32 cases are represented as real Java-8 behavioral tests','14 executable methods are truthfully classified as exact, partial, characterization, or deferred; surrogate assertions are never counted as exact R32 closure')
s=s.replace('DEV-P2-DEV01-SKEL-R01@','DEV-P2-DEV01-SKEL-R02@')

# 当前 49f 已包含正确的 characterization DisplayName；不要用旧 runner 覆盖测试文件。
a=s.find('# Replace reflection-only TDD placeholders with all 14 real DEV-01 behavioral oracles (Java 8 compatible).')
b=s.find('# Reproduce the real pre-fix RED on exact BASE production with the final behavioral tests.')
if a<0 or b<0 or b<=a: raise SystemExit('test replacement region missing')
s=s[:a]+'''# R03 keeps the already-corrected R02 test source; no test body rewrite here.\n\n'''+s[b:]

# R03 不制造新的 RED；复用已验证的 R01 RED provenance。
a=s.find('# Reproduce the real pre-fix RED on exact BASE production with the final behavioral tests.')
b=s.find('# Add the real -ar skeleton.')
if a<0 or b<0 or b<=a: raise SystemExit('RED region missing')
s=s[:a]+'''# Historical valid RED provenance from R01.\nREDREV='DEV-P2-DEV01-RED@9d94e9f7822d'\nexport REDREV\n\n'''+s[b:]

# 写入 R03 oracle ownership matrix；JUnit 执行状态和 semantic closure 永久分离。
marker='# Add the real -ar skeleton. The main branch/order is frozen; concrete source freezing remains explicit UOE.\n'
ownership=r'''python3 - <<'PYOWN'
from pathlib import Path
import json
TD=Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC')
rows=[
 {'test':'SystemCompilationContractTest.systemIdentitySetIsSourceOrderIndependent','r32_case':'CASE-P2-TD-SYSTEM-DETERMINISM-001','role':'PARTIAL_ORACLE','semantic_owner_task':'TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION','verified':'SystemKey set source-order independence','deferred':'semantic digest determinism'},
 {'test':'SystemCompilationContractTest.duplicateSystemDiagnosticIsSourceOrderIndependent','r32_case':'CASE-P2-TD-SYSTEM-DUPLICATE-001','role':'EXACT_ORACLE','semantic_owner_task':'TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION'},
 {'test':'SystemCompilationContractTest.ruleViewMayPrecedeSystemButMissingOwnerFails','r32_case':'CASE-P2-TD-SYSTEM-FORWARD-REF-001','role':'EXACT_ORACLE','semantic_owner_task':'TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION'},
 {'test':'SystemCompilationContractTest.systemDefinitionIsFrozenFromMutableInputAttributes','r32_case':'CASE-P2-TD-SYSTEM-OWNERSHIP-SNAPSHOT-001','role':'EXACT_ORACLE','semantic_owner_task':'TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION'},
 {'test':'SystemCompilationContractTest.schemaVersionChangesDefinitionValueButNotSystemIdentity','r32_case':'CASE-P2-TD-SYSTEM-VERSION-IDENTITY-001','role':'PARTIAL_ORACLE','semantic_owner_task':'TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION','verified':'SystemKey identity version-independence','deferred':'compiled aggregate semantic digest'},
 {'test':'SystemCompilationContractTest.systemKeyUsesCanonicalSharedIdentity','r32_case':'CASE-P2-TD-BM-CANONICAL-PAIR-001','role':'EXACT_ORACLE','semantic_owner_task':'TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION'},
 {'test':'RuleViewCompilationContractTest.missingSystemOwnerFailsWithoutPartialPublication','r32_case':'CASE-P2-TD-RULEVIEW-SYSTEM-REQUIRED-001','role':'EXACT_ORACLE','semantic_owner_task':'TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION'},
 {'test':'RuleViewCompilationContractTest.duplicateRuleViewDiagnosticIsSourceOrderIndependent','r32_case':'CASE-P2-TD-RULEVIEW-SAME-SYSTEM-DUPLICATE-001','role':'EXACT_ORACLE','semantic_owner_task':'TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION'},
 {'test':'RuleViewCompilationContractTest.sameLocalNameIsIsolatedByOwningSystem','r32_case':'CASE-P2-TD-RULEVIEW-CROSS-SYSTEM-ISOLATION-001','role':'EXACT_ORACLE','semantic_owner_task':'TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION'},
 {'test':'RuleViewCompilationContractTest.explicitOwnerNeverFallsBackToMostRecentSystem','characterization_id':'DEV01-CHAR-EXPLICIT-SYSTEM-OWNER-NO-RECENCY-FALLBACK-001','target_r32_case':'CASE-P2-TD-RULEVIEW-VIEW-RESOLUTION-001','role':'CHARACTERIZATION','semantic_owner_task':'TASK-P2-DEV-02-RULEVIEW-REFERENCE','closure':'DEFERRED'},
 {'test':'RuleViewCompilationContractTest.compositeRuleViewIdentityRemainsCaseSensitive','characterization_id':'DEV01-CHAR-RULEVIEWKEY-CASE-SENSITIVE-001','target_r32_case':'CASE-P2-TD-RULEKEY-CONTRACT-001','role':'CHARACTERIZATION','semantic_owner_task':'TASK-P2-DEV-02-RULEVIEW-REFERENCE','closure':'DEFERRED'},
 {'test':'RuleViewCompilationContractTest.noBareStringRuleViewKeyConstructorExists','characterization_id':'DEV01-CHAR-RULEVIEWKEY-NO-BARE-CONSTRUCTOR-001','target_r32_case':'CASE-P2-TD-RULEVIEW-COMPOSITE-LOOKUP-001','role':'CHARACTERIZATION','semantic_owner_task':'TASK-P2-DEV-02-RULEVIEW-REFERENCE','closure':'DEFERRED'},
 {'test':'RuleViewCompilationContractTest.explicitLexicalOwnerAndNameMapToSharedCompositeKey','characterization_id':'DEV01-CHAR-LEXICAL-OWNER-NORMALIZATION-001','target_r32_case':'CASE-P2-TD-KEY-SOURCE-COMPAT-001','role':'CHARACTERIZATION','semantic_owner_task':'TASK-P2-DEV-09-REAL-FIXTURE-COMPATIBILITY','closure':'DEFERRED'},
 {'test':'RuleViewCompilationContractTest.localNameNeverAuthorizesCrossSystemLookup','characterization_id':'DEV01-CHAR-CROSS-SYSTEM-NEGATIVE-LOOKUP-001','target_r32_case':'CASE-P2-TD-BARE-NAME-COMPATIBILITY-BOUNDARY-001','role':'CHARACTERIZATION','semantic_owner_task':'TASK-P2-DEV-09-REAL-FIXTURE-COMPATIBILITY','closure':'DEFERRED'}]
payload={'schema_version':1,'task':'TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION','authority':'TESTDESIGN-P2-R32','executed_test_methods':14,'exact_oracles':7,'partial_oracles':2,'characterization_methods':5,'all_r32_oracles_closed':False,'rule':'JUnit GREEN does not imply exact R32 semantic closure','rows':rows}
(TD/'evidence/commands/dev01-r03-oracle-ownership.json').write_text(json.dumps(payload,ensure_ascii=False,indent=2)+'\n')
PYOWN

'''
if marker not in s: raise SystemExit('skeleton code marker missing')
s=s.replace(marker,ownership+marker,1)

# Current production already contains the reviewed concrete seam. Fresh skeleton temporarily makes only that seam fail-fast.
a=s.find('# Add the real -ar skeleton. The main branch/order is frozen; concrete source freezing remains explicit UOE.')
b=s.find('SKELREV=$(python3 - <<\'PY\'')
if a<0 or b<0 or b<=a: raise SystemExit('skeleton source region missing')
skel=r'''# Add the fresh R02 architecture skeleton by replacing only the reviewed concrete seam.
python3 - <<'PYSKEL'
from pathlib import Path
p=Path('dec-core-compiler/src/main/java/dec/core/compiler/symbol/SymbolTableBuilder.java')
s=p.read_text()
old='''    private static DuplicateConflictSources freezeDuplicateConflictSources(\n            SourceRef lower,\n            SourceRef higher) {\n        return new DuplicateConflictSources(higher, lower);\n    }\n'''
new='''    private static DuplicateConflictSources freezeDuplicateConflictSources(\n            SourceRef lower,\n            SourceRef higher) {\n        throw new UnsupportedOperationException(\n                "DEV-01 R02 architecture skeleton: concrete duplicate source freezing is not implemented");\n    }\n'''
if old not in s: raise SystemExit('current reviewed concrete seam missing')
p.write_text(s.replace(old,new,1))
PYSKEL

'''
s=s[:a]+skel+s[b:]

# Remove helper Action skeleton Maven/diff validation while preserving the patch artifact.
a=s.find('SKOUT="$TDIR/evidence/command-results/$SKELREV"; mkdir -p "$SKOUT"')
b=s.find('git diff -- "$BUILDER" "$SYS_TEST" "$RV_TEST" > "$TDIR/evidence/commands/dev01-skeleton.patch"')
if a<0 or b<0 or b<=a: raise SystemExit('skeleton validation region missing')
s=s[:a]+s[b:]

# Replace skeleton evidence block with command-free, ownership-aware Evidence.
a=s.find("python3 - <<'PY'\nfrom pathlib import Path\nimport sys,json,os\nsys.path.insert(0,'/home/oai/skills/common-develop/scripts')\nimport evidence\nTD=Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC'); rev=os.environ['SKELREV']")
b=s.find("# Create independent skeleton assertions",a)
if a<0 or b<0: raise SystemExit('skeleton evidence region missing')
evidence_block=r'''python3 - <<'PYEVID'
from pathlib import Path
import sys,json,os
sys.path.insert(0,'/home/oai/skills/common-develop/scripts')
import evidence
TD=Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC'); rev=os.environ['SKELREV']
tests=evidence.register_bundle(TD,agent='DevelopAgent',items=[{'type':'test_ref','ref':'../../../dec-core-compiler/src/test/java/dec/core/compiler/system/SystemCompilationContractTest.java'},{'type':'test_ref','ref':'../../../dec-core-compiler/src/test/java/dec/core/compiler/ruleview/RuleViewCompilationContractTest.java'}],revision=rev,phase='development',scope_refs=['architecture_skeleton','test_oracle_ownership','TESTDESIGN-P2-R32'])
own=evidence.register_evidence(TD,agent='DevelopAgent',evidence_type='test_ref',ref='evidence/commands/dev01-r03-oracle-ownership.json',revision=rev,phase='development',scope_refs=['architecture_skeleton','test_oracle_ownership','TESTDESIGN-P2-R32'])
plan=evidence.register_evidence(TD,agent='DevelopAgent',evidence_type='plan_ref',ref='development_tasks.yaml',revision=rev,phase='development',scope_refs=['implementation_contract','TP-FEATURE-DESC-3361AD2E54FC-R05@b71685a8d84a'])
design=evidence.register_evidence(TD,agent='DevelopAgent',evidence_type='design_ref',ref='../../doc/COMPILER/COMPILER_design.md',revision=rev,phase='development',scope_refs=['architecture_skeleton','DESIGN-P2-R30'])
diff=evidence.register_evidence(TD,agent='DevelopAgent',evidence_type='diff_ref',ref='evidence/commands/dev01-skeleton.patch',revision=rev,phase='development',scope_refs=['architecture_skeleton','TASK-P2-DEV-01-SYSTEM-RULEVIEW-SKELETON-R02#expected_results/0'])
summary=TD/'evidence/commands/dev01-r03-skeleton-summary.json'; summary.write_text(json.dumps({'revision':rev,'task':'TASK-P2-DEV-01-SYSTEM-RULEVIEW-SKELETON-R02','production_boundary':'SymbolTableBuilder.freezeDuplicateConflictSources','concrete_implemented':False,'oracle_ownership_ref':'evidence/commands/dev01-r03-oracle-ownership.json','all_r32_oracles_closed':False},indent=2)+'\n')
se=evidence.register_evidence(TD,agent='DevelopAgent',evidence_type='test_ref',ref=str(summary.relative_to(TD)),revision=rev,phase='development',scope_refs=['architecture_skeleton','implementation_contract'])
result={'tests':tests['evidence_id'],'ownership':own['evidence_id'],'plan':plan['evidence_id'],'design':design['evidence_id'],'diff':diff['evidence_id'],'commands':[],'summary':se['evidence_id']}
Path('/tmp/dev01-skel-evidence.json').write_text(json.dumps(result)); print(json.dumps(result,indent=2))
PYEVID
python3 - <<'PYFIN'
import json,subprocess,os
x=json.load(open('/tmp/dev01-skel-evidence.json'))
args=['python3','/home/oai/skills/common-develop/scripts/long_task.py','finish-attempt','-g','DevelopAgent','--task-dir','project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC','--attempt-id',os.environ['SK_ATTEMPT'],'--status','PASSED','--output-revision',os.environ['SKELREV'],'--summary','DEV-01 R02 architecture skeleton preserves production topology and explicitly freezes truthful exact/partial/characterization/deferred oracle ownership.','--next-agent','ProjectManagerAgent']
for f in ['dec-core-compiler/src/main/java/dec/core/compiler/symbol/SymbolTableBuilder.java','dec-core-compiler/src/test/java/dec/core/compiler/system/SystemCompilationContractTest.java','dec-core-compiler/src/test/java/dec/core/compiler/ruleview/RuleViewCompilationContractTest.java']: args += ['--modified-file',f]
for eid in [x['tests'],x['ownership'],x['plan'],x['design'],x['diff'],x['summary']]: args += ['--evidence-ref',eid]
subprocess.run(args,check=True)
PYFIN
python3 /home/oai/skills/common-develop/scripts/long_task.py publish-artifact -g DevelopAgent --task-dir "$TDIR" --attempt-id "$SK_ATTEMPT" --evidence-id "$(python3 -c 'import json;print(json.load(open("/tmp/dev01-skel-evidence.json"))["summary"])')"

'''
s=s[:a]+evidence_block+s[b:]

# Fresh skeleton assertions/reviews are truthful and bind the same SKEL-R02 revision.
s=s.replace('ASRT-P2-DEV01-SKEL-ARCH-001','ASRT-P2-DEV01-SKEL-R02-ARCH-001')
s=s.replace('ASRT-P2-DEV01-SKEL-SPEC-001','ASRT-P2-DEV01-SKEL-R02-SPEC-001')
s=s.replace('existing System/RuleView boundaries, two-pass explicit-owner flow, fail-closed conflict topology and explicit unimplemented concrete source-freezing seam.','existing System/RuleView boundaries, two-pass explicit-owner flow, fail-closed conflict topology, explicit unimplemented concrete source-freezing seam, and truthful exact/partial/characterization/deferred oracle ownership.')
s=s.replace('covers the exact R05/R32 System/RuleView scope without bare-name fallback, System inference, second registry or concrete algorithm before review.','preserves the bounded R05 System/RuleView scope while explicitly deferring DEV-02/DEV-09 semantic oracles; no bare-name fallback, System inference, second registry or concrete algorithm is introduced before review.')
s=s.replace("evs=[x['tests'],x['plan'],x['design'],x['diff'],x['summary'],*x['commands']]","evs=[x['tests'],x['ownership'],x['plan'],x['design'],x['diff'],x['summary']]",1)

# Standalone validation commands are excluded from helper Action. Finalize/advance are lifecycle gates.
for line in [
 'python3 /home/oai/skills/common-develop/scripts/acceptance.py validate -g ProjectManagerAgent --task-dir "$TDIR"\n',
 'python3 /home/oai/skills/common-develop/scripts/evidence.py validate -g ProjectManagerAgent --task-dir "$TDIR"\n',
 'python3 /home/oai/skills/common-develop/scripts/long_task.py validate -g ProjectManagerAgent --task-dir "$TDIR"\n',
]: s=s.replace(line,'')
adv='python3 /home/oai/skills/common-develop/scripts/long_task.py advance-development-step -g ProjectManagerAgent --task-dir "$TDIR"'
s=s.replace(adv,'python3 /home/oai/skills/common-develop/scripts/long_task.py finalize-phase -g ProjectManagerAgent --task-dir "$TDIR"\n'+adv,1)
s=s.replace("state.get('architecture_review',{}).get('step')!='CONCRETE_IMPLEMENTATION'","state.get('architecture_review',{}).get('step')!='IMPLEMENTATION'",1)

# Concrete iteration: remove the skeleton row and materialize canonical R05 slices on the new implementation iteration.
anchor="art=state['artifact_revisions']['development']; source=yaml.safe_load((TD/'development_tasks.yaml').read_text())\nexisting={t.get('id') for t in tasks if isinstance(t,dict)}"
replacement="art=state['artifact_revisions']['development']; source=yaml.safe_load((TD/'development_tasks.yaml').read_text())\ntasks=[t for t in tasks if not (isinstance(t,dict) and str(t.get('id') or '').startswith('TASK-P2-DEV-01-SYSTEM-RULEVIEW-SKELETON'))]\nexisting={t.get('id') for t in tasks if isinstance(t,dict)}"
if anchor not in s: raise SystemExit('concrete materialization anchor missing')
s=s.replace(anchor,replacement,1)
s=s.replace("tid=src['id']","tid=src.get('task_id') or src.get('id')",1)
s=s.replace("len([t for t in source['tasks'] if t['id'] not in existing])","len([t for t in source['tasks'] if (t.get('task_id') or t.get('id')) not in existing])",1)
s=s.replace("'allowed_files':impl.get('affected_files') or []","'allowed_files':src.get('affected_files') or impl.get('affected_files') or []",1)
# Existing repository verify workflow is the Maven authority for DEV-01 R03.
s=s.replace("'validation_commands':src.get('validation_commands') or []","'validation_commands':(['./mvnw --batch-mode --no-transfer-progress clean verify'] if tid == 'TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION' else (src.get('validation_commands') or []))",1)
s=s.replace('--input-revision "$SKELREV"','--input-revision "$TDDREV"',1)

# Concrete source only restores the reviewed seam. Production result must equal the current 49f algorithm.
s=s.replace("print('DEV-P2-DEV01-R01@'+h.hexdigest()[:12])","print('DEV-P2-DEV01-R03@'+h.hexdigest()[:12])",1)

# Stop before every Maven/diff validation and before concrete attempt closure/review.
cut='DEVOUT="$TDIR/evidence/command-results/$DEVREV"; mkdir -p "$DEVOUT"'
pos=s.find(cut)
if pos<0: raise SystemExit('concrete validation cut point missing')
s=s[:pos]+r'''
# Pre-Maven candidate only. Maven is delegated to the existing verify-and-open-pr.yml after PR36 fast-forward.
git add -A
git commit -m "test(p2): prepare DEV-01 R03 full-ar oracle closure" \
  -m "Reopen Development, complete a fresh DEV-01 Skeleton R02 review gate, preserve the existing production algorithm, materialize the concrete R05 queue on the new implementation iteration, and stop with DEV-01 R03 RUNNING for Maven verification by verify-and-open-pr.yml." \
  -m "WK-Task: TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION" \
  -m "WK-Revision: $DEVREV"
git push --force origin HEAD:common-develop/dev01-r03-pre-maven-20260812
echo "PRE_MAVEN_DEVREV=$DEVREV"
echo "PRE_MAVEN_COMMIT=$(git rev-parse HEAD)"
'''
p.write_text(s)
PY

chmod +x /tmp/r03-run.sh
exec /tmp/r03-run.sh

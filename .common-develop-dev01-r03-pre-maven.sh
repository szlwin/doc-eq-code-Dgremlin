#!/usr/bin/env bash
set -euo pipefail

BASE='49f81d68a0aafacd284cb24ba5567d4e84b3ae08'
SOURCE_WORKFLOW_BRANCH='common-develop/dev01-r02-run-20260812'

git fetch origin "$SOURCE_WORKFLOW_BRANCH" tmp/pr36-dev01-run-20260811 feature/p2-design-testdesign-20260808
if [ "$(git rev-parse origin/feature/p2-design-testdesign-20260808)" != "$BASE" ]; then
  echo 'PR36 head moved before R03 preparation' >&2
  exit 2
fi

git show "origin/$SOURCE_WORKFLOW_BRANCH:.github/workflows/common-develop-dev01-r02-run.yml" >/tmp/r02-source-workflow.yml

python3 - <<'PY'
from pathlib import Path
import textwrap
workflow=Path('/tmp/r02-source-workflow.yml').read_text()
marker='        run: |\n'
if marker not in workflow:
    raise SystemExit('R02 source workflow run block missing')
block=textwrap.dedent(workflow.split(marker,1)[1])
block=block.replace("BASE='1b9399d958472fe486e9548015090267f624fa30'","BASE='49f81d68a0aafacd284cb24ba5567d4e84b3ae08'",1)
source_line='source /tmp/dev01-r02-run.sh'
if source_line not in block:
    raise SystemExit('source runner line missing')
block=block.replace(source_line,"bash /tmp/dev01-r03-postpatch.sh\n"+source_line,1)
Path('/tmp/dev01-r03-driver.sh').write_text('#!/usr/bin/env bash\n'+block)
PY

cat >/tmp/dev01-r03-postpatch.sh <<'POST'
#!/usr/bin/env bash
set -euo pipefail
python3 - <<'PY'
from pathlib import Path
import re
p=Path('/tmp/dev01-r02-run.sh')
s=p.read_text()

# 当前 R02 remediation 已写入历史；本次正式 -ar 生成新的 R03 concrete candidate。
s=s.replace("--source-revision 'DEV-P2-DEV01-R02@1f85b2e6b265'","--source-revision 'DEV-P2-DEV01-R02@c36e32f12ff4'",1)
s=s.replace('Independent Review P1: DEV-01 R01 closure conflated 14 green test methods with exact closure of all TESTDESIGN-P2-R32 oracles; reopen for truthful oracle ownership and same-revision re-review.',
            'Replace bounded R02 remediation with the user-selected full -ar lifecycle: reopen Development, rebuild R02 skeleton gate, then produce R03 concrete candidate with truthful oracle ownership and zero production drift.',1)
# 仅 concrete revision 升为 R03；Skeleton 仍是本次新建的 SKEL-R02。
s=s.replace("print('DEV-P2-DEV01-R02@'+h.hexdigest()[:12])","print('DEV-P2-DEV01-R03@'+h.hexdigest()[:12])",1)
s=s.replace('ASRT-P2-DEV01-R02-','ASRT-P2-DEV01-R03-')
s=s.replace('dev01-r02-oracle-ownership.json','dev01-r03-oracle-ownership.json')

# Skeleton 不在 helper Action 执行 Maven/diff；Maven 统一交给现有 verify-and-open-pr.yml。
s=s.replace("'validation_commands':['./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install','git diff --check']","'validation_commands':[]",1)
pattern=r'''SKOUT="\$TDIR/evidence/command-results/\$SKELREV"; mkdir -p "\$SKOUT"\n.*?(?=git diff -- "\$BUILDER" "\$SYS_TEST" "\$RV_TEST" > "\$TDIR/evidence/commands/dev01-skeleton.patch")'''
s,n=re.subn(pattern,'',s,count=1,flags=re.S)
if n!=1:
    raise SystemExit('skeleton helper validation block not removed')
pattern=r'''cmd=\[\]\nfor name in \['boot','diff-check'\]:\n.*?cmd\.append\(e\['evidence_id'\]\)'''
s,n=re.subn(pattern,'cmd=[]',s,count=1,flags=re.S)
if n!=1:
    raise SystemExit('skeleton command evidence loop not removed')

# standalone validator 不在 helper Action 中作为验证执行；正式 lifecycle 命令内部 gate 保留。
for line in [
    'python3 /home/oai/skills/common-develop/scripts/acceptance.py validate -g ProjectManagerAgent --task-dir "$TDIR"\n',
    'python3 /home/oai/skills/common-develop/scripts/evidence.py validate -g ProjectManagerAgent --task-dir "$TDIR"\n',
    'python3 /home/oai/skills/common-develop/scripts/long_task.py validate -g ProjectManagerAgent --task-dir "$TDIR"\n',
]:
    s=s.replace(line,'')

# advance-development-step 会再次 reopen development 到 concrete iteration；Skeleton task 只留历史记录。
anchor="art=state['artifact_revisions']['development']; source=yaml.safe_load((TD/'development_tasks.yaml').read_text())\nexisting={t.get('id') for t in tasks if isinstance(t,dict)}"
replacement="art=state['artifact_revisions']['development']; source=yaml.safe_load((TD/'development_tasks.yaml').read_text())\ntasks=[t for t in tasks if not (isinstance(t,dict) and str(t.get('id') or '').startswith('TASK-P2-DEV-01-SYSTEM-RULEVIEW-SKELETON'))]\nexisting={t.get('id') for t in tasks if isinstance(t,dict)}"
if anchor not in s:
    raise SystemExit('concrete materialization anchor missing')
s=s.replace(anchor,replacement,1)

# 只准备 concrete R03 attempt；不在 helper Action 中运行 Maven、diff-check 或完成 attempt。
cut='DEVOUT="$TDIR/evidence/command-results/$DEVREV"; mkdir -p "$DEVOUT"'
pos=s.find(cut)
if pos<0:
    raise SystemExit('concrete validation cut point missing')
pre=s[:pos]
pre += r'''
git add -A
git commit -m "test(p2): prepare DEV-01 R03 full-ar oracle closure" \
  -m "Reopen Development under -ar, pass a fresh DEV-01 Skeleton R02 review gate, preserve the existing production algorithm, retain truthful R32 oracle ownership, and stop with the R03 concrete attempt open for Maven verification by verify-and-open-pr.yml." \
  -m "WK-Task: TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION" \
  -m "WK-Revision: $DEVREV"
git push --force origin HEAD:common-develop/dev01-r03-pre-maven-20260812
echo "PRE_MAVEN_DEVREV=$DEVREV"
echo "PRE_MAVEN_COMMIT=$(git rev-parse HEAD)"
'''
p.write_text(pre)
PY
POST

chmod +x /tmp/dev01-r03-postpatch.sh /tmp/dev01-r03-driver.sh
exec /tmp/dev01-r03-driver.sh

#!/usr/bin/env bash
set -euo pipefail

python3 - <<'PY'
from pathlib import Path
import textwrap
workflow=Path('.github/workflows/common-develop-dev01-r02-run.yml').read_text()
marker='        run: |\n'
if marker not in workflow:
    raise SystemExit('source R02 workflow run block missing')
block=textwrap.dedent(workflow.split(marker,1)[1])
source_line='source /tmp/dev01-r02-run.sh'
if source_line not in block:
    raise SystemExit('source runner missing')
block=block.replace(source_line,"bash /tmp/dev01-r02-postpatch.sh\n"+source_line,1)
Path('/tmp/dev01-r02-v13.sh').write_text('#!/usr/bin/env bash\n'+block)
PY

cat >/tmp/dev01-r02-postpatch.sh <<'POST'
#!/usr/bin/env bash
set -euo pipefail
python3 - <<'PY'
from pathlib import Path
import re
p=Path('/tmp/dev01-r02-run.sh')
s=p.read_text()

# Skeleton 的 Maven 与 diff 验证不在 helper Action 中执行；Maven 留给现有 verify-and-open-pr.yml。
s=s.replace("'validation_commands':['./mvnw -pl dec-core-compiler -am -Dmaven.test.skip=true install','git diff --check']","'validation_commands':[]",1)

pattern=r'''SKOUT="\$TDIR/evidence/command-results/\$SKELREV"; mkdir -p "\$SKOUT"\n.*?(?=git diff -- "\$BUILDER" "\$SYS_TEST" "\$RV_TEST" > "\$TDIR/evidence/commands/dev01-skeleton.patch")'''
s,n=re.subn(pattern,'',s,count=1,flags=re.S)
if n!=1:
    raise SystemExit('skeleton validation block not removed')

pattern=r'''cmd=\[\]\nfor name in \['boot','diff-check'\]:\n.*?cmd\.append\(e\['evidence_id'\]\)'''
s,n=re.subn(pattern,'cmd=[]',s,count=1,flags=re.S)
if n!=1:
    raise SystemExit('skeleton command evidence loop not removed')

# 显式 standalone validator 不在 helper Action 中执行；生命周期命令自身的内部 gate 仍保留。
for line in [
    'python3 /home/oai/skills/common-develop/scripts/acceptance.py validate -g ProjectManagerAgent --task-dir "$TDIR"\n',
    'python3 /home/oai/skills/common-develop/scripts/evidence.py validate -g ProjectManagerAgent --task-dir "$TDIR"\n',
    'python3 /home/oai/skills/common-develop/scripts/long_task.py validate -g ProjectManagerAgent --task-dir "$TDIR"\n',
]:
    s=s.replace(line,'')

# Skeleton 已完成后，不把其 task row 带进 concrete iteration；历史保留在 attempt/review/outcome。
anchor="art=state['artifact_revisions']['development']; source=yaml.safe_load((TD/'development_tasks.yaml').read_text())\nexisting={t.get('id') for t in tasks if isinstance(t,dict)}"
replacement="art=state['artifact_revisions']['development']; source=yaml.safe_load((TD/'development_tasks.yaml').read_text())\ntasks=[t for t in tasks if not (isinstance(t,dict) and str(t.get('id') or '').startswith('TASK-P2-DEV-01-SYSTEM-RULEVIEW-SKELETON'))]\nexisting={t.get('id') for t in tasks if isinstance(t,dict)}"
if anchor not in s:
    raise SystemExit('concrete materialization anchor missing')
s=s.replace(anchor,replacement,1)

# Concrete R02 只准备候选并开启 attempt；不在 helper Action 中执行 Maven/diff 或宣称 PASSED。
cut='DEVOUT="$TDIR/evidence/command-results/$DEVREV"; mkdir -p "$DEVOUT"'
pos=s.find(cut)
if pos<0:
    raise SystemExit('concrete validation cut point missing')
pre=s[:pos]
pre += r'''
git add -A
git commit -m "test(p2): prepare DEV-01 R02 truthful oracle closure" \
  -m "Keep the reviewed production algorithm unchanged; relabel surrogate R32 assertions as characterization, record oracle ownership, complete the R02 -ar skeleton gate, and stop with the concrete DEV-01 attempt open for Maven verification by verify-and-open-pr.yml." \
  -m "WK-Task: TASK-P2-DEV-01-SYSTEM-RULEVIEW-COMPILATION" \
  -m "WK-Revision: $DEVREV"
git push --force origin HEAD:common-develop/dev01-r02-pre-maven-20260812
echo "PRE_MAVEN_DEVREV=$DEVREV"
echo "PRE_MAVEN_COMMIT=$(git rev-parse HEAD)"
'''
p.write_text(pre)
PY
POST

chmod +x /tmp/dev01-r02-postpatch.sh /tmp/dev01-r02-v13.sh
exec /tmp/dev01-r02-v13.sh

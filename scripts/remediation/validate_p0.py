#!/usr/bin/env python3
from pathlib import Path
import xml.etree.ElementTree as ET, re, sys
root=Path(__file__).resolve().parents[2]; errors=[]
required=['mvnw','mvnw.cmd','.mvn/wrapper/maven-wrapper.properties','.github/workflows/p0-build.yml','scripts/remediation/bootstrap_legacy_dependencies.sh','scripts/remediation/run_p0_dynamic_verification.sh','scripts/remediation/run_p0_local_mysql_verification.sh','scripts/remediation/run_p0_local_verification.sh','scripts/remediation/verify_p0_github_actions.sh','project_doc/docs/_plans/mix-framework-technical-remediation-plan.md','project_doc/docs/_plans/mix-framework-p0-p8-detailed-task-plan.md','doc/mix-framework-technical-remediation-plan.md','doc/mix-framework-p0-p8-detailed-task-plan.md']
for x in required:
    if not (root/x).exists(): errors.append('missing '+x)

for x in ['scripts/remediation/run_p0_dynamic_verification.sh','scripts/remediation/run_p0_local_mysql_verification.sh','scripts/remediation/run_p0_local_verification.sh','scripts/remediation/verify_p0_github_actions.sh']:
    p=root/x
    if p.exists() and not (p.stat().st_mode & 0o111): errors.append('not executable '+x)
plan=(root/'project_doc/docs/_plans/mix-framework-p0-p8-detailed-task-plan.md').read_text(errors='replace')
if 'run_p0_local_verification.sh' not in plan: errors.append('formal local P0 verification entry missing from plan')
if 'GitHub Actions 保留为跨环境辅助回归入口，不作为 P0 退出阻断条件' not in plan: errors.append('GitHub Actions auxiliary/non-blocking policy missing from plan')
for p in [root/'pom.xml',*root.glob('*/pom.xml')]:
    try: ET.parse(p)
    except Exception as e: errors.append(f'invalid XML {p.relative_to(root)}: {e}')
text='\n'.join(p.read_text(errors='replace') for p in [root/'pom.xml',*root.glob('*/pom.xml')])
if 'testFailureIgnore>true' in text: errors.append('testFailureIgnore=true remains')
if '<module>dec-demo</module>' not in (root/'pom.xml').read_text(): errors.append('dec-demo missing from reactor')
for x in ['LegacyResourceSnapshotTest.java','MixContractTest.java','BaseDataContractTest.java']:
    if not list(root.glob('**/'+x)): errors.append('missing test '+x)

root_pom=(root/'pom.xml').read_text(errors='replace')
mysql_profile_match=re.search(r'<profile>\s*<id>mysql-it</id>(.*?)</profile>', root_pom, re.S)
if not mysql_profile_match:
    errors.append('mysql-it profile missing')
else:
    mysql_profile=mysql_profile_match.group(1)
    if '<forkCount>1</forkCount>' not in mysql_profile or '<reuseForks>false</reuseForks>' not in mysql_profile:
        errors.append('mysql-it profile must isolate legacy singleton state with forkCount=1 and reuseForks=false')

if errors:
    print('\n'.join(errors)); sys.exit(1)
print('P0 static validation passed')

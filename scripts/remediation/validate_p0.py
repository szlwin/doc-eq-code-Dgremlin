#!/usr/bin/env python3
from pathlib import Path
import xml.etree.ElementTree as ET, re, sys
root=Path(__file__).resolve().parents[2]; errors=[]
required=['mvnw','mvnw.cmd','.mvn/wrapper/maven-wrapper.properties','.github/workflows/p0-build.yml','scripts/remediation/bootstrap_legacy_dependencies.sh','doc/mix-framework-technical-remediation-plan.md','doc/mix-framework-p0-p8-detailed-task-plan.md']
for x in required:
    if not (root/x).exists(): errors.append('missing '+x)
for p in [root/'pom.xml',*root.glob('*/pom.xml')]:
    try: ET.parse(p)
    except Exception as e: errors.append(f'invalid XML {p.relative_to(root)}: {e}')
text='\n'.join(p.read_text(errors='replace') for p in [root/'pom.xml',*root.glob('*/pom.xml')])
if 'testFailureIgnore>true' in text: errors.append('testFailureIgnore=true remains')
if '<module>dec-demo</module>' not in (root/'pom.xml').read_text(): errors.append('dec-demo missing from reactor')
for x in ['LegacyResourceSnapshotTest.java','MixContractTest.java','BaseDataContractTest.java']:
    if not list(root.glob('**/'+x)): errors.append('missing test '+x)
if errors:
    print('\n'.join(errors)); sys.exit(1)
print('P0 static validation passed')

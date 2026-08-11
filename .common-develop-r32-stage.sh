#!/usr/bin/env bash
set -euo pipefail
cp .common-develop-r32-stage-v2.sh /tmp/common-develop-r32-stage-v2.sh
python3 - <<'PY'
from pathlib import Path
import re
p=Path('/tmp/common-develop-r32-stage-v2.sh')
s=p.read_text(encoding='utf-8')
replacement='c1="python3 -c \\\"from pathlib import Path; import re; s=Path(\'project_doc/version/V_1.0/doc/FEATURE-DESC-3361AD2E54FC/test_case.md\').read_text(); req="+req+"; assert \'TESTDESIGN-P2-R32\' in s; assert len(set(re.findall(r\'CASE-P2-TD-[A-Z0-9-]+-001\',s)))==101; assert len(set(re.findall(r\'\\\\| ([A-Za-z0-9_]+Test) \\\\|\',s)))==23; assert all(x in s for x in req)\\\""'
s,n=re.subn(r'^c1=.*$',replacement,s,count=1,flags=re.M)
if n!=1: raise SystemExit('failed to patch c1')
s=s.replace('publish-artifact -g ProjectManagerAgent','publish-artifact -g TestDesignAgent',1)
p.write_text(s,encoding='utf-8')
PY
exec bash /tmp/common-develop-r32-stage-v2.sh

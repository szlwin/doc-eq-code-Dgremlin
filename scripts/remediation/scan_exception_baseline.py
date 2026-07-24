#!/usr/bin/env python3
from pathlib import Path
import re
root=Path(__file__).resolve().parents[2]
patterns={'printStackTrace':r'\.printStackTrace\s*\(','swallowed catch':r'catch\s*\([^)]*\)\s*\{\s*\}','return null':r'\breturn\s+null\s*;','System.out':r'\bSystem\.out\.'}
print('# P0 异常与日志风险扫描\n')
for name,pat in patterns.items():
    rows=[]
    for f in sorted(root.glob('**/*.java')):
        if '/target/' in str(f): continue
        for n,line in enumerate(f.read_text(errors='replace').splitlines(),1):
            if re.search(pat,line): rows.append(f'{f.relative_to(root)}:{n}: {line.strip()}')
    print(f'## {name} ({len(rows)})\n')
    print('```text'); print('\n'.join(rows) or '(none)'); print('```\n')

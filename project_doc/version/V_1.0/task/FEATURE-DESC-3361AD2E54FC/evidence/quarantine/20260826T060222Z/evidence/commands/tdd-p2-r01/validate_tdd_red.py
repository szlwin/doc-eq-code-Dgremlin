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

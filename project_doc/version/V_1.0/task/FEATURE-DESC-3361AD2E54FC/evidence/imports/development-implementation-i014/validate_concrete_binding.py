from pathlib import Path
import subprocess, sys
root=Path('/mnt/data/project_doc')
def text(p): return (root/p).read_text(encoding='utf-8')
def git(*a): return subprocess.check_output(['git',*a],cwd=root,text=True).strip()
errors=[]
def need(cond,msg):
    if not cond: errors.append(msg)
current=git('rev-parse','HEAD')
need(current=='437b8547d0b660cfa912d9e3333452fc6f248f3e','unexpected canonical HEAD')
nonproj=[x for x in git('diff','--name-only','1a936fdd2a4559178518dce3ab1214a058c5e2ab..HEAD').splitlines() if x and not x.startswith('project_doc/')]
need(not nonproj,f'executable drift since Task8 closure: {nonproj}')
raw=subprocess.check_output(['git','status','--porcelain=v1','-z'],cwd=root); work=[]
for entry in raw.split(b'\0'):
    if not entry: continue
    line=entry.decode('utf-8','surrogateescape')
    if not line[3:].startswith('project_doc/'): work.append(line)
need(not work,f'non-project_doc worktree mutation: {work}')
guard=text('dec-core-starter/src/main/java/dec/core/starter/access/ExactModelAccessGuard.java')
need('final class ExactModelAccessGuard' in guard,'Guard must remain package-private final')
for s in ['exactRule(','DenialCode denial(','authorizeRead(','authorizeWrite(']: need(s in guard,f'missing Guard boundary {s}')
auth=text('dec-core-starter/src/main/java/dec/core/starter/access/ModelEffectAuthorization.java')
for s in ['private ModelEffectAuthorization(','AtomicBoolean consumed','consumeRead()','consumeWrite()']: need(s in auth,f'missing one-shot authorization boundary {s}')
effect=text('dec-core-starter/src/main/java/dec/core/starter/access/GuardAuthorizedModelEffectPort.java')
need('read(ModelEffectAuthorization authorization)' in effect,'effect READ must require Guard authorization')
need('write(ModelEffectAuthorization authorization)' in effect,'effect WRITE must require Guard authorization')
port=text('dec-core-starter/src/main/java/dec/core/starter/access/GuardedProtectedAccessPort.java')
order=['guard.exactRule(invocation)','resolver.resolve(','guard.denial(rule, invocation, target)','AccessOperation.READ','AccessOperation.WRITE']
pos=[port.find(x) for x in order]
need(all(x>=0 for x in pos),f'missing guarded orchestration marker: {list(zip(order,pos))}')
need(pos[0] < pos[1] < pos[2] < pos[3] < pos[4],f'guarded orchestration order drifted: {pos}')
need('effectPort.read(authorization)' in port and 'effectPort.write(authorization)' in port,'effect invocation must remain authorization-gated')
factory=text('dec-core-starter/src/main/java/dec/core/starter/access/ProtectedAccessRuntimeFactory.java')
need('private final EngineContext context;' in factory,'factory must capture final EngineContext')
for forbidden in ['setContext(', 'replaceContext(', 'reloadContext(', 'rebindContext(', 'hotReload(']: need(forbidden not in factory,f'live context mutation seam detected: {forbidden}')
roots=text('dec-core-model/src/main/java/dec/core/model/runtime/RuntimeModelExecutionRoots.java')
need('private final EngineContext context;' in roots,'runtime root must capture final EngineContext')
need('public synchronized void close()' in roots,'runtime root close boundary missing')
iface=text('dec-core-model/src/main/java/dec/core/model/runtime/RuntimeModelExecutionRoot.java')
for forbidden in ['rebind','reload','replace','hotReload','setContext']: need(forbidden not in iface,f'live runtime mutation seam in root interface: {forbidden}')
all_main='\n'.join(p.read_text(encoding='utf-8',errors='ignore') for p in root.glob('**/src/main/**/*.java'))
need('RuntimeContextBinding' not in all_main,'forbidden RuntimeContextBinding production type detected')
blob=git('rev-parse','HEAD:dec-core-starter/src/test/java/dec/core/starter/access/SingleEngineContextRuntimeLifecycleTest.java')
need(blob=='8b3200201546d4fd333cca768b049ab5036176e6',f'lifecycle test blob drifted: {blob}')
if errors:
    print('CONCRETE_BINDING_VALIDATION=FAILED')
    for e in errors: print('ERROR:',e)
    sys.exit(1)
print('CONCRETE_BINDING_VALIDATION=PASSED')
print('canonical_head='+current)
print('non_project_doc_committed_changes_since_task8_closure=0')
print('non_project_doc_worktree_changes=0')
print('guard_authority_boundary=PASSED')
print('one_shot_authorization_boundary=PASSED')
print('guarded_call_order=PASSED')
print('single_engine_context_generation_boundary=PASSED')
print('forbidden_runtime_context_binding=ABSENT')
print('lifecycle_test_blob=8b3200201546d4fd333cca768b049ab5036176e6')

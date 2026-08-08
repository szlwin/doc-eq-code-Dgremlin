#!/usr/bin/env python3
from __future__ import annotations

import datetime as dt
import hashlib
import json
import subprocess
from pathlib import Path

TASK = Path('project_doc/version/V_1.0/task/FEATURE-DESC-3361AD2E54FC')
INDEX = TASK / 'evidence/evidence_index.json'
LEDGER = TASK / 'evidence/migrations/20260808-pr34-flattened-checkpoint-gitref-repair.json'
EXPECTED_HEAD = 'f0beab1f4230adaa4800ff6a49a060bbedce32ae'
BROKEN_COMMIT = 'dfb2d6b9707ed6127a0434bd5fb5578c2160b5cf'
REBIND_IDS = {'EVD-000030','EVD-000031','EVD-000034'}
FREEZE_PATHS = {
    'EVD-000028': 'project_doc/version/V_1.0/doc/COMPILER/COMPILER_business_model.yaml',
    'EVD-000032': 'project_doc/docs/_relations/dependency_impact.yaml',
    'EVD-000033': 'project_doc/docs/_relations/dependency_graph.md',
}
ALL_IDS = REBIND_IDS | set(FREEZE_PATHS)


def git(*args: str):
    p = subprocess.run(['git', *args], capture_output=True, text=True)
    if p.returncode:
        raise RuntimeError(f"git {' '.join(args)} failed: {p.stderr}")
    return p.stdout


def blob_fact(head: str, path: str):
    tree_line = git('ls-tree', head, '--', path).strip()
    if not tree_line:
        raise RuntimeError(f'path missing at current merge commit: {path}')
    header = tree_line.split('\t',1)[0].split()
    oid = header[2] if len(header) == 3 else ''
    if not oid:
        raise RuntimeError(f'invalid git tree entry: {path}')
    blob = subprocess.run(['git','cat-file','blob',oid],capture_output=True,check=True).stdout
    return oid, hashlib.sha256(blob).hexdigest(), len(blob)


def main():
    head = git('rev-parse','HEAD').strip()
    if head != EXPECTED_HEAD:
        raise RuntimeError(f'expected {EXPECTED_HEAD}, got {head}')
    doc = json.loads(INDEX.read_text(encoding='utf-8'))
    found = []

    for entry in doc.get('evidences', []):
        eid = str(entry.get('evidence_id') or '')
        if eid in REBIND_IDS:
            if entry.get('capture_mode') != 'GIT_REF':
                raise RuntimeError(f'{eid}: expected GIT_REF')
            metadata = entry.get('metadata') if isinstance(entry.get('metadata'), dict) else {}
            gr = metadata.get('git_ref') if isinstance(metadata.get('git_ref'), dict) else {}
            if str(gr.get('commit') or '') != BROKEN_COMMIT or entry.get('ref') != f'git:{BROKEN_COMMIT}':
                raise RuntimeError(f'{eid}: unexpected historical git ref')
            members = gr.get('members') if isinstance(gr.get('members'), list) else []
            if len(members) != 1 or not isinstance(members[0], dict):
                raise RuntimeError(f'{eid}: expected exactly one git member')
            member = members[0]
            path = str(member.get('path') or '')
            expected_oid = str(member.get('blob_oid') or '')
            expected_digest = str(member.get('digest') or entry.get('digest') or '')
            expected_size = int(member.get('size') or 0)
            expected_type = str(member.get('type') or entry.get('type') or '')
            actual_oid, actual_digest, actual_size = blob_fact(head, path)
            if actual_oid != expected_oid:
                raise RuntimeError(f'{eid}: blob OID changed: expected {expected_oid}, actual {actual_oid}')
            if actual_digest != expected_digest:
                raise RuntimeError(f'{eid}: SHA256 mismatch: expected {expected_digest}, actual {actual_digest}')
            if actual_size != expected_size:
                raise RuntimeError(f'{eid}: size mismatch: expected {expected_size}, actual {actual_size}')
            if str(entry.get('digest') or '') != expected_digest:
                raise RuntimeError(f'{eid}: top-level Evidence digest changed')

            old_git_ref = json.loads(json.dumps(gr))
            repaired_member = {**member, 'digest': expected_digest, 'type': expected_type}
            metadata['git_ref'] = {**gr, 'repository': '.', 'commit': head, 'members': [repaired_member]}
            metadata['governance_repair'] = {
                'kind': 'FLATTENED_CHECKPOINT_GIT_REF_REBIND_AND_COMPACT_MEMBER_RESTORE',
                'reason': 'PR #34 flattened local common-develop checkpoint commits into one remote commit and retained compact GIT_REF member metadata without member-level digest/type. The exact recorded blob is unchanged at the merged dev_all commit.',
                'source_commit': BROKEN_COMMIT,
                'replacement_commit': head,
                'original_ref': f'git:{BROKEN_COMMIT}',
                'original_git_ref': old_git_ref,
                'byte_identity_verified': True,
                'blob_oid_verified': expected_oid,
                'digest_verified': expected_digest,
                'size_verified': expected_size,
                'member_type_restored': expected_type,
                'repaired_at': dt.datetime.now(dt.timezone.utc).isoformat(),
            }
            entry['ref'] = f'git:{head}'
            entry['metadata'] = metadata
            found.append({'kind':'REBIND','evidence_id':eid,'path':path,'blob_oid':expected_oid,'digest':expected_digest,'size':expected_size,'type':expected_type,'old_commit':BROKEN_COMMIT,'new_commit':head})

        elif eid in FREEZE_PATHS:
            if entry.get('status') != 'ACTIVE':
                raise RuntimeError(f'{eid}: expected ACTIVE evidence')
            if entry.get('capture_mode') != 'DIRECT':
                raise RuntimeError(f'{eid}: expected DIRECT before freeze, got {entry.get("capture_mode")}')
            path = FREEZE_PATHS[eid]
            expected_digest = str(entry.get('digest') or '')
            evidence_type = str(entry.get('type') or '')
            oid, actual_digest, size = blob_fact(head, path)
            if actual_digest != expected_digest:
                raise RuntimeError(f'{eid}: current blob digest does not match historical Evidence digest: {actual_digest} != {expected_digest}')
            old_ref = str(entry.get('ref') or '')
            old_source_ref = str(entry.get('source_ref') or '')
            old_metadata = json.loads(json.dumps(entry.get('metadata') if isinstance(entry.get('metadata'), dict) else {}))
            member = {'path':path,'blob_oid':oid,'digest':expected_digest,'size':size,'type':evidence_type}
            metadata = dict(old_metadata)
            metadata['git_ref'] = {'repository':'.','commit':head,'members':[member]}
            metadata['governance_repair'] = {
                'kind': 'FREEZE_MUTABLE_BM_R06_DIRECT_EVIDENCE_BEFORE_REWORK',
                'reason': 'BM-R06 Evidence was registered as a mutable DIRECT reference. Business Model I003 legitimately changes the same source file, so the prior revision Evidence must be frozen at the merged BM-R06 commit before rework.',
                'replacement_commit': head,
                'original_capture_mode': 'DIRECT',
                'original_ref': old_ref,
                'original_source_ref': old_source_ref,
                'original_metadata': old_metadata,
                'byte_identity_verified': True,
                'blob_oid_verified': oid,
                'digest_verified': expected_digest,
                'size_verified': size,
                'repaired_at': dt.datetime.now(dt.timezone.utc).isoformat(),
            }
            entry['capture_mode'] = 'GIT_REF'
            entry['ref'] = f'git:{head}'
            entry['source_ref'] = old_source_ref or old_ref
            entry['metadata'] = metadata
            found.append({'kind':'FREEZE_DIRECT','evidence_id':eid,'path':path,'blob_oid':oid,'digest':expected_digest,'size':size,'type':evidence_type,'new_commit':head,'original_ref':old_ref})

    seen = {x['evidence_id'] for x in found}
    if seen != ALL_IDS:
        raise RuntimeError(f'missing expected evidence IDs: {sorted(ALL_IDS - seen)}')

    LEDGER.parent.mkdir(parents=True, exist_ok=True)
    ledger = {
        'schema_version': 1,
        'repair_id': 'GOV-REPAIR-P2-PR34-FLATTENED-GITREF-20260808',
        'target_id': 'FEATURE-DESC-3361AD2E54FC',
        'reason': 'Restore RC9 Evidence resolvability after PR #34 flattened local task checkpoint history and freeze BM-R06 mutable DIRECT Evidence before Business Model I003 rework.',
        'invariant': 'Evidence IDs, types, phases, revisions, source bytes and SHA-256 digests are unchanged. Repairs only restore/freeze immutable Git provenance after verifying exact blob identity.',
        'old_commit': BROKEN_COMMIT,
        'new_commit': head,
        'entries': found,
        'created_at': dt.datetime.now(dt.timezone.utc).isoformat(),
    }
    INDEX.write_text(json.dumps(doc, ensure_ascii=False, separators=(',', ':')) + '\n', encoding='utf-8')
    LEDGER.write_text(json.dumps(ledger, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

    subprocess.run(['python3','/home/oai/skills/common-develop/scripts/evidence.py','validate','-g','ProjectManagerAgent','--task-dir',str(TASK)],check=True)
    subprocess.run(['python3','/home/oai/skills/common-develop/scripts/long_task.py','validate','-g','ProjectManagerAgent','--task-dir',str(TASK)],check=True)
    subprocess.run(['git','diff','--check'],check=True)

    subprocess.run(['git','config','user.name','Common Develop ProjectManagerAgent'],check=True)
    subprocess.run(['git','config','user.email','common-develop@local.invalid'],check=True)
    subprocess.run(['git','add',str(INDEX),str(LEDGER)],check=True)
    subprocess.run(['git','diff','--cached','--check'],check=True)
    subprocess.run(['git','commit','-m','chore(p2): repair and freeze prior business model evidence'],check=True)
    print(json.dumps({'status':'PASSED','repair_id':ledger['repair_id'],'commit':git('rev-parse','HEAD').strip(),'entries':found},ensure_ascii=False,indent=2))

if __name__ == '__main__':
    main()

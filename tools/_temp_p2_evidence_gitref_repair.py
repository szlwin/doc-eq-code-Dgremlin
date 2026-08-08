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
IDS = {'EVD-000030','EVD-000031','EVD-000034'}


def git(*args: str):
    p = subprocess.run(['git', *args], capture_output=True, text=True)
    if p.returncode:
        raise RuntimeError(f"git {' '.join(args)} failed: {p.stderr}")
    return p.stdout


def main():
    head = git('rev-parse','HEAD').strip()
    if head != EXPECTED_HEAD:
        raise RuntimeError(f'expected {EXPECTED_HEAD}, got {head}')
    doc = json.loads(INDEX.read_text(encoding='utf-8'))
    found = []
    for entry in doc.get('evidences', []):
        if entry.get('evidence_id') not in IDS:
            continue
        eid = str(entry['evidence_id'])
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
        expected_digest = str(member.get('digest') or '')
        expected_size = int(member.get('size') or 0)

        tree_line = git('ls-tree', head, '--', path).strip()
        if not tree_line:
            raise RuntimeError(f'{eid}: path missing at current merge commit: {path}')
        header = tree_line.split('\t',1)[0].split()
        actual_oid = header[2] if len(header) == 3 else ''
        if actual_oid != expected_oid:
            raise RuntimeError(f'{eid}: blob OID changed: expected {expected_oid}, actual {actual_oid}')
        blob = subprocess.run(['git','cat-file','blob',actual_oid],capture_output=True,check=True).stdout
        actual_digest = hashlib.sha256(blob).hexdigest()
        if actual_digest != expected_digest or len(blob) != expected_size:
            raise RuntimeError(f'{eid}: blob content mismatch')
        if str(entry.get('digest') or '') != expected_digest:
            raise RuntimeError(f'{eid}: aggregate digest changed')

        old_git_ref = json.loads(json.dumps(gr))
        metadata['git_ref'] = {**gr, 'commit': head}
        metadata['governance_repair'] = {
            'kind': 'FLATTENED_CHECKPOINT_GIT_REF_REBIND',
            'reason': 'PR #34 flattened local common-develop checkpoint commits into one remote commit; the original checkpoint commit is unreachable on GitHub, while the exact recorded blob OID/digest/size is present unchanged at the merged dev_all commit.',
            'source_commit': BROKEN_COMMIT,
            'replacement_commit': head,
            'original_ref': f'git:{BROKEN_COMMIT}',
            'original_git_ref': old_git_ref,
            'byte_identity_verified': True,
            'blob_oid_verified': expected_oid,
            'digest_verified': expected_digest,
            'size_verified': expected_size,
            'repaired_at': dt.datetime.now(dt.timezone.utc).isoformat(),
        }
        entry['ref'] = f'git:{head}'
        entry['metadata'] = metadata
        found.append({
            'evidence_id': eid,
            'path': path,
            'blob_oid': expected_oid,
            'digest': expected_digest,
            'size': expected_size,
            'old_commit': BROKEN_COMMIT,
            'new_commit': head,
        })

    seen = {x['evidence_id'] for x in found}
    if seen != IDS:
        raise RuntimeError(f'missing expected evidence IDs: {sorted(IDS - seen)}')

    LEDGER.parent.mkdir(parents=True, exist_ok=True)
    ledger = {
        'schema_version': 1,
        'repair_id': 'GOV-REPAIR-P2-PR34-FLATTENED-GITREF-20260808',
        'target_id': 'FEATURE-DESC-3361AD2E54FC',
        'reason': 'Restore RC9 Evidence resolvability after PR #34 flattened local task checkpoint history.',
        'invariant': 'Evidence ID, type, phase, revision, source path, blob OID, SHA-256 digest, size and bytes are unchanged; only the unreachable commit pointer is rebound to the merged remote commit containing the exact same blob.',
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
    subprocess.run(['git','commit','-m','chore(p2): repair flattened checkpoint evidence refs'],check=True)
    print(json.dumps({'status':'PASSED','repair_id':ledger['repair_id'],'commit':git('rev-parse','HEAD').strip(),'entries':found},ensure_ascii=False,indent=2))

if __name__ == '__main__':
    main()

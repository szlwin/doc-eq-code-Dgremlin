# P2 Dependency Graph

- Project: `doc-eq-code`
- Current candidate: `BM-R20 / FLOW-R11 / P2-IMPACT-R25 / DESIGN-P2-R26 / TESTDESIGN-P2-R27`
- Status: `NEEDS_REVIEW / MACHINE_BLOCKED`
- Decisions: Direct Bridge ACTIVE; AC-007 Option B ACTIVE; AccessOperation READ/WRITE-only

```text
REQAN-P2-R01@d08612768131 + Overlay R04
        |
        v
BM-R20
        |
        v
FLOW-R11
        |------------------> P2-IMPACT-R25 (parallel projection)
        v
DESIGN-P2-R26
        |
        v
TESTDESIGN-P2-R27
```

BM-R20 and FLOW-R11 retain their independently reviewed semantic PASS. Exact downstream revision linkage is owned by this graph and task traceability; downstream artifacts are not authoritative upstream inputs.

## Trusted MODEL production chain

```text
Compiler/P1 target resolution
 -> CONTEXT RuntimeBindingPlan(TargetKey + CompiledTargetBinding)
 -> STARTER builds RuntimeModelFrameRequest(plan + deep-immutable source snapshot)
 -> MODEL RuntimeModelRuntimes.production(captured EngineContext)
 -> MODEL RuntimeModelRuntime.open(request)
      -> verify exact plan membership
      -> exact target view = plan.compiledTargetBinding.targetViewKey
      -> create NEW ModelData under that captured-context view definition
      -> atomically freeze provenance + handle
      -> after all inputs succeed create frame + sealed session
 -> MODEL returns RuntimeModelExecution(frame + session) to STARTER
 -> STARTER exact resolver + one-shot capability + Guard
 -> MODEL actual READ / rollback-safe WRITE
```

Forbidden: existing ModelData as trusted input, `ModelData.name`/metadata/default-context identity inference, raw selector reparse, partial frame/session publication, caller-injected runtime/session/Guard/operation port, handle rebind, frame relabel, first-match fallback or Guard bypass.

Current CMI IDs: `CMI-P2-COMPILE-004`, `CMI-P2-PROTECTED-ACCESS-005`.

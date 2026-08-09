# P2 Dependency Graph

- Project：`doc-eq-code`
- Version：`V_1.0`
- Current candidate：`BM-R20 / FLOW-R10 / P2-IMPACT-R22 / DESIGN-P2-R22 / TESTDESIGN-P2-R23`
- Status：`NEEDS_REVIEW / MACHINE_BLOCKED`
- Decisions：AC-007 `OPTION_B / ACTIVE`；AccessOperation `READ_WRITE_ONLY / ACTIVE`

## Authoritative revision direction

```text
REQAN-P2-R01@d08612768131 + Overlay R04
        |
        v
BM-R20 (complete current business-model snapshot)
        |
        v
FLOW-R10
        |
        +--> P2-IMPACT-R22
        |
        v
DESIGN-P2-R22
        |
        v
TESTDESIGN-P2-R23
```

No downstream artifact is an authoritative upstream input.

## Compile / publication relationship

```text
FLOW-CONFIG-COMPILE
  |
  +-> dec-core-compiler
  |    symbol/reference/path/policy validation
  |        |
  |        v
  +-> dec-core-context
  |    immutable EngineContext + PolicyIndex candidate representation
  |        |
  |        v
  +-> dec-core-compiler
       atomic publication coordinator
       -> whole new Context or unchanged old Context
```

`CMI-P2-COMPILE-003` is the structured cross-module implementation contract for this flow. CONTEXT is not publication owner and no global/default current Context is introduced.

## Runtime protected-access relationship

```text
explicit EngineContext + RuntimeExecutionFrameSnapshot
        |
        v
dec-core-starter / ProtectedAccessRuntimeFactory
        |
        v
ProtectedAccessComposition
  exact frame/owner/session
        |
        v
RuntimeTargetResolver
  RuntimeBindingPlan + frame/owner/cursor + sealed session
        |
        v
ResolvedRuntimeTarget(sessionId, objectId, proof)
        |
        +-- READ --> Guard --> dec-core-model immutable snapshot
        |
        `-- WRITE -> RuntimeMutationStamp(sessionId,objectId,path,version)
                    -> intent 0/1/N
                    -> capability
                    -> Guard
                    -> dec-core-model actual-ModelData coordination/transaction
```

`CMI-P2-PROTECTED-ACCESS-003` is the structured cross-module implementation contract.

## Actual runtime-object concurrency boundary

```text
actual ModelData/runtime handle
        |
        1:1
        v
RuntimeModelCoordinationCell
  activeSessionLease
  per-ModelPath lock + RuntimeMutationVersion
```

One actual ModelData cannot have two active session registrations. This prevents cross-session aliases from creating independent version/lock domains.

## Dependency direction

```text
compiler -> context              allowed
starter -> context               allowed
starter -> model                 allowed production assembly
model -> context                 allowed/existing
P3/P4/P6 core -> context         allowed
P3/P4/P6 core -> starter         forbidden
```

## P2 / P7 boundary

P2 RuntimeModelSession and WRITE transaction are internal protected-operation seams only. User/session lifecycle, cross-request transaction/resource lifetime and P7 convergence remain outside P2.

## Gate

All 20 formal P1 findings remain OPEN. Current risk scan and same-revision specialist Review/TDD Evidence remain absent; Implementation Plan/TDD/Development remain BLOCKED.

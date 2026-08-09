# COMPILER P2 Architecture

> Revision：`DESIGN-P2-R24`
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`
> CrossModule Projection：`P2-IMPACT-R23`

## Compile / publication

```text
dec-core-compiler
  symbol/reference/path/selector/policy validation
  P1 targetView + TargetPropertyPath(kind,value)
        |
        v
dec-core-context
  neutral CompiledTargetBinding + RuntimeBindingPlan
  immutable EngineContext + PolicyIndex candidate representation
        |
        v
dec-core-compiler
  atomic publication coordinator
```

Compiler performs the one and only selector resolution. Context owns neutral compiled values; it does not depend on compiler-only `SystemViewSelector/TargetPropertyPath`, does not initiate publication, and introduces no global/default current Context.

## Production registration provenance

```text
exact EngineContext
+ RuntimeExecutionFrameSnapshot
    frameId / owner / optional cursor
    List<RuntimeModelRegistrationInput>
        |
        | starter exact membership validation
        v
RuntimeModelRegistrationInput
    TargetKey
    CompiledTargetBinding
    ModelData
        |
        | validated pair only
        v
dec-core-model RuntimeModelSession.register(...)
        |
        v
sealed typed registrations
```

`RuntimeModelRegistrationInput` is starter-owned production/internal assembly data because starter already depends on context and model. It is not exposed through the neutral business caller port and does not grant permission. Each `(TargetKey, CompiledTargetBinding)` must be present in an exact `RuntimeBindingPlan` in the captured EngineContext before ModelData registration.

Forbidden provenance sources: `ModelData.name`, `ViewData`, list order, raw XML/YAML/definitions, selector re-parsing, first-match scanning, or a new global mutable association map.

## Protected runtime access

```text
application / P3-P4-P6 consumer
        |
        v neutral ProtectedAccessPort (context contract)
dec-core-starter
  explicit EngineContext composition
  validated typed runtime registrations
  compiler-produced RuntimeBindingPlan
  RuntimeTargetResolver (exact sourceTargetKey + compiled-binding match only)
  one-shot capability
  Gateway / Guard
        |
        v
dec-core-model
  sealed RuntimeModelSession
  actual ModelData 1:1 RuntimeModelCoordinationCell
  real READ / rollback-safe WRITE
```

`RuntimeTargetResolver` is the sole `RuntimeBindingPlan -> ResolvedRuntimeTarget` selection path. `RuntimeMutationStamp(sessionId, objectId, path, version)` binds WRITE concurrency proof to the same resolved target. Registration provenance selects no permission: permission remains `ModelAccessRuleKey + ModelAccessPolicyIndex + Guard`.

## Dependency rules

```text
compiler -> context                allowed
starter -> context                 allowed
starter -> model                   allowed for production assembly
model -> context                   existing/allowed
context -> compiler                forbidden
model -> starter                   forbidden
P3/P4/P6 core -> context           allowed
P3/P4/P6 core -> starter           forbidden
business caller -> registration input/Guard/model port forbidden
```

## Runtime identity / concurrency

- `RuntimeModelSession` is a Java interface and **extends `AutoCloseable`**;
- explicit `RuntimeModelSessionId` distinguishes scope without parsing opaque `RuntimeObjectId`;
- one actual ModelData/runtime handle has one coordination cell and at most one active session registration lease;
- per-path lock/version is actual-model scoped, not session scoped;
- stale/failure WRITE consumes capability but leaves observable model state unchanged.

## Trace ownership

BM-R20 and FLOW-R10 keep stable/non-authoritative downstream projection refs. Exact current `DESIGN-P2-R24 / TESTDESIGN-P2-R25 / P2-IMPACT-R23` linkage is maintained by the central dependency/traceability projections, avoiding upstream artifact churn on every downstream revision.

## P2/P7 boundary

RuntimeModelSession, registration lease and mutation transaction are P2 internal protected-operation seams only; they do not define P7 business-session lifecycle, cross-request transaction scope or general resource ownership.

## Gate

Candidate only; same-revision ApiContract/Architecture/Develop/Impact/CrossModule/Concurrency Review and current risk Evidence remain required.

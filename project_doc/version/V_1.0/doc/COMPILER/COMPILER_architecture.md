# COMPILER P2 Architecture

> Revision：`DESIGN-P2-R22`
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`

## Compile / publication

```text
dec-core-compiler
  symbol/reference/path/policy validation
        |
        v
dec-core-context
  immutable EngineContext + PolicyIndex candidate representation
        |
        v
dec-core-compiler
  atomic publication coordinator
```

Context does not initiate publication and no global/default current Context is introduced.

## Protected runtime access

```text
application / P3-P4-P6 consumer
        |
        v neutral ProtectedAccessPort (context contract)
dec-core-starter
  explicit EngineContext composition
  exact frame snapshot
  RuntimeTargetResolver
  one-shot capability
  Gateway / Guard
        |
        v
dec-core-model
  sealed RuntimeModelSession
  actual ModelData 1:1 RuntimeModelCoordinationCell
  real READ / rollback-safe WRITE
```

`RuntimeTargetResolver` is the sole `RuntimeBindingPlan -> ResolvedRuntimeTarget` selection path. `RuntimeMutationStamp(sessionId, objectId, path, version)` binds WRITE concurrency proof to that same target.

## Dependency rules

```text
compiler -> context                allowed
starter -> context                 allowed
starter -> model                   allowed for production assembly
model -> context                   existing/allowed
P3/P4/P6 core -> context           allowed
P3/P4/P6 core -> starter           forbidden
business caller -> Guard/model port forbidden
```

## Runtime identity / concurrency

- explicit `RuntimeModelSessionId` distinguishes scope without parsing opaque `RuntimeObjectId`;
- one actual ModelData/runtime handle has one coordination cell and at most one active session registration lease;
- per-path lock/version is actual-model scoped, not session scoped;
- stale/failure WRITE consumes capability but leaves observable model state unchanged.

## P2/P7 boundary

RuntimeModelSession and mutation transaction are P2 internal protected-operation seams only; they do not define P7 business-session lifecycle, cross-request transaction scope or general resource ownership.

## Gate

Candidate only; same-revision Architecture/Impact/CrossModule/Concurrency Review and risk Evidence remain required.

# COMPILER P2 Detailed Design — Authority Boundary and Single Runtime Context Lifecycle Overlay

> Revision `DESIGN-P2-R33`; supersedes the runtime-context portions of `DESIGN-P2-R32` while preserving the R32 raw-authority remediation.
> Base: `DESIGN-P2-R30 + DESIGN-P2-R31 WRITE Value Transport Overlay + DESIGN-P2-R32`.
> Effective inputs: `REQAN-P2-R01@d08612768131 + Requirement Overlay R04 + BM-R20 + FLOW-R11 + DEC-P2-SINGLE-RUNTIME-CONTEXT-001`.
> Operation scope remains `READ / WRITE only`; `EXECUTE = N/A`.

## R33-1 Architecture decision

Current P2 production runtime uses a **single EngineContext per runtime lifecycle/generation**.

A runtime generation:

1. obtains one compiler-published `EngineContext` during bootstrap;
2. constructs its MODEL/STARTER production roots and protected-access composition from that same bootstrap Context;
3. keeps that Context immutable for the whole generation;
4. exposes no supported live-rebind, context-swap, runtime-republication or hot-reload operation.

Configuration change is applied only by lifecycle replacement:

```text
old runtime generation
  -> stop accepting work
  -> close old composition / RuntimeModelExecutionRoot / Scope / Frame / Handle
  -> terminate old generation
  -> compile/publish configuration for next startup
  -> start new runtime generation
  -> bind exactly one new EngineContext
```

Compiler/test/offline code may construct multiple candidate EngineContexts across independent compilation sessions or runtime generations. That is compatible with this rule and is not a live-runtime multi-context contract.

## R33-2 Upstream semantic reconciliation

Requirement Overlay R04 retains:

- atomic publication;
- old Context preservation;
- Context isolation.

R33 interprets those clauses at the **Compiler publication / distinct runtime-generation boundary**. They do not require an already-running production runtime to replace its bound Context in place.

Therefore:

- `REQAN-P2-R01 + Overlay R04` remains unchanged;
- `BM-R20` remains unchanged;
- `FLOW-R11` remains unchanged;
- only Design and downstream TestDesign/Implementation artifacts require refresh.

The current decision narrows supported runtime lifecycle behavior; it does not weaken READ/WRITE authorization, publication atomicity or isolation between independent compiled contexts.

## R33-3 P2-CR-001 remains blocking

`P2-CR-001` remains a P0 security defect.

MODEL currently exposes a raw effect acquisition chain plus proofless resolved-access factories that can allow a caller reaching MODEL to execute READ/WRITE without the unique `ExactModelAccessGuard` authority.

The R32 remediation remains fully active:

- `ExactModelAccessGuard` is the sole READ/WRITE policy authority;
- a successful Guard decision may mint a non-forgeable opaque effect authorization;
- ordinary callers cannot construct, clone, infer or downgrade that authorization;
- a usable raw `RuntimeModelOperationPort` must not be available to ordinary production callers without Guard authorization;
- `beginSession()`, `effectProvider()`, raw operation-port extraction and proofless access factories may be removed, visibility-reduced or internalized;
- any retained internal primitive must reject absent, wrong-operation, wrong-target/path, stale or consumed authorization before effect;
- DENY remains deterministic and effect-free;
- WRITE preserves the R31 value + mutation-stamp/version invariants.

The authorization binds the facts required to protect the effect:

- exact `READ|WRITE` operation;
- exact `ModelAccessRuleKey` / canonical path;
- exact resolved target;
- exact session/object identity;
- for WRITE, the frozen mutation stamp/version/value contract.

No separate `RuntimeContextBinding` field is required because the supported active runtime owns exactly one immutable EngineContext generation.

## R33-4 P2-CR-002 is superseded by runtime lifecycle scope

`P2-CR-002` was raised because STARTER currently uses structural `RuntimeBindingPlan.equals()` when comparing a captured Context with a Scope/Handle, and R32 assumed that two EngineContexts could be active within one supported runtime lifecycle.

`DEC-P2-SINGLE-RUNTIME-CONTEXT-001` removes that premise.

For current P2:

```text
Runtime generation A -> EngineContext A only
Runtime generation B -> EngineContext B only
```

A Scope/Frame/Handle from generation A is not a supported input to generation B. Cross-generation object mixing is outside the supported runtime contract and is not a P2 authorization boundary that must be repaired with per-object context identity propagation.

Consequences:

- do **not** introduce `RuntimeContextBinding`;
- do **not** propagate exact EngineContext-generation identity through every MODEL provenance object;
- `RuntimeBindingPlan` remains a structural execution plan and may continue to use value equality for plan semantics;
- no P2 blocking acceptance requires `Context A + Scope B -> PROVENANCE_MISMATCH`;
- no P2 blocking acceptance requires two structurally identical EngineContexts to be identity-distinguished inside one active runtime.

This is a scope correction, not a claim that two independently created EngineContext objects are the same Java instance.

## R33-5 Runtime ownership / composition boundary

The production composition root owns the one runtime Context.

Supported composition semantics:

```text
compiler-published EngineContext
  -> runtime bootstrap captures once
  -> RuntimeModelExecutionRoot / MODEL lifecycle
  -> ProtectedAccessRuntimeFactory / ProtectedAccessComposition
  -> Rule / Change / CustomAction protected entries
  -> ProtectedExecutionBridge
  -> Gateway
  -> ExactModelAccessGuard
  -> authorized MODEL effect
```

Business consumers operate through the runtime services created from that bootstrap generation. They do not replace the active Context.

Existing APIs such as `CompilerBootstrap.compileAndPublish(...)` or a public constructor/factory that can be used to create a separate standalone runtime do not constitute a hot-reload API for an already-running generation. If implementation exposes an explicit active-runtime `setContext`, `reloadContext`, `replaceContext`, or equivalent rebinding path, it violates R33 and must be closed.

No global JVM singleton is required. Multiple independent application/runtime instances in one JVM are allowed; each instance is its own runtime lifecycle and each binds exactly one EngineContext.

## R33-6 Authority/effect sequencing

The legal protected-access sequence becomes:

```text
runtime bootstrap captures its only EngineContext
  -> MODEL root/scope is created inside that runtime generation
  -> resolve exact target
  -> exact Guard lookup using READ/WRITE key
  -> mint one-shot opaque authorization
  -> bind MODEL session/effect
  -> consume authorization exactly once
  -> effect
```

No effect primitive may infer permission from `RuntimeBindingPlan.equals()`, plan content, policy digest, source target or caller-provided markers.

The removal of `RuntimeContextBinding` does not allow bypass of Guard, target/path/operation checks, session checks or WRITE mutation checks.

## R33-7 Failure and restart semantics

Supported failures remain deterministic:

- missing rule / operation mismatch / wrong target/path -> DENY before protected effect;
- missing, invalid, replayed or wrong-operation effect authorization -> DENY before effect;
- stale/closed/mismatched MODEL session -> existing session/effect binding failure;
- stale WRITE mutation stamp -> existing WRITE stale/version failure;
- configuration change requested while runtime is active -> **no in-place Context mutation**; operational action is restart/new runtime generation.

A failed compilation/publication for the next runtime generation does not mutate the currently running generation's bound EngineContext. This preserves R04/BM publication semantics.

## R33-8 Dependency direction and implementation scope

Dependency direction remains:

```text
compiler -> context
model -> context
starter -> context + model
business consumer -> starter + context
```

Authorized implementation scope remains limited to `dec-core-context`, `dec-core-model`, `dec-core-starter` and tests where required for P2-CR-001 closure and single-runtime composition integrity.

No DB/schema change, Compiler grammar change, RuleView ownership change, PolicyIndex ownership change, P3/P4/P6 business execution expansion, or hot-reload mechanism is authorized.

## R33-9 TDD boundary

Before production mutation for the security remediation, Development must freeze genuine RED only for the current defects that remain blocking under R33:

1. ordinary caller can reach a usable raw MODEL operation seam without Guard;
2. READ-only policy can use the raw path to WRITE;
3. proofless READ transport can participate in executable READ authority;
4. proofless WRITE transport can participate in executable WRITE authority.

The former same-plan cross-context cases are not converted to fake GREEN. They are retired/superseded because their supported-runtime premise was removed by `DEC-P2-SINGLE-RUNTIME-CONTEXT-001`.

Single-runtime lifecycle/no-hot-reload checks are Design contracts and TestDesign GREEN/regression gates, not substitutes for the four genuine security REDs.

## R33-10 Completion criteria

`DESIGN-P2-R33` is complete when independent Review confirms:

1. P2-CR-001 remains fully closed by the planned Guard-authorized effect boundary;
2. no exact `RuntimeContextBinding` propagation remains required;
3. active runtime binds one EngineContext exactly once and exposes no supported hot reload/rebind;
4. config update is restart/new-generation only;
5. Compiler may still create multiple candidates across sessions/generations without violating runtime single-context semantics;
6. Requirement Overlay R04 / BM-R20 / FLOW-R11 are not semantically weakened;
7. TestDesign retires the two cross-context REDs explicitly and keeps the four genuine P0 REDs;
8. downstream phases remain blocked until canonical lifecycle reconciliation is safely appended.

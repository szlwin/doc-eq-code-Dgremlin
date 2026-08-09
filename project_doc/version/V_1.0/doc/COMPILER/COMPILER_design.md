# COMPILER P2 详细设计

> Revision：`DESIGN-P2-R24`。Base：`DESIGN-P2-R23`。
> Authoritative Inputs：`REQAN-P2-R01@d08612768131` + Overlay R04 + `BM-R20` + `FLOW-R10@p2-system-ruleview-protected-access`。
> CrossModule Projection：`P2-IMPACT-R23`（non-authoritative review projection）。
> Decisions：Direct Bridge ACTIVE；AC-007 Option B ACTIVE；READ/WRITE-only ACTIVE。
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`。

R24 保留独立 Review 已确认正确的 P1 `TargetKey(shared ViewKey)`、READ/WRITE-only、两行 Policy truth table、`ModelAccessRuleKey` WRITE authority、单一 WRITE ModelPath、0/1/N WriteIntent、typed IDs、`RuntimeFactValue`、compiler-produced `CompiledTargetBinding`、explicit EngineContext composition、RuntimeMutationStamp、actual-ModelData coordination 和 P2/P7 boundary。本 revision 只关闭 production `CompiledTargetBinding ↔ ModelData` registration provenance、Java interface contract 和 downstream trace/Impact 同步问题。

## 1. Revision DAG

```text
REQAN-P2-R01@d08612768131 + Overlay R04
 -> BM-R20
 -> FLOW-R10
 -> DESIGN-P2-R24
 -> TESTDESIGN-P2-R25
```

`P2-IMPACT-R23` is a parallel cross-module/impact projection of the same current facts, not an authoritative upstream input to Design. BM/Flow canonical artifacts use stable trace/artifact refs for downstream projection so future Design/TestDesign revision increments do not invalidate upstream business semantics.

## 2. Compile identity / policy semantics

```text
sourceModel -> existing shared ViewKey -> TargetKey(ViewKey)
sourcePath  -> exact ModelPath

ModelAccessRuleKey
 = authorizationOwnerSystemKey
 + TargetKey
 + ModelPath
 + AccessOperation(READ|WRITE)
```

TargetKey 不 System-qualify source View。Runtime 无 wildcard、无 EXECUTE、无 bare-name fallback。

只允许：

```text
STATIC_ALLOW           + NONE                  + no plan
RUNTIME_GUARD_REQUIRED + EXACT_RUNTIME_BINDING + plan
```

## 3. Atomic publication responsibility

Requirement ownership is frozen as:

```text
COMPILER
  validate all symbols/references/paths/policies
       |
       v
CONTEXT
  construct immutable EngineContext / PolicyIndex candidate representation
       |
       v
COMPILER
  coordinate atomic publication
  -> new candidate becomes visible as one closure
  -> or previous Context remains unchanged
```

CONTEXT is representation holder, not publication coordinator. P2 introduces no global/default current Context.

<a id="compiled-runtime-binding-plan"></a>
## 4. Compiler-produced neutral RuntimeBindingPlan

R24 preserves the independently verified R23 compiler-resolved binding without changing BM-R20/FLOW-R10 business semantics. P1 already resolves `targetView + selector` during compilation into a typed `TargetPropertyPath(kind,value)`. P2 therefore must publish that resolved meaning, not downgrade it back to raw Strings.

```text
P1 compiler facts
  ViewKey targetView
  SystemViewSelector lexical selector        (compiler-only input)
  TargetPropertyPath(kind, exactValue)       (resolved compiler fact)
        |
        | one-way compiler adaptation
        v
dec-core-context neutral fact
  CompiledTargetBinding(
      ViewKey targetViewKey,
      ResolvedTargetKind TARGET_MAIN|PROPERTY_PATH,
      String exactResolvedValue)
        |
        v
  RuntimeBindingPlan(
      TargetKey sourceTargetKey,
      CompiledTargetBinding compiledTargetBinding)
```

Rules:

- `exactResolvedValue` is the canonical resolved `TargetPropertyPath` value, never the raw selector expression;
- compiler performs selector resolution exactly once using existing P1 semantics;
- runtime never re-parses selector text, never scans raw View definitions/property trees, and never normalizes selector values;
- context does not depend on compiler-only `SystemViewSelector` or `TargetPropertyPath` classes;
- session registration records the same neutral `CompiledTargetBinding`; resolver performs exact value matching only;
- `TARGET_MAIN` and `PROPERTY_PATH` remain distinguishable at runtime without re-interpreting lexical configuration.

<a id="current-api-contract"></a>
## 5. Current API completeness

`COMPILER_api_contract.md@DESIGN-P2-R24` is the complete current P2 API contract. Every P2-added cross-module immutable value has an explicit Java-8-compatible `of(...)`/factory construction path, not just getters. Pre-P2 stable `SystemKey/RuleViewKey/ViewKey/EngineContext/CompiledModelSet` are explicitly treated as existing source-compatible APIs.

The current contract fully defines policy enums/plan, IDs, invocation, runtime target, mutation stamp, resolved READ/WRITE, operation port, results/denials, factory/composition and all new construction surfaces. No superseded R19/R20/R21 design document is required to implement P2.

## 6. Explicit production registration provenance and composition

<a id="runtime-registration-provenance"></a>
The production factory no longer accepts a bare list of ModelData handles. The association required by `RuntimeModelSession.register(...)` is explicit typed assembly input:

```text
RuntimeModelRegistrationInput
  TargetKey sourceTargetKey
  CompiledTargetBinding compiledTargetBinding
  ModelData modelData
```

Ownership and authority rules:

1. `RuntimeModelRegistrationInput` is a `dec-core-starter` production/internal assembly value because starter may depend on both neutral context contracts and `dec-core-model`.
2. It is not exposed through `ProtectedAccessPort` and is not permission authority.
3. `ProtectedAccessRuntimeFactory.production(exact EngineContext)` captures one immutable Context.
4. `create(RuntimeExecutionFrameSnapshot)` receives frame/owner/cursor plus immutable `RuntimeModelRegistrationInput` values.
5. Before model-session registration, starter validates `(sourceTargetKey, compiledTargetBinding)` is an exact current `RuntimeBindingPlan` pair in that captured EngineContext.
6. Only after that membership check may starter call model-session `register(frame, owner, cursor, sourceTargetKey, compiledTargetBinding, modelData)` and seal the session.
7. The association must never be reconstructed from `ModelData.name`, `ViewData`, list order, raw XML/YAML/definitions, first-match iteration or selector parsing.
8. Passing a valid binding pair does not grant READ/WRITE permission; permission remains exclusively `ModelAccessRuleKey + PolicyIndex + Guard`.

```text
explicit EngineContext
+ RuntimeExecutionFrameSnapshot(
    frameId,
    ownerResolutionId,
    optional cursorId,
    List<RuntimeModelRegistrationInput>)
        |
        v
starter registration provenance validation
        |
        v
sealed RuntimeModelSession
        |
        v
RuntimeTargetResolver
```

Composition construction fails closed if association provenance is missing, not present in the captured Context, duplicated incompatibly, or conflicts with actual ModelData active ownership. No protected invocation is created from a partially built session.

## 7. Unique RuntimeTargetResolver

Direct caller supplies `ModelAccessRuleKey + typed frame/owner/cursor`, not a RuntimeObjectId.

Before selection:

```text
invocation.frameId == composition.frameId
invocation.ownerResolutionId == composition.ownerResolutionId
```

else `RUNTIME_CONTEXT_MISMATCH`.

Then the **only** resolver is:

```text
RuntimeBindingPlan(sourceTargetKey + compiler-produced CompiledTargetBinding)
+ exact composition-bound frame/owner/cursor
+ sealed RuntimeModelSession containing explicit validated (sourceTargetKey + CompiledTargetBinding + ModelData) registration facts
        |
        v
RuntimeTargetResolver
  exact match only; no raw selector/property-tree re-resolution
  0 -> RUNTIME_TARGET_NOT_FOUND
  1 -> ResolvedRuntimeTarget(
         RuntimeModelSessionId,
         RuntimeObjectId,
         TargetKey,
         typed context,
         RuntimeBindingProof)
  N -> RUNTIME_TARGET_AMBIGUOUS
```

No “first object”, frame-only, owner-only, cursor-only, `ModelData.name`, `ViewData`, list-order, raw-definition or alternate fallback is legal. Guard and operation consume the same immutable `ResolvedRuntimeTarget`.

<a id="runtime-model-session"></a>
## 8. Session scope without opaque-ID inference

Each production session has a separate opaque `RuntimeModelSessionId`. `RuntimeObjectId` remains opaque and never encodes scope.

Registration storage is explicit: every session entry stores the exact `TargetKey + CompiledTargetBinding + ModelData` association supplied by the validated production assembly. Model never derives either binding key from ModelData metadata. `RuntimeModelSession` is a Java interface and **extends `AutoCloseable`**.

```text
target.sessionId != currentSession.sessionId
  -> RUNTIME_SESSION_SCOPE_MISMATCH

same active session, object absent
  -> RUNTIME_OBJECT_NOT_FOUND

same session/binding previously valid but closed/expired
  -> RUNTIME_OBJECT_STALE
```

Therefore cross-session classification is deterministic without a global registry or semantic parsing of `RuntimeObjectId`.

## 9. Actual ModelData ownership and cross-session concurrency

Session-local locks/versions are insufficient because current `ModelLoader` directly holds mutable `ModelData`. R22 freezes the concurrency owner at the actual runtime model identity:

```text
actual ModelData/runtime handle
        |
        1:1
        v
RuntimeModelCoordinationCell
  activeSessionLease
  Map<ModelPath, lock + RuntimeMutationVersion>
```

Rules:

1. first active registration acquires the lease;
2. same-session duplicate registration of the same actual ModelData -> `RUNTIME_OBJECT_ALREADY_REGISTERED`;
3. another active session registering the same actual ModelData -> `RUNTIME_OBJECT_OWNERSHIP_CONFLICT`;
4. closing the owner session releases only the active lease;
5. per-path version/coordination state remains attached to the actual model coordination cell, so a new session does not reset version history;
6. the coordination cell is model-internal metadata, not a global mutable object registry.

This prevents Session A and Session B from acquiring independent locks/versions over the same mutable ModelData.

## 10. Atomic RuntimeMutationStamp

WRITE version proof is no longer a bare number:

```text
RuntimeMutationStamp
 = RuntimeModelSessionId
 + RuntimeObjectId
 + exact ModelPath
 + RuntimeMutationVersion
```

`ResolvedWriteIntent` freezes:

```text
ModelAccessRuleKey
+ ResolvedRuntimeTarget
+ RuntimeMutationStamp
+ Optional<RuleKey> provenance
```

Construction rejects unless:

```text
stamp.sessionId == target.sessionId
stamp.runtimeObjectId == target.runtimeObjectId
stamp.modelPath == ModelAccessRuleKey.modelPath
```

`ResolvedProtectedWriteAccess` contains only `invocationId + ResolvedWriteIntent`; it cannot splice object B with version from object A.

## 11. Protected operation ordering

```text
explicit composition
 -> validate invocation frame/owner
 -> unique RuntimeTargetResolver
 -> READ resolved access
    or WRITE 0/1/N intent + atomic mutation stamp
 -> mint/atomic-consume capability
 -> exact PolicyIndex/runtime-proof Guard
 -> production model operation
```

Target/stamp/authority cannot be replaced after Guard.

## 12. READ / WRITE failure semantics

READ:

- production adapter reads actual model/path;
- returns deep immutable RuntimeFactValue;
- model mutation/version change = 0.

WRITE after Guard ALLOW:

```text
actual-model/path coordination lock
 -> compare current version with stamp.version
 -> stale: WRITE_INTENT_STALE, mutation=0
 -> else apply isolated/frozen mutation
 -> commit data/model transaction
 -> publish committed state
 -> increment version once
 -> receipt
```

Any mutation/commit failure rolls back/restores externally observable `ModelData`/origin state and returns no receipt. Because capability was already consumed:

```text
modelStateChanged      = false
capabilityStateChanged = true
```

No automatic retry/reselection occurs.

## 13. Cross-module closure

`P2-IMPACT-R23` contains both required CMIs.

Compile CMI:

```text
CMI-P2-COMPILE-003
COMPILER -> CONTEXT immutable candidate construction
COMPILER -> CONTEXT atomic publication command
```

Runtime CMI:

```text
CMI-P2-PROTECTED-ACCESS-003
STARTER -> MODEL target selection/session
STARTER -> CONTEXT exact Guard facts
STARTER -> MODEL real READ/WRITE operation
```

Dependency rules remain:

```text
P3/P4/P6 core -> dec-core-context : allowed
P3/P4/P6 core -> dec-core-starter : forbidden
starter -> dec-core-model          : allowed production assembly
```

## 14. P2 / P7 boundary

The R22 runtime session/transaction/version concepts are deliberately narrow P2 internal seams:

- `RuntimeModelSession` = one composition/frame protected-operation locator scope;
- transaction = one protected WRITE atomic mutation;
- coordination cell = actual-model alias/concurrency guard for this protected operation seam.

P2 **does not** define user/session lifecycle, cross-request transaction scope, general resource ownership/lifetime or P7 declaration/session convergence. Those remain P7.

## 15. Gate

No production Java, risk Evidence or TDD execution is claimed. `DESIGN-P2-R24` requires same-revision ApiContract/Architecture/Develop/Impact/CrossModule/Concurrency Review and current risk scan. Implementation Plan/TDD/Development remain BLOCKED.

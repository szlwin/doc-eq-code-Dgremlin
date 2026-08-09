# COMPILER P2 详细设计

> Revision：`DESIGN-P2-R22`。Base：`DESIGN-P2-R21`。
> Inputs：`REQAN-P2-R01@d08612768131` + Overlay R04 + `BM-R20` + `FLOW-R10@p2-system-ruleview-protected-access` + `P2-IMPACT-R22`。
> Decisions：Direct Bridge ACTIVE；AC-007 Option B ACTIVE；READ/WRITE-only ACTIVE。
> Status：`NEEDS_REVIEW / BLOCKED_BY_BM_REVIEW / MACHINE_BLOCKED`。

R22 保留已独立 Review 确认正确的 P1 `TargetKey(shared ViewKey)`、READ/WRITE-only、两行 Policy truth table、`ModelAccessRuleKey` WRITE authority、单一 WRITE ModelPath、0/1/N WriteIntent、typed IDs、`RuntimeFactValue` 与 R22 exact RED registry 方向。本 revision 只关闭 current snapshot/API completeness、compile-side responsibility/impact、runtime target selection、object/version atomic binding、explicit composition input、cross-session alias concurrency 和 P2/P7 boundary。

## 1. Revision DAG

```text
REQAN-P2-R01@d08612768131 + Overlay R04
 -> BM-R20
 -> FLOW-R10
 -> P2-IMPACT-R22
 -> DESIGN-P2-R22
 -> TESTDESIGN-P2-R23
```

`BM-R20` 是完整 current snapshot；`baseRevision` 仅表达 lineage，不代表省略的 BM-R18/R19 事实会被工具隐式继承。

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

<a id="current-api-contract"></a>
## 4. Current API completeness

`COMPILER_api_contract.md@DESIGN-P2-R22` is the complete current P2 API contract. Every P2-added cross-module immutable value has an explicit Java-8-compatible `of(...)`/factory construction path, not just getters. Pre-P2 stable `SystemKey/RuleViewKey/ViewKey/EngineContext/CompiledModelSet` are explicitly treated as existing source-compatible APIs.

The current contract fully defines policy enums/plan, IDs, invocation, runtime target, mutation stamp, resolved READ/WRITE, operation port, results/denials, factory/composition and all new construction surfaces. No superseded R19/R20/R21 design document is required to implement P2.

## 5. Explicit production composition

Production assembly is explicit:

```text
ProtectedAccessRuntimeFactory.production(exact EngineContext)
        |
        + create(RuntimeExecutionFrameSnapshot)
                  frameId
                  ownerResolutionId
                  Optional<cursorId>
                  immutable runtime ModelData handles
        |
        v
ProtectedAccessComposition
  exact EngineContext
  exact frame/owner
  sealed RuntimeModelSession
  production RuntimeTargetResolver
  production model adapter
  Gateway / Guard
  shared Rule/Change/CustomAction Bridge
```

A factory implementation may not read a global/default Context. Runtime `ModelData` handles enter only through the assembly snapshot.

<a id="runtime-target-resolution"></a>
## 6. Unique RuntimeTargetResolver

Direct caller supplies `ModelAccessRuleKey + typed frame/owner/cursor`, not a RuntimeObjectId.

Before selection:

```text
invocation.frameId == composition.frameId
invocation.ownerResolutionId == composition.ownerResolutionId
```

else `RUNTIME_CONTEXT_MISMATCH`.

Then the **only** resolver is:

```text
RuntimeBindingPlan
+ exact composition-bound frame/owner/cursor
+ sealed RuntimeModelSession
        |
        v
RuntimeTargetResolver
  0 -> RUNTIME_TARGET_NOT_FOUND
  1 -> ResolvedRuntimeTarget(
         RuntimeModelSessionId,
         RuntimeObjectId,
         TargetKey,
         typed context,
         RuntimeBindingProof)
  N -> RUNTIME_TARGET_AMBIGUOUS
```

No “first object”, frame-only, owner-only, cursor-only, `ModelData.name` or alternate fallback is legal. Guard and operation consume the same immutable `ResolvedRuntimeTarget`.

<a id="runtime-model-session"></a>
## 7. Session scope without opaque-ID inference

Each production session has a separate opaque `RuntimeModelSessionId`. `RuntimeObjectId` remains opaque and never encodes scope.

```text
target.sessionId != currentSession.sessionId
  -> RUNTIME_SESSION_SCOPE_MISMATCH

same active session, object absent
  -> RUNTIME_OBJECT_NOT_FOUND

same session/binding previously valid but closed/expired
  -> RUNTIME_OBJECT_STALE
```

Therefore cross-session classification is deterministic without a global registry or semantic parsing of `RuntimeObjectId`.

## 8. Actual ModelData ownership and cross-session concurrency

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

## 9. Atomic RuntimeMutationStamp

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

## 10. Protected operation ordering

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

## 11. READ / WRITE failure semantics

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

## 12. Cross-module closure

`P2-IMPACT-R22` contains both required CMIs.

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

## 13. P2 / P7 boundary

The R22 runtime session/transaction/version concepts are deliberately narrow P2 internal seams:

- `RuntimeModelSession` = one composition/frame protected-operation locator scope;
- transaction = one protected WRITE atomic mutation;
- coordination cell = actual-model alias/concurrency guard for this protected operation seam.

P2 **does not** define user/session lifecycle, cross-request transaction scope, general resource ownership/lifetime or P7 declaration/session convergence. Those remain P7.

## 14. Gate

No production Java, risk Evidence or TDD execution is claimed. `DESIGN-P2-R22` still requires same-revision ApiContract/Architecture/Develop/Impact/CrossModule/Concurrency Review and current risk scan. Implementation Plan/TDD/Development remain BLOCKED.

# COMPILER P2 Detailed Design

> Revision: `DESIGN-P2-R25`. Base: `DESIGN-P2-R24`.
> Authoritative inputs: `REQAN-P2-R01@d08612768131` + Overlay R04 + `BM-R20` + `FLOW-R11`.
> CrossModule projection: `P2-IMPACT-R24` (parallel/non-authoritative).
> Status: `NEEDS_REVIEW / MACHINE_BLOCKED`.

R25 preserves the independently reviewed P2 semantics (shared-View TargetKey, READ/WRITE-only, ModelAccessRuleKey authority, exact ModelPath, 0/1/N target/intent, compiler-produced CompiledTargetBinding, explicit EngineContext, RuntimeMutationStamp, actual-ModelData coordination, rollback and P2/P7 boundary). It closes only trusted runtime-model provenance, current API completeness/visibility, Flow/CMI ownership alignment and TestDesign executability.

## 1. Current revision chain

```text
REQAN-P2-R01@d08612768131 + Overlay R04
 -> BM-R20
 -> FLOW-R11
 -> DESIGN-P2-R25
 -> TESTDESIGN-P2-R26

parallel projection: P2-IMPACT-R24
```

BM-R20 remains semantic PASS; it is not rewritten. FLOW-R11 is a new candidate because actual-effect ownership/provenance text changed after independent Review of FLOW-R10.

## 2. Compile/policy/publication semantics preserved

```text
sourceModel -> shared ViewKey -> TargetKey(ViewKey)
sourcePath  -> exact ModelPath
ModelAccessRuleKey = owner System + TargetKey + ModelPath + READ|WRITE
```

Only `STATIC_ALLOW+NONE+no-plan` and `RUNTIME_GUARD_REQUIRED+EXACT_RUNTIME_BINDING+plan` publish. Compiler resolves P1 selector exactly once to `TargetPropertyPath(kind,value)` and publishes neutral `CompiledTargetBinding`. CONTEXT constructs immutable candidate representation; COMPILER coordinates atomic publication. Runtime never reinterprets raw selector syntax.

<a id="trusted-runtime-model-provenance"></a>
## 3. Trusted actual-model provenance

R24 still let assembly declare `(valid binding A, ModelData B)`. R25 removes that surface entirely.

`dec-core-model` owns a trusted `RuntimeModelFrame` containing immutable `RuntimeModelHandle` values. Each handle atomically encapsulates its actual internal `ModelData` plus immutable `RuntimeModelProvenance(TargetKey, CompiledTargetBinding)` created during **the same model-internal materialization operation**. Public/cross-module code can read provenance but cannot construct, wrap, rebind or mutate `RuntimeModelProvenance`, `RuntimeModelHandle` or `RuntimeModelFrame`, and cannot extract `ModelData` from a handle.

```text
model-internal trusted materialization
  exact current RuntimeBindingPlan
  + model materialization/load inputs
        |
        | create/load ModelData and provenance atomically
        v
RuntimeModelHandle [read-only cross-module]
  provenance = TargetKey + CompiledTargetBinding
  internal actual ModelData = not publicly replaceable/exposed
        |
        v
RuntimeModelFrame
  frame/owner/cursor + handles; no public constructor/rebind
        |
        v
RuntimeExecutionFrameSnapshot.from(trustedFrame)
        |
        v
starter validates each handle provenance against captured EngineContext
        |
        v
RuntimeModelSession.register(trustedHandle) -> seal
```

Mandatory implementation owner: package-private `dec.core.model.runtime.RuntimeModelFrameAssembler` in `dec-core-model`. It is the only production creator of `RuntimeModelProvenance`, `RuntimeModelHandle` and `RuntimeModelFrame`; it integrates with the model module's existing ModelData materialization/load lifecycle and freezes the exact compiled binding during that same creation/load handoff. There is no public `wrapExisting(ModelData, binding)` or rebind path. STARTER consumes only the resulting read-only `RuntimeModelFrame`; it never creates handles.

Consequences:
- `valid A + arbitrary existing ModelData B` cannot be expressed by a legal public API;
- swapping list order changes nothing because selection uses immutable handle provenance;
- presenting trusted handle B for plan A yields no matching A candidate/provenance mismatch;
- a handle from another trusted frame cannot be relabeled with a different frame because snapshot facts derive from the frame itself;
- metadata (`ModelData.name`, ViewData/property tree), raw definitions, first-match and selector reparse remain forbidden;
- provenance is not permission; Guard still authorizes exact `ModelAccessRuleKey` before effect.

## 4. Unique target/WRITE flow

```text
captured EngineContext + RuntimeExecutionFrameSnapshot.from(trusted RuntimeModelFrame)
 -> validate all handle provenance against current RuntimeBindingPlan set
 -> sealed RuntimeModelSession of trusted handles
 -> invocation frame/owner equality
 -> RuntimeTargetResolver exact plan-to-handle provenance match
 -> 0 / exactly 1 / N fail-closed selection
 -> freeze READ access or WRITE intent + RuntimeMutationStamp
 -> consume one-shot capability
 -> Guard exact ModelAccessRuleKey + same target/stamp
 -> MODEL-owned real READ / rollback-safe WRITE
```

No caller supplies RuntimeObjectId, ModelData, binding, path or version authority at invocation time.

## 5. API completeness and visibility

`COMPILER_api_contract.md@DESIGN-P2-R25` is self-contained. It defines every P2-added type referenced in current public signatures, including formerly missing `RuntimeTargetResolution`, `LocatedRuntimeObject`, `ProtectedReadValue`, `ProtectedWriteReceipt`, `ProtectedAccessDenial`, and Rule/Change/CustomAction entry interfaces. Every cross-module top-level type is explicitly `public`; trusted model provenance objects intentionally expose no public construction/rebind surface.

Owner-module API verification is split: CONTEXT tests only CONTEXT contracts; MODEL tests MODEL contracts/constructor restrictions; STARTER tests STARTER contracts plus legal cross-module consumption. No context->starter/model test dependency is introduced.

## 6. Module/effect ownership

- CONTEXT: neutral immutable contracts, EngineContext/PolicyIndex representation.
- COMPILER: compile validation + atomic publication coordination.
- MODEL: trusted model/frame provenance, ModelData encapsulation, session/locator/coordination, actual READ/WRITE effect.
- STARTER: production composition, Context provenance validation, resolver, intent, capability, Gateway/Guard, delegation to MODEL.

`FLOW-R11 STEP-P2-ACCESS-06 ownerModule = MODEL` is authoritative for the actual effect. STARTER only invokes/delegates after Guard ALLOW.

## 7. Cross-module closure

Current Impact projection is `P2-IMPACT-R24` and the current CMI IDs are exactly:

```text
CMI-P2-COMPILE-004
CMI-P2-PROTECTED-ACCESS-004
```

No `-003` CMI is current authority. Impact freezes compiler->context compiled binding transport, model-owned trusted handle materialization, starter provenance validation/session registration, and MODEL effect ownership/failure paths.

## 8. Concurrency/atomicity preserved

One actual ModelData/runtime handle has one model-internal coordination cell and at most one active session lease. Per-ModelPath version/lock is actual-model scoped. WRITE stamp binds session/object/path/version to the same resolved trusted handle. Same-version competing capabilities commit at most once; stale loser mutates zero. Operation failure rolls back/restores observable model state; capability remains CONSUMED and no receipt exists.

## 9. P2/P7 and formal Gate

Trusted RuntimeModelFrame/Handle, RuntimeModelSession and one protected-operation transaction are P2 internal runtime seams only, not P7 user/session lifecycle or cross-request transaction ownership.

Formal OPEN P1 findings remain open until same-revision specialist Review, current risk scan and machine Evidence. Implementation Plan/TDD/Development remain BLOCKED.

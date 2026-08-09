# COMPILER P2 Detailed Design

> `DESIGN-P2-R27`; base R26; authoritative inputs `REQAN-P2-R01@d08612768131 + Overlay R04 + BM-R20 + FLOW-R11`; parallel `P2-IMPACT-R26`.
> Status `NEEDS_REVIEW / MACHINE_BLOCKED`.

R27 explicitly withdraws the R26 fresh-snapshot MODEL-open scheme because it introduced object-source/lifecycle/failure semantics not present in BM-R20 or FLOW-R11. BM-R20 core semantics and FLOW-R11 remain unchanged. R27 restores the existing production-object lifecycle and makes the trusted frame precondition implementation-unique.

## 1. Current chain and preserved authority

`REQAN+Overlay -> BM-R20 -> FLOW-R11 -> DESIGN-P2-R27 -> TESTDESIGN-P2-R28`; parallel Impact R26.

Preserved: shared-View `TargetKey`, independent owner System, exact `ModelPath`, READ/WRITE-only, `ModelAccessRuleKey` sole permission authority, compiler-resolved `CompiledTargetBinding`, 0/1/N target/intent, one-shot capability, Guard before MODEL effect, mutation stamp, actual-object coordination, rollback, and P2/P7 boundary.

## 2. R26 fresh-snapshot seam is superseded

The following R26 concepts are **not current production contracts**: `RuntimeModelMaterializationInput(RuntimeFactValue sourceSnapshot)`, caller-created `RuntimeModelFrameRequest`, `RuntimeModelRuntime.open`, `RuntimeModelExecutionResult`, and `RuntimeModelOpenFailureCode`. They are retained only in Git history. R27 introduces no new business object source or new public model-open failure algebra.

<a id="compiled-view-materialization"></a>
## 3. Compiler-published typed View materialization

P1 `EngineContext` currently exposes `CompiledModelSet`, whose View registry contains `CompiledDefinition(normalizedBody)`. MODEL must not interpret `NormalizedBody`, XML/YAML, legacy `ViewData`, or default `ConfigContextUtil` to recover View semantics.

COMPILER therefore publishes a neutral immutable `CompiledViewMaterializationPlan` for every View that can participate in a P2 `RuntimeBindingPlan`. It contains the exact `ViewKey` plus a canonical immutable field/relation tree (`SCALAR | OBJECT | LIST`). It is created from compiler-resolved View semantics in `FLOW-R11 STEP-P2-COMPILE-02/03` and travels with the same immutable Context candidate. The plan contains no permission decision and no raw source syntax.

MODEL lookup algorithm is unique:
1. take the exact `RuntimeBindingPlan.compiledTargetBinding().targetViewKey()`;
2. lookup exactly one `CompiledViewMaterializationPlan` in the **same captured EngineContext**;
3. never read/parse `CompiledDefinition.normalizedBody`, XML/YAML, `ViewData`, `ModelData.name`, or a default/global Context to infer identity;
4. if the compiled descriptor is absent for a P2 runtime plan, the candidate is invalid and must have been rejected before publication; runtime never repairs it.

<a id="existing-production-model-lifecycle"></a>
## 4. Existing production object lifecycle and trusted handle creation

R27 keeps the existing MODEL production behavior instead of creating an isolated `RuntimeFactValue` copy.

The P2 implementation adds a typed overload to the existing CONTEXT `ModelDataFactory`:

```text
createData(CompiledViewMaterializationPlan exactPlan, Object originObject)
```

This overload uses only the compiled plan to build the ModelData field/relation shape. For a non-Map origin object it preserves the current `ModelDataFactory.createData(name,Object)` compatibility behavior: the same object is retained as `originData` and its values initialize ModelData. For Map input, the same Map remains the values object. It does not call `ConfigContextUtil`, does not select by String name, and does not parse `NormalizedBody`.

Inside `dec-core-model`, package-private `CompiledRuntimeModelBinder` is invoked only by the existing ModelLoader/ModelContainer production integration. Its inputs are the captured Context, exact current `RuntimeBindingPlan`, and the real production origin object already being loaded by that MODEL execution. It:
- requires exact plan membership in the captured Context;
- obtains the exact compiled materialization plan by target ViewKey;
- calls the typed ModelDataFactory overload to create the actual ModelData used by the existing ModelLoader/ModelContainer;
- freezes `RuntimeModelProvenance(runtimeBindingPlan)` and `RuntimeModelHandle` around that **same ModelData reference** in the same MODEL operation;
- never accepts an already-created ModelData from STARTER/business code and exposes no public wrap/rebind/ModelData accessor.

This is identity provenance, not permission. Guard remains the only READ/WRITE authorization authority.

<a id="real-production-writeback"></a>
## 5. Real production WRITE and write-back

Protected operations use the exact ModelData inside the trusted handle that the existing production ModelLoader/ModelContainer is executing. R27 does not create a second detached model runtime.

For non-Map origin objects, successful WRITE follows the existing `ModelContainer` completion path: committed ModelData values are copied back to the same `originData` object exactly once. For Map origins, the same map is the live values object. Guard DENY, stale intent, rollback, or operation failure must leave the real production object at its pre-operation observable state and produce no success receipt. A test that only mutates an internal copy is invalid evidence.

<a id="trusted-runtime-scope"></a>
## 6. MODEL-signed runtime scope and FLOW-R11 handoff

The current production handoff is a MODEL-owned `RuntimeModelAccessScope`, created only by the active existing MODEL execution root after its trusted handles are loaded. Scope/frame/handle classes have no public/protected constructor or factory.

The MODEL execution root mints `RuntimeExecutionFrameId`, `RuntimeResolutionOwnerId`, and optional `RuntimeCollectionCursorId`; these are never inputs to a public frame request. The resulting `RuntimeModelFrame` freezes those IDs with the active handles. A caller may construct `ProtectedAccessInvocation` with typed IDs, but those values are checked against independently MODEL-minted frame facts, so equality is no longer self-asserted.

FLOW-R11 is implemented exactly:
1. STARTER receives the MODEL-created `RuntimeModelAccessScope`, reads its trusted frame, and validates every handle plan/provenance against the captured EngineContext (`STEP-P2-ACCESS-01`).
2. STARTER calls `scope.beginSession()`, registers the trusted frame handles, and seals the session (`STEP-P2-ACCESS-02`). Duplicate/cross-session ownership remains fail closed.
3. STARTER performs exact target resolution (`STEP-03`), freezes READ/WRITE intent and one-shot capability (`STEP-04`), Guards exact authority (`STEP-05`), then delegates the same resolved target/stamp to MODEL actual operation (`STEP-06`).

`ProtectedAccessRuntimeFactory.production(context).create(scope)` has no production overload accepting independent frame/owner/cursor, ModelData, handle, session, Guard, operation port, or provider. Scope inactivity/staleness fails before capability/Guard/effect.

## 7. Cross-module mapping

- CONTEXT: immutable policy/binding/value contracts plus typed materialization descriptor and typed ModelDataFactory overload.
- COMPILER: resolve View/materialization semantics once; publish descriptor with policy/binding in the same Context candidate.
- MODEL: existing production ModelData lifecycle, trusted handle/scope minting, session/locator/coordination, actual READ/WRITE and existing write-back.
- STARTER: consume MODEL scope, validate provenance, register/seal session, resolve target/intent, capability, Guard, delegate effect.

Current CMIs: `CMI-P2-COMPILE-004` and `CMI-P2-PROTECTED-ACCESS-006`. `CMI-006` maps one-to-one to FLOW-R11 STEP-01..06; trusted scope production realizes the FLOW precondition and is not a new business step.

## 8. Concurrency and P2/P7

One actual ModelData/handle has one coordination cell and at most one active session lease. Per-ModelPath lock/version and `RuntimeMutationStamp` stay bound to the same handle. Same-version competitors commit at most once. WRITE failure restores ModelData and the real origin object. `RuntimeModelAccessScope`/session are P2 technical execution seams, not P7 user/business session lifecycle.

## 9. Gate

All 20 P1 remain OPEN pending same-revision BusinessModel/Impact/API/Architecture/Develop/CrossModule/Concurrency/TestDesign Review, current risk scan and machine Evidence. Implementation Plan/TDD/Development remain BLOCKED.

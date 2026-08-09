# COMPILER P2 Detailed Design

> Revision: `DESIGN-P2-R26`; base `DESIGN-P2-R25`.
> Inputs: `REQAN-P2-R01@d08612768131` + Overlay R04 + `BM-R20` + `FLOW-R11`; parallel Impact `P2-IMPACT-R25`.
> Status: `NEEDS_REVIEW / MACHINE_BLOCKED`.

R26 preserves the R25 authority/API/effect semantics and changes only trusted materialization identity plus the MODEL -> STARTER production handoff. `BM-R20` and `FLOW-R11` retain their independent semantic PASS and are not rewritten.

## Current chain

`REQAN+Overlay -> BM-R20 -> FLOW-R11 -> DESIGN-P2-R26 -> TESTDESIGN-P2-R27`; parallel `P2-IMPACT-R25`.

<a id="trusted-runtime-model-materialization"></a>
## Trusted materialization

The trusted relationship is proved by the input contract and deterministic algorithm, not by naming an assembler. Production MODEL accepts only `RuntimeModelFrameRequest`. Each request item is an exact `RuntimeBindingPlan` plus a deep-immutable `RuntimeFactValue sourceSnapshot`. **Existing `ModelData` is not an accepted production input.**

For each item MODEL must, in order:

1. prove that the exact `RuntimeBindingPlan` is a member of the captured `EngineContext`;
2. derive target identity only from `plan.compiledTargetBinding().targetViewKey()` and the already-compiled binding kind/value;
3. resolve that exact immutable target-view definition from the **same captured EngineContext**;
4. create a **new internal `ModelData`** under that exact view using only the source snapshot as business values;
5. fail the entire open with `SOURCE_NOT_MATERIALIZABLE` if the source is incompatible with that exact view;
6. atomically create `RuntimeModelProvenance(plan)` and `RuntimeModelHandle(newModelData, provenance)`;
7. only after every item succeeds, create one `RuntimeModelFrame`, one `RuntimeModelSession`, register the new handles, seal the session, and return frame+session together.

Forbidden identity evidence: caller-supplied existing `ModelData`, `ModelData.name`, caller `ViewData`, list order, first-match iteration, raw XML/YAML, selector reparsing, or legacy `ModelDataFactory.createData(name,Object)` / default `ConfigContextUtil` lookup. Those legacy seams may remain for existing behavior but are not the P2 trusted production materializer.

Package-private `dec.core.model.runtime.RuntimeModelFrameAssembler` remains the implementation owner, but it is called only by MODEL `RuntimeModelRuntime.open(...)` with the captured Context and typed request above. It has no cross-module caller and no free-form `(binding, ModelData)` input.

## MODEL -> STARTER production handoff

The current producer seam is explicit:

```text
RuntimeModelRuntimes.production(captured EngineContext)
  -> RuntimeModelRuntime
  -> open(RuntimeModelFrameRequest)
  -> RuntimeModelExecutionResult
     success: RuntimeModelExecution(frame + sealed session)
     failure: stable RuntimeModelOpenFailure; frame/session/handle exposure = 0
```

`RuntimeModelSession.register/seal` are MODEL-internal and are not public cross-module mutation APIs.

`ProtectedAccessRuntimeFactory.production(the same captured EngineContext).create(frameRequest)` internally obtains that MODEL production runtime, calls `open`, and retains the **exact returned `RuntimeModelExecution`**. It then performs request/frame scope equality, exact 0/1/N target resolution, READ access or WRITE-intent+`RuntimeMutationStamp` freeze, one-shot capability, Guard of the exact `ModelAccessRuleKey`, and finally the MODEL-owned READ/rollback-safe WRITE.

No production overload may accept an injected `RuntimeModelRuntime`, `RuntimeModelSession`, `RuntimeModelFrame`, `RuntimeModelOperationPort`, Guard, existing `ModelData`, or independently supplied frame/owner/cursor authority. `ProtectedAccessComposition.close()` closes the same MODEL execution/session returned by `open`.

## Wrong-target / wrong-source semantics

- plan A + existing ModelData B: not expressible because existing ModelData is not an input;
- plan A + values originating from another business object: MODEL still creates only an A-view ModelData using A's exact captured view, or fails `SOURCE_NOT_MATERIALIZABLE`;
- stale/non-member plan: `PLAN_NOT_IN_CAPTURED_CONTEXT`, with no frame/session/effect;
- later plan A + handle B target resolution: provenance mismatch/not-found before capability/Guard/effect;
- provenance never grants permission; Guard remains the sole permission decision for the exact `ModelAccessRuleKey`.

## Cross-module / effect closure

Current CMIs are `CMI-P2-COMPILE-004` and `CMI-P2-PROTECTED-ACCESS-005`. STARTER orchestrates/resolves/freezes capability and Guards. MODEL owns trusted materialization, frame/session/locator/coordination and actual effect. CONTEXT remains neutral. `FLOW-R11 STEP-P2-ACCESS-06` remains MODEL-owned.

## Atomicity and concurrency

MODEL `open` is all-or-nothing. One actual MODEL-created handle has one coordination cell and at most one active session lease. Per-path version/lock and `RuntimeMutationStamp` bind the same resolved handle. Same-version competitors commit at most once; the stale loser mutates zero. WRITE failure rolls back/restores observable state, produces no receipt, and leaves the capability consumed.

## Gate

All 20 P1 findings remain OPEN pending same-revision specialist Review, current risk scan and required machine Evidence. Implementation Plan / TDD / Development remain BLOCKED. No production Java or execution Evidence is claimed.

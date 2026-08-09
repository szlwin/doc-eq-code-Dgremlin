# COMPILER P2 Test Seams

> Revision `DESIGN-P2-R26`; inputs `BM-R20 / FLOW-R11`; Impact `P2-IMPACT-R25`. Status `NEEDS_REVIEW / MACHINE_BLOCKED`.

## MODEL trusted materialization seam

Owner-module fixture: `dec-core-model` / `RuntimeModelMaterializationIntegrationTest`.

The fixture must call the public production seam `RuntimeModelRuntimes.production(capturedEngineContext).open(RuntimeModelFrameRequest)` and assert:

- request input is exact `RuntimeBindingPlan + RuntimeFactValue sourceSnapshot`; no existing `ModelData` parameter exists;
- plan membership is checked against the captured Context before materialization;
- target view is selected only by `CompiledTargetBinding.targetViewKey()` in that same Context;
- MODEL creates a new internal ModelData under that exact view and freezes `RuntimeModelProvenance(plan)` + handle in the same materialization operation;
- incompatible source -> `SOURCE_NOT_MATERIALIZABLE`; non-member plan -> `PLAN_NOT_IN_CAPTURED_CONTEXT`; missing target view -> `TARGET_VIEW_NOT_FOUND`;
- any input failure exposes zero frame/session/handle; success returns one `RuntimeModelExecution(frame + sealed session)`;
- public/reflection surface has no `wrapExisting(ModelData,...)`, rebind, public ModelData accessor, or public session register/seal;
- `ModelData.name`, caller ViewData, list order, raw definitions, selector reparsing and legacy default `ConfigContextUtil` lookup are never identity authority.

Positive oracle: plan A + compatible source -> new A-view ModelData internal to trusted handle A. Negative oracle: existing ModelData B cannot be supplied; plan A cannot materialize using B identity; stale/non-member plan or incompatible source fails before handoff.

## MODEL -> STARTER handoff seam

Owner-module fixture: `dec-core-starter` / `ProtectedAccessProductionCompositionTest`.

`ProtectedAccessRuntimeFactory.production(exactEngineContext).create(frameRequest)` must internally obtain MODEL production runtime bound to the same Context, call `open`, retain the exact returned frame+session execution pair, and close that same execution with composition close. MODEL-open failure yields no `ProtectedAccessPort`, capability, Guard call or model effect.

No production overload may inject `RuntimeModelRuntime`, `RuntimeModelSession`, `RuntimeModelFrame`, `RuntimeModelOperationPort`, Guard, existing ModelData, or separate frame/owner/cursor authority.

## Existing owner-module API seams

- CONTEXT: `ProtectedAccessContextApiContractTest` checks neutral public authority/value/result types only.
- MODEL: `ProtectedAccessModelApiContractTest` checks current materialization/runtime/execution/frame/session/locator public signatures and constructor restrictions.
- STARTER: `ProtectedAccessStarterApiContractTest` checks bootstrap/resolver/entry APIs and legal CONTEXT+MODEL consumption.
- A compile/setup/missing-class failure before the intended assertion is `INVALID_RED`, never valid RED.

## Runtime/effect seams

Resolver exact-matches plan+trusted provenance in the retained execution; 0/1/N is deterministic. READ mutates zero. WRITE freezes the same session/object/path/version, Guard precedes effect, successful mutation commits once, failure restores observable state and produces no receipt. Concurrency uses latch/barrier, not sleep.

## TestDesign self-containment

`TESTDESIGN-P2-R27` contains every blocking Case ID with a current-revision fixture/action/expected/forbidden/ref oracle and exact registry coordinates. It contains no literal truncation marker `...` or unicode ellipsis. Tests must not consult R26/R25 for missing expected behavior.

## Gate

Risk scan, same-revision specialist Review and required machine/Test Evidence remain required. No TDD execution is claimed.

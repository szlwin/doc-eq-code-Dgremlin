# COMPILER P2 Architecture

> Revision: `DESIGN-P2-R26`
> Inputs: `BM-R20 / FLOW-R11`; parallel Impact `P2-IMPACT-R25`
> Status: `NEEDS_REVIEW / MACHINE_BLOCKED`

## Compile/publication

`dec-core-compiler -> dec-core-context`: compiler resolves P1 selector once and publishes neutral `CompiledTargetBinding/RuntimeBindingPlan` into an immutable Context candidate; compiler coordinates atomic publication. Runtime does not reparse selector syntax and P2 does not introduce a global/default current Context.

## Trusted materialization boundary

The trusted association is established by input contract and algorithm, not by naming an assembler trusted.

```text
captured EngineContext
  + RuntimeModelFrameRequest
      frame/owner/cursor
      each input = exact RuntimeBindingPlan + deep-immutable RuntimeFactValue source snapshot
            |
            v
MODEL RuntimeModelRuntime.open(request)
            |
            +-- verify plan is exact member of captured Context
            +-- derive target identity only from CompiledTargetBinding.targetViewKey
            +-- resolve exact captured-context view definition
            +-- create NEW ModelData under that exact view definition
            +-- atomically freeze RuntimeModelProvenance(plan) + handle
            +-- after all inputs succeed, create frame + sealed session
            v
RuntimeModelExecution(frame + session)
```

Existing `ModelData` is never a trusted-path input. Legacy `ModelDataFactory.createData(name, object)` and default `ConfigContextUtil` lookup are outside the P2 trusted path because a name/default lookup does not prove exact membership in the captured P2 Context.

## MODEL -> STARTER production handoff

`dec-core-model` exposes `RuntimeModelRuntimes.production(capturedEngineContext) -> RuntimeModelRuntime`. `RuntimeModelRuntime.open(frameRequest)` returns one `RuntimeModelExecution` containing the exact trusted frame and sealed session created from the same request and Context.

`dec-core-starter` `ProtectedAccessRuntimeFactory.production(engineContext)` internally obtains that MODEL production runtime; its public `create` accepts only `RuntimeModelFrameRequest`. It does not accept injected MODEL runtime/session/operation port/Guard, existing ModelData, or an independently constructed frame. The composition retains the returned MODEL execution until close.

## Protected access and effect owner

STARTER owns composition orchestration, exact target resolution, intent, one-shot capability and Guard. MODEL owns trusted materialization, frame/session/locator/coordination and the actual READ/WRITE effect. `FLOW-R11 STEP-P2-ACCESS-06` remains MODEL-owned. `RuntimeModelOperationPort` remains a CONTEXT neutral contract implemented by MODEL and wired internally by STARTER.

## Dependencies

```text
compiler -> context        allowed
model    -> context        allowed P2 neutral contracts and captured Context consumption
starter  -> context+model  allowed production composition
context  -> compiler/model/starter forbidden
model    -> starter        forbidden
P3/P4/P6 core -> context   allowed
P3/P4/P6 core -> starter   forbidden
```

## API verification boundary

CONTEXT, MODEL and STARTER each own their API contract test. MODEL additionally owns `RuntimeModelMaterializationIntegrationTest`, which validates captured-context membership, no existing-ModelData input, exact target-view materialization, all-or-nothing frame/session creation and legacy default-context exclusion. STARTER owns the production handoff/reachability tests.

## Current cross-module projection

`P2-IMPACT-R25` with `CMI-P2-COMPILE-004` and `CMI-P2-PROTECTED-ACCESS-005`.

## Gate

No production Java, TDD execution or risk Evidence is claimed. Same-revision specialist Review and machine closure remain required.

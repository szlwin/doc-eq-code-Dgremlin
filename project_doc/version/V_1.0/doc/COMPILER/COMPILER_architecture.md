# COMPILER P2 Architecture

> Revision: `DESIGN-P2-R25`
> Inputs: `BM-R20 / FLOW-R11`; parallel Impact `P2-IMPACT-R24`
> Status: `NEEDS_REVIEW / MACHINE_BLOCKED`

## Compile/publication

`dec-core-compiler -> dec-core-context`: compiler resolves P1 selector once and publishes neutral `CompiledTargetBinding/RuntimeBindingPlan` into an immutable Context candidate; compiler coordinates atomic publication. No runtime selector reparse and no global/default Context.

## Trusted runtime-model provenance

```text
dec-core-model trusted internal materialization
  creates/loads actual ModelData
  + freezes exact TargetKey/CompiledTargetBinding provenance in same operation
        v
RuntimeModelHandle (public read-only; no public constructor/wrap/rebind; no ModelData accessor)
        v
RuntimeModelFrame (frame/owner/cursor + handles; no public constructor/rebind)
        v
STARTER RuntimeExecutionFrameSnapshot.from(trustedFrame)
        v
validate handle provenance against captured EngineContext
        v
MODEL RuntimeModelSession.register(handle) / seal
```

The public architecture has no `binding + arbitrary ModelData` association API. Handle substitution cannot relabel provenance; wrong-provenance handles fail before capability/Guard/effect.

## Protected access and effect owner

STARTER owns composition, exact target resolution, intent, one-shot capability, Gateway/Guard. MODEL owns session/locator/coordination and the actual READ/WRITE effect. `FLOW-R11 STEP-P2-ACCESS-06` is MODEL-owned. `RuntimeModelOperationPort` is a CONTEXT neutral contract implemented by MODEL and wired by STARTER.

## Dependencies

```text
compiler -> context        allowed
model    -> context        planned/allowed P2 neutral contracts
starter  -> context+model  allowed production composition
context  -> compiler/model/starter forbidden
model    -> starter        forbidden
P3/P4/P6 core -> context   allowed
P3/P4/P6 core -> starter   forbidden
```

## API verification boundary

CONTEXT, MODEL and STARTER each own their API contract test. A context test never adds reverse dependencies solely to inspect model/starter. STARTER is the legal cross-module consumer test surface for context+model public contracts.

## Current cross-module projection

`P2-IMPACT-R24` with `CMI-P2-COMPILE-004` and `CMI-P2-PROTECTED-ACCESS-004`. No current `-003` CMI reference.

## Gate

No production Java/TDD/risk Evidence is claimed. Same-revision specialist Review and machine closure remain required.

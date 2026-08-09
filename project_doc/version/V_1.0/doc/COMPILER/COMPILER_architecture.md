# COMPILER P2 Architecture

> Revision `DESIGN-P2-R28`; inputs `BM-R20 / FLOW-R11`; parallel `P2-IMPACT-R27`; status `NEEDS_REVIEW / MACHINE_BLOCKED`.

## Context publication boundary

`CompiledViewMaterializationIndex` is a constructor-owned member of `CompiledModelSet`, not side state on `EngineContext`. The compiler publishes one aggregate containing definitions + typed materialization index + deferred/diagnostics/digest/version facts. Aggregate equality/hash and semantic digest include the index; `EngineContext.viewMaterializationIndex()` delegates to the same `CompiledModelSet`. Missing/duplicate required descriptors block compile/publication; runtime repair is forbidden.

## Existing production MODEL integration

```text
captured EngineContext + existing MODEL Container
        -> RuntimeModelExecutionRoot.production(...)
real origin object + exact RuntimeBindingPlan + explicit rule/connection
        -> root.load(request)
        -> captured Context index lookup
        -> typed ModelDataFactory
        -> internal ModelLoader.load(rule, same ModelData, connection)
        -> owned Container.load(loader)
        -> same ModelData trusted handle
        -> root.accessScope()
        -> MODEL-minted RuntimeModelAccessScope/frame/owner/cursor
        -> STARTER
```

The root is the only current production seam that connects origin object + plan + captured Context to the existing ModelLoader/Container lifecycle. STARTER/business code cannot inject an existing ModelData into the trusted association. No thread-local/global/default Context/plan/scope fallback is legal.

## FLOW-R11 composition boundary

STARTER consumes only the scope returned by MODEL root. STEP-01 validates scope/frame provenance. STEP-02 begins one MODEL session, registers each trusted handle once and seals. Setup failures are closed enums/result types, not implementation-chosen null/unchecked behavior. STEP-03..06 remain exact resolution -> access/capability -> Guard -> MODEL effect.

## Dependency direction

`compiler -> context`, `model -> context`, `starter -> context+model` only; reverse dependencies remain forbidden. `RuntimeModelExecutionRoot` and session failure types are MODEL-owned; composition result/failure types are STARTER-owned.

## Explicit non-goal

The user explicitly excluded changing or testing restoration of a POJO/Map already copied by legacy `ModelContainer` before a later commit failure. Existing successful write-back remains production reachability; this remediation adds no post-copy rollback mechanism.

## Gate

No production Java/TDD/risk Evidence is claimed. Same-revision specialist Review and machine closure remain required.

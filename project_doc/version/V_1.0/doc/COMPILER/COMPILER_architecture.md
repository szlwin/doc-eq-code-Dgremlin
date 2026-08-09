# COMPILER P2 Architecture

> Revision `DESIGN-P2-R29`; inputs `BM-R20 / FLOW-R11`; parallel `P2-IMPACT-R28`; status `NEEDS_REVIEW / MACHINE_BLOCKED`.

## Context aggregate

COMPILER publishes `RuntimeBindingPlan + ModelAccessPolicyIndex + CompiledViewMaterializationIndex` in one immutable `CompiledModelSet`. `EngineContext` only delegates to that aggregate. Materialization index equality/hash/digest/publication are atomic; MODEL never rebuilds materialization from raw/normalized configuration or default/global Context.

## Trusted production invocation

```text
one active MODEL production invocation
  -> package-private invocation assembler atomically captures
       exact RuntimeBindingPlan
       + real origin object
       + explicit rule/connection
       + owning root identity
  -> opaque RuntimeModelProductionInvocation (no public factory/rebind)
  -> RuntimeModelExecutionRoot.load(token)
  -> typed ModelDataFactory
  -> existing 3-arg ModelLoader
  -> root-owned production Container created internally by ContainerFactory
  -> same ModelData trusted handle
```

The former public `RuntimeModelLoadRequest.of(plan, originObject, ...)` is superseded. Plan A + arbitrary Object B is not a public production expression. A token is one-shot and root-bound. Cross-root token use and reuse fail with stable codes before ModelData creation.

## Production Container trust

`RuntimeModelExecutionRoots.production(context, ProductionContainerKind)` selects the existing production Container internally. No public production overload accepts `Container`. Unit tests may use MODEL-internal test seams, but AC-007 production Evidence must traverse `ContainerFactory` and a supported production kind; fake/test Container is invalid production Evidence.

## STARTER-to-MODEL effect seam

```text
MODEL scope
  -> STARTER validates frame
  -> begin/register/seal exact MODEL session
  -> scope.effectProvider().bind(the same sealed session)
  -> private RuntimeModelOperationPort retained inside composition
  -> resolve exact registered handle/object
  -> freeze intent/capability
  -> Guard
  -> ALLOW only -> private bound MODEL operation port
```

The provider is bound to the same root/scope/session/handle set. The port validates session/object membership again before touching ModelData. `ProtectedAccessComposition` and Rule/Change/CustomAction entries expose no port/provider. Production dependency checks forbid business consumers from importing MODEL runtime effect types; they consume STARTER entries only.

## FLOW-R11 mapping

Trusted production invocation/root load realizes the existing FLOW precondition “MODEL-owned trusted RuntimeModelFrame available.” FLOW STEP-01 validates that frame; STEP-02 creates/registers/seals the session and binds the MODEL effect provider; STEP-03 resolves the exact target; STEP-04 freezes access/capability; STEP-05 Guards; STEP-06 invokes the privately bound MODEL operation port on the same registered object.

## Compatibility / scope

Existing successful originData write-back remains the production destination. Per user directive, post-copy POJO/Map restoration after a later legacy commit failure is not changed or required. No new business authority, operation kind, session concept, or P7 lifecycle is introduced.

## Dependency direction

`compiler -> context`, `model -> context`, `starter -> context+model` are allowed. `context -> model/starter`, `model -> starter`, and business/consumer-core -> MODEL runtime effect contracts are forbidden.

No production Java/TDD/risk Evidence is claimed.

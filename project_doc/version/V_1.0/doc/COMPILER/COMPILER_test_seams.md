# COMPILER P2 Test Seams

> Revision `DESIGN-P2-R29`; inputs `BM-R20 / FLOW-R11`; Impact `P2-IMPACT-R28`; status `NEEDS_REVIEW / MACHINE_BLOCKED`.

## Plan/origin same-invocation seam

MODEL-package fixture drives the package-private production invocation assembler from one active production invocation A. Assert token A has no public/protected constructor/factory/rebind; root A loads A successfully; root B loading token A yields `INVOCATION_ROOT_MISMATCH`; root A reusing A yields `INVOCATION_ALREADY_CONSUMED`. Public API inspection must show no `of(plan, originObject)` trusted request. A Plan A/Object B substitution cannot be expressed through public production API.

## Production Container seam

Public root creation accepts `ProductionContainerKind`, not `Container`. Instrument existing `ContainerFactory` to prove production COMMIT/SYNCHRONIZED selection creates the real supported container and trusted load calls that exact root-owned container. A fake/test Container may be used only by MODEL unit harness; AC-007 real-production tests fail if a fake/injected Container is used.

## MODEL effect provider seam

After STARTER validates scope and seals the exact session, composition must call `scope.effectProvider().bind(theSameSession)` once and privately retain the returned operation port. Guard DENY yields operation-port call count zero. Guard ALLOW READ/WRITE invokes that bound port once with a resolved target whose session/object belong to the same registered handle. Cross-session port binding or substituted object fails before model mutation with the stable code/denial.

## Consumer no-bypass seam

Compile-time dependency scan proves Rule/Change/CustomAction production consumers import STARTER entries and CONTEXT values only; they do not import `RuntimeModelAccessScope`, `RuntimeModelEffectProvider`, or `RuntimeModelOperationPort`. `ProtectedAccessComposition` exposes no provider/port getter, and no production factory overload accepts an injected port or Guard.

## Context/materialization and API seams

Retain R28 checks: `CompiledViewMaterializationIndex` is a `CompiledModelSet` aggregate member in equality/hash/digest/publication; MODEL reads only captured exact View descriptor; all public construction factories compile in their legal owner modules; superseded R26 fresh-snapshot/open API remains absent.

## Transaction/write-back scope

Successful Guard-allowed WRITE must reach the same production ModelData and existing successful originData write-back. MODEL mutation failure before successful production completion returns no success receipt. Per user directive, do not require restoration of a POJO/Map already copied before a later legacy commit failure.

## TestDesign quality gate

R30 must contain explicit Fixture/Action/Expected/Forbidden/Ref for every blocking Case. Template phrases such as “named behavior/case remains...” are forbidden. Each Expected must state the observable success/failure outcome specific to its Case. Target RED never uses `-am`; pre-assert compile/setup/missing class is `INVALID_RED`.

Risk scan, same-revision specialist Review and machine Evidence remain required before TDD.

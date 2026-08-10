# COMPILER P2 Test Seams

> Revision `DESIGN-P2-R30`; TestDesign target `TESTDESIGN-P2-R31`.

## Owner-module seams

- COMPILER: exact binding/materialization publication and deterministic diagnostics.
- CONTEXT: `CompiledModelSet` aggregate, typed materialization, neutral value/result contracts.
- MODEL: direct `RuntimeModelLoadRequest` validation/loading, same-ModelData identity, production Container factory boundary, Scope/Session/EffectProvider/OperationPort.
- STARTER: composition/session/effect binding, resolver/capability/Guard sequencing and representative consumers.

## Mandatory direct-load seams

1. `RuntimeModelLoadRequest.of(plan, origin, rule, connection)` is constructible as data but provides no READ/WRITE authority.
2. Production fixture proves MODEL lifecycle constructs the request and sends it to root; business/application/STARTER production code does not use root load directly.
3. Invalid plan in captured Context fails `PLAN_NOT_IN_CAPTURED_CONTEXT` before ModelData creation/Container/scope.
4. Exact descriptor is selected only through captured `CompiledViewMaterializationIndex`.
5. Created ModelData A is the exact reference passed to ModelLoader, root-owned Container, Handle, Session and Guard-allowed effect.
6. Production root accepts `ProductionContainerKind`, never a caller fake/custom Container.
7. Token classes/failures from R29 are absent from current API/TestDesign authority and are marked deferred only in changelog/design history.

## Protected-effect seams preserved

- provider binds only the exact sealed session from the same scope;
- operation port is private to STARTER composition;
- Guard DENY -> port/effect count zero;
- A->B target substitution fails before effect;
- Rule/Change/CustomAction have no MODEL-effect bypass.

## TDD validity

Each blocking Case maps to one exact owner-module TestClass. Bootstrap may use `-am`; target RED must not. Compile/setup/missing-class failure before intended assertion is `INVALID_RED`.

## Exclusion

Do not turn legacy post-copy POJO/Map rollback after later commit failure into a blocking P2 oracle. Successful originData write-back remains required.

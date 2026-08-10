# COMPILER P2 Architecture

> Revision `DESIGN-P2-R30`; base `DESIGN-P2-R29`; Impact `P2-IMPACT-R29`.
> BM-R20 and FLOW-R11 are unchanged.

## Architecture decision

P2 drops the opaque production-invocation credential. `RuntimeModelLoadRequest` is restored as a simple MODEL loading DTO. The trust boundary is the existing MODEL production lifecycle; the trusted cross-module runtime-object boundary begins only after MODEL has validated/loaded the request and minted `RuntimeModelHandle/RuntimeModelAccessScope`.

```text
compiler -> immutable Context/materialization
                    |
                    v
MODEL production lifecycle
  -> RuntimeModelLoadRequest(plan, real origin, rule, connection)
  -> RuntimeModelExecutionRoot.load(request)
  -> exact captured Context validation
  -> typed ModelDataFactory
  -> existing 3-arg ModelLoader
  -> MODEL ContainerFactory-created Container
  -> SAME ModelData -> Handle -> Scope
                    |
                    v
STARTER begin/register/seal + bind scope EffectProvider
  -> exact resolver -> capability -> Guard
  -> private same-session RuntimeModelOperationPort
  -> SAME handle/ModelData effect
```

## Trust boundaries

1. `RuntimeModelLoadRequest` is data, not authority. It does not grant READ/WRITE, scope, handle or operation capability.
2. MODEL production code is the trusted producer/consumer boundary for the request. P2 intentionally does not protect against malicious/incorrect plan+origin composition by already-trusted MODEL production implementation code.
3. Application/business/Rule/Change/CustomAction/STARTER must not use root loading as a production entry or substitute trusted ModelData/Container/Scope/effect provider.
4. Production Container is created by MODEL through existing `ContainerFactory` from `ProductionContainerKind`; fake/custom Container is not production evidence.
5. Trusted cross-module identity is the MODEL-minted Handle/Scope, not the request.

## Same-target invariant

```text
ModelData A
 -> Handle A
 -> Session S registers A as RuntimeObjectId A
 -> Resolver returns A
 -> Guard evaluates A + exact ModelAccessRuleKey
 -> bound MODEL port revalidates S/A
 -> effect A
```

Any A->B substitution after resolution fails before protected effect and produces no success value/receipt.

## FLOW-R11 mapping

Request loading is precondition establishment, not a new business flow. FLOW-R11 stays:

1. trusted MODEL frame available;
2. STARTER session register/seal + effect binding;
3. exact target resolve;
4. READ/WRITE intent + capability;
5. Guard;
6. same MODEL object effect.

## Dependency direction

`compiler -> context`, `model -> context`, `starter -> context + model`, business consumers -> `starter + context`. Business consumers must not import `RuntimeModelExecutionRoot`, `ModelData`, `RuntimeModelEffectProvider`, or `RuntimeModelOperationPort`.

## Deferred design

`RuntimeModelProductionInvocation` and all root-bound/replay credential semantics are `NOT_ADOPTED_IN_P2 / DEFERRED`. They may be reconsidered only in a future revision; they are not compatibility/current API obligations.

## Scope exclusion

No change is made to legacy POJO/Map post-copy rollback after a later commit failure. Successful existing originData write-back and Guard-before-effect remain required.

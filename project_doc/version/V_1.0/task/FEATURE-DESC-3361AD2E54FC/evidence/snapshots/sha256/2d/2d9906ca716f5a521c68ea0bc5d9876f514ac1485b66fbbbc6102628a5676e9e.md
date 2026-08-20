# COMPILER P2 Detailed Design

> Revision `DESIGN-P2-R30`; base `DESIGN-P2-R29`.
> Inputs: REQAN-P2-R01 + Overlay R04 + BM-R20 + FLOW-R11; Impact `P2-IMPACT-R29`.

## D1. Current loading seam

Current production loading is exactly:

```text
MODEL production lifecycle
 -> RuntimeModelLoadRequest.of(exactPlan, realOrigin, ruleName, connectionName)
 -> RuntimeModelExecutionRoot.production(capturedContext, ProductionContainerKind)
 -> root.load(request)
```

The request is a DTO, not a permission credential. Guard remains the sole READ/WRITE authority.

## D2. Root load algorithm

- L01 closed root -> `EXECUTION_CLOSED`, no ModelData, no scope.
- L02 request plan must be an exact member of captured EngineContext -> else `PLAN_NOT_IN_CAPTURED_CONTEXT`.
- L03 exact target ViewKey must resolve exactly one `CompiledViewMaterializationPlan` from captured aggregate -> else `MATERIALIZATION_DESCRIPTOR_NOT_FOUND`.
- L04 typed `ModelDataFactory.createData(plan, originObject)` -> else `ORIGIN_NOT_MATERIALIZABLE`.
- L05 use existing three-argument `ModelLoader.load(ruleName, modelData, connectionName)` only.
- L06 root-owned production Container from MODEL `ContainerFactory` must accept loader -> else `CONTAINER_LOAD_REJECTED`.
- L07 freeze the same created+loaded ModelData reference into `RuntimeModelHandle`; no A-load/B-handle substitution.

No runtime config reparse/default Context, existing ModelData trusted injection, caller Container, two-argument default-connection load, or detached model runtime is legal.

## D3. Production trust boundary

P2 intentionally treats the existing MODEL production lifecycle as trusted for forming `plan + originObject`. The public constructibility of `RuntimeModelLoadRequest` does not make it an authority token. Production architecture must not expose a business/application/STARTER path that directly calls root loading to decide trusted ModelData.

The following previous R29 objects are not current:

```text
RuntimeModelProductionInvocation
RuntimeProductionInvocationId
RuntimeModelProductionInvocationAssembler
INVOCATION_ROOT_MISMATCH
INVOCATION_ALREADY_CONSUMED
```

Status: `NOT_ADOPTED_IN_P2 / DEFERRED`.

## D4. Trusted runtime boundary after load

After successful L07:

```text
same ModelData A
 -> RuntimeModelHandle A
 -> RuntimeModelAccessScope(frame + handles + effectProvider)
 -> RuntimeModelSession S
 -> register A -> runtimeObjectId A
 -> seal S
 -> effectProvider.bind(S)
```

`Scope`, `Frame`, `Handle`, provider and returned operation port remain MODEL-minted/non-caller-injectable.

## D5. Protected effect

STARTER composition privately retains only the port returned by the same scope provider bound to the same sealed session. Resolver produces a target for one registered runtimeObjectId; capability freezes that target/path/version; Guard evaluates the same proof. STEP-06 port rechecks `sessionId + runtimeObjectId + registered handle` before touching ModelData.

Guarantee: `resolve A -> Guard A -> effect A`; A->B substitution gives a stable denial/failure and effect count zero.

## D6. Production Container

Public root creation accepts only `ProductionContainerKind`. MODEL creates the supported existing Container internally through `ContainerFactory`. `production(context, Container)`, caller ModelData, caller provider/operation port and caller Guard overloads are forbidden. Fake/mock Container cannot satisfy AC-007 production reachability evidence.

## D7. Failure algebra

Current load failures are exactly:

```text
EXECUTION_CLOSED
PLAN_NOT_IN_CAPTURED_CONTEXT
MATERIALIZATION_DESCRIPTOR_NOT_FOUND
ORIGIN_NOT_MATERIALIZABLE
CONTAINER_LOAD_REJECTED
```

Session/effect/composition failure enums from R29 remain unchanged. Every pre-scope load failure yields handle=0, scope=0, STARTER composition=0, Guard=0, protected effect=0.

## D8. Consumer dependency and bypass

Rule/Change/CustomAction use STARTER entry interfaces. Application/business consumers do not call `RuntimeModelExecutionRoot.load`, do not accept/replace trusted ModelData/Container and do not import MODEL effect provider/operation port. Architecture tests enforce this dependency boundary.

## D9. Scope exclusion

No P2 requirement is added to restore a POJO/Map already copied before a later legacy commit failure. Guard DENY/pre-effect failure still means no protected WRITE; normal success must still reach existing originData write-back.

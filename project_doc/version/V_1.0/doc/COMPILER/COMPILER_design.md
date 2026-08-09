# COMPILER P2 Detailed Design

> `DESIGN-P2-R29`; base R28; authoritative inputs `REQAN-P2-R01@d08612768131 + Overlay R04 + BM-R20 + FLOW-R11`; parallel `P2-IMPACT-R28`.
> Status `NEEDS_REVIEW / MACHINE_BLOCKED`.

R29 preserves BM-R20 and FLOW-R11. It only closes implementation seams identified by the independent Review: actual MODEL effect binding, Plan/origin same-invocation provenance, production Container trust, and TestDesign oracle specificity. The user-confirmed legacy post-copy POJO/Map rollback exclusion remains unchanged.

## 1. Preserved business semantics

Shared-View `TargetKey`, independent owner System, exact `ModelPath`, READ/WRITE-only, `ModelAccessRuleKey` sole authority, 0/1/N fail-closed target/intent, one-shot capability, Guard before effect, actual ModelData coordination, mutation stamp and P2/P7 boundary are unchanged.

<a id="compiled-view-materialization"></a>
## 2. Compiled materialization remains aggregate-owned

`CompiledViewMaterializationIndex` remains a mandatory `CompiledModelSet` member and is included in equals/hash/semantic digest/atomic publication. MODEL reads only the captured Context aggregate by exact target ViewKey. Missing/duplicate descriptors are compile/publication errors; runtime repair or raw/normalized configuration reinterpretation is forbidden.

<a id="trusted-production-invocation"></a>
## 3. Plan + real origin provenance is a single MODEL invocation token

R28's public `RuntimeModelLoadRequest.of(rule, connection, plan, originObject)` is superseded. R29 introduces `RuntimeModelProductionInvocation`, which is read-only and has no public/protected constructor/factory/rebind.

Package-private `dec.core.model.runtime.RuntimeModelProductionInvocationAssembler` is called only from the MODEL production adapter at the point one active production invocation already owns all four facts: exact compiler-selected `RuntimeBindingPlan`, the real business origin object, explicit rule name, and explicit connection name. The assembler mints one opaque `RuntimeProductionInvocationId`, captures those facts atomically, and binds the token to the receiving `RuntimeModelExecutionRoot` identity. No setter or public extraction/replacement of originObject exists.

`RuntimeModelExecutionRoot.load(token)` verifies root identity and one-shot consumption before reading the token. Therefore:
- token A on root A with its captured Plan A/Object A may load;
- token A on root B fails `INVOCATION_ROOT_MISMATCH` before ModelData creation;
- reused token A fails `INVOCATION_ALREADY_CONSUMED`;
- ordinary business/STARTER code cannot express “valid Plan A + arbitrary Object B” through current public API.

This is identity provenance, not permission; Guard remains the sole runtime authorization authority.

<a id="production-container-trust"></a>
## 4. Production Container is MODEL-created, not caller-injected

`RuntimeModelExecutionRoots.production(capturedContext, ProductionContainerKind)` is the only production root factory. `ProductionContainerKind.COMMIT/SYNCHRONIZED` maps inside MODEL to the existing `ContainerFactory`; unsupported/null selection fails root creation. There is no public production overload accepting `Container`.

The root owns the resulting Container for its lifetime. Trusted load always creates typed ModelData from the captured descriptor/origin, constructs an internal `ModelLoader`, calls the three-argument `load(ruleName, modelData, connectionName)`, then `ownedContainer.load(loader)`, and freezes the same ModelData reference in the handle. The two-argument default-connection `ModelLoader.load` is forbidden on the trusted P2 path.

Fake/test Containers are allowed only through MODEL-internal unit seams and cannot satisfy AC-007 production reachability Evidence.

<a id="model-effect-provider"></a>
## 5. MODEL actual effect provider bound to same scope/session/handle

`RuntimeModelAccessScope.effectProvider()` returns a MODEL-owned provider associated with exactly the same root/frame/handles as the scope. STARTER composition performs:
1. validate scope/frame provenance against captured Context;
2. begin one session, register every frame handle exactly once, seal once;
3. call `effectProvider.bind(theSameSealedSession)`;
4. retain the returned `RuntimeModelOperationPort` privately if and only if binding succeeds.

Binding rejects inactive scope, unsealed/closed session, or session from another scope. A composition is not created on binding failure.

At invocation time, resolver and intent freeze a `ResolvedRuntimeTarget` whose `sessionId/runtimeObjectId` name one object registered in that same session. Guard ALLOW is required before the private port is invoked. The MODEL port revalidates session/object membership and uses the same registered handle/ModelData. `RuntimeModelOperationException` maps to existing fail-closed `DenialCode`; no success result is fabricated.

No business/application API, Rule/Change/CustomAction entry, or `ProtectedAccessComposition` getter exposes the provider or operation port. Architecture rules permit MODEL runtime effect imports only from STARTER production composition code (plus MODEL/tests); representative consumers use STARTER entry interfaces.

<a id="flow-r11"></a>
## 6. FLOW-R11 remains literal

MODEL root load is precondition establishment, not a new Flow step. FLOW STEP-01 validates trusted frame; STEP-02 begins/registers/seals and binds the effect provider; STEP-03 resolves; STEP-04 freezes access/capability; STEP-05 Guards; STEP-06 invokes the private MODEL port on the same target resolved at STEP-03.

<a id="failure-algebra"></a>
## 7. Stable setup/effect failures

Existing R28 scope/session/composition failure algebra remains. R29 adds stable invocation/effect binding failures: `INVOCATION_ROOT_MISMATCH`, `INVOCATION_ALREADY_CONSUMED`, and effect binding `SCOPE_INACTIVE / SESSION_NOT_SEALED / SESSION_CLOSED / SESSION_SCOPE_MISMATCH`. All setup failures occur before capability/Guard/effect and expose no composition. MODEL operation scope/object mismatch fails before mutation.

## 8. Explicit user scope exclusion

Normal successful existing originData write-back remains required. Restoration of a POJO/Map already copied before a later legacy commit failure is explicitly outside this remediation and is not a blocker/oracle.

## 9. Gate

All 20 existing P1 findings remain OPEN pending same-revision specialist Review, risk scan and machine Evidence. No FND-021 is added. Implementation Plan/TDD/Development remain BLOCKED.

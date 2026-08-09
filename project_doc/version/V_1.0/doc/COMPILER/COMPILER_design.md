# COMPILER P2 Detailed Design

> `DESIGN-P2-R28`; base R27; authoritative inputs `REQAN-P2-R01@d08612768131 + Overlay R04 + BM-R20 + FLOW-R11`; parallel `P2-IMPACT-R27`.
> Status `NEEDS_REVIEW / MACHINE_BLOCKED`.

BM-R20 and FLOW-R11 remain unchanged. R28 only closes implementation seams identified by independent Review. The explicit user directive removes legacy post-copy POJO/Map rollback from this remediation scope.

<a id="compiled-view-materialization"></a>
## 1. Context publication aggregate

The compiler builds `CompiledViewMaterializationIndex` from already-resolved View semantics. It is a required `CompiledModelSet` constructor member. `CompiledModelSet.equals/hashCode` and semantic-digest input include its canonical representation. `EngineContext` owns only the `CompiledModelSet`; `viewMaterializationIndex()` delegates to it. Every dynamic P2 plan target View must have exactly one descriptor before candidate construction/publication. Missing/duplicate descriptors are compile/publication errors and runtime never repairs them.

This closes the previous gap where Design described an index that no captured `EngineContext` could actually expose.

<a id="model-production-root"></a>
## 2. Exact MODEL production integration

R28 freezes one integration path instead of leaving `CompiledRuntimeModelBinder` placement ambiguous:

1. MODEL creates `RuntimeModelExecutionRoot.production(capturedEngineContext, ownedContainer)`; both references are final for root lifetime.
2. Existing production caller supplies `RuntimeModelLoadRequest(ruleName, connectionName, exact RuntimeBindingPlan, real originObject)` to the MODEL root. STARTER does not create ModelData/ModelLoader.
3. Root validates the exact plan against captured Context, reads only `capturedEngineContext.viewMaterializationIndex()`, and obtains the exact target View descriptor.
4. Root calls the typed `ModelDataFactory.createData(descriptor, originObject)` and obtains the actual production `ModelData`.
5. Root constructs the existing MODEL `ModelLoader` and uses the explicit three-argument `load(ruleName, modelData, connectionName)`; the default-connection overload is not used by this path.
6. Root calls its owned `Container.load(loader)` and freezes trusted provenance/handle around that same ModelData reference.
7. After at least one trusted load, `root.accessScope()` returns the active MODEL-minted scope. The scope is valid until the root/execution closes; after close it is stale/inactive and cannot be refreshed through a global registry.

`ruleName`/`connectionName` are routing facts, not target identity or permission. The only target identity is the exact current `RuntimeBindingPlan + CompiledViewMaterializationPlan`; permission remains `ModelAccessRuleKey + PolicyIndex + Guard`.

<a id="trusted-runtime-scope"></a>
## 3. Scope producer and handoff

`RuntimeModelExecutionRoot` is the concrete MODEL producer missing in R27. `RuntimeModelAccessScope`, frame and handle remain non-constructible outside MODEL. Scope frame/owner/cursor IDs are minted by the root and never accepted from a public frame request. STARTER obtains scope only from `RuntimeModelScopeResult` and then implements FLOW-R11 STEP-01/02 exactly.

<a id="composition-failure-algebra"></a>
## 4. Stable composition/session setup failures

MODEL session setup uses checked `RuntimeModelSessionException(code,message)` with closed codes for inactive scope, closed/already-sealed session, duplicate registration and ownership conflict. STARTER `ProtectedAccessRuntimeFactory.create(scope)` returns `ProtectedAccessCompositionResult`, never null and never relies on unchecked implementation exceptions as the external contract.

Mapping is exact:
- inactive scope -> `SCOPE_INACTIVE`;
- stale scope -> `SCOPE_STALE`;
- handle/current-Context mismatch -> `PROVENANCE_MISMATCH`;
- duplicate registration -> `SESSION_DUPLICATE_REGISTRATION`;
- cross-session ownership conflict -> `SESSION_OWNERSHIP_CONFLICT`;
- already sealed / closed -> corresponding session code.

Any setup failure returns no composition and reaches capability/Guard/MODEL effect zero times.

## 5. Preserved business/runtime semantics

Shared-View `TargetKey`, independent System owner, exact `ModelPath`, READ/WRITE-only, one actual ModelData/coordination domain, one-shot capability, Guard-before-effect and successful existing originData write-back remain as in BM-R20/FLOW-R11/R27.

Per current user confirmation, no new design is introduced for restoring a POJO/Map already copied before a later legacy commit failure. That behavior is explicitly excluded from current blocking Review/TestDesign criteria.

## 6. Gate

All 20 P1 remain OPEN pending same-revision BusinessModel/Impact/API/Architecture/Develop/CrossModule/Concurrency/TestDesign Review, current risk scan and machine Evidence. Implementation Plan/TDD/Development remain BLOCKED.

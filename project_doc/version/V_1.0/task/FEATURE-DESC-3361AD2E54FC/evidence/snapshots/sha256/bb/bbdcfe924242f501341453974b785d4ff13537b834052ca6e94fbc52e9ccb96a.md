# COMPILER P2 API Contract

> Revision `DESIGN-P2-R30`; base `DESIGN-P2-R29`; inputs `REQAN-P2-R01@d08612768131 + Overlay R04 + BM-R20 + FLOW-R11`; Impact `P2-IMPACT-R29`.
> Status `NEEDS_REVIEW / MACHINE_BLOCKED`. This is the complete current P2 cross-module contract.

## 1. Authority

`RuntimeModelLoadRequest` is a public MODEL loading DTO, **not** a credential and grants no READ/WRITE authority. `ModelAccessRuleKey` remains the sole permission authority; Guard is mandatory before effect. BM-R20/FLOW-R11 are unchanged. `RuntimeModelProductionInvocation`, `RuntimeProductionInvocationId`, its assembler, token root/replay/one-shot semantics, `INVOCATION_ROOT_MISMATCH`, and `INVOCATION_ALREADY_CONSUMED` are `NOT_ADOPTED_IN_P2 / DEFERRED`, not current API.

## 2. Preserved neutral construction surface

The following public factories remain normative and may not be removed: `RuleKey.of`, `TargetKey.of`, `ModelPath.of`, `ModelAccessRuleKey.of`, `CompiledTargetBinding.targetMain/propertyPath`, `RuntimeBindingPlan.exact`, `CompiledModelAccessRule.of`, `ModelAccessPolicyIndex.of`, opaque runtime ID `of(String)`, `RuntimeMutationVersion.of`, all `RuntimeFactValue` factories, `ProtectedAccessInvocation.of`, `RuntimeBindingProof.exact`, `ResolvedRuntimeTarget.of`, `RuntimeMutationStamp.of`, `ResolvedProtectedReadAccess.of`, `ResolvedWriteIntent.of`, `ResolvedProtectedWriteAccess.of`, `ProtectedReadValue.of`, `ProtectedWriteReceipt.of`, `ProtectedAccessDenial.of`, `ProtectedAccessResult.allowRead/allowWrite/deny`, and `RuntimeTargetResolution.resolved/denied`.

Normative neutral signatures:
```java
public enum AccessOperation { READ, WRITE }
public final class ModelAccessRuleKey { public static ModelAccessRuleKey of(SystemKey owner, TargetKey target, ModelPath path, AccessOperation op); }
public final class RuntimeBindingPlan { public static RuntimeBindingPlan exact(TargetKey source, CompiledTargetBinding binding); public TargetKey sourceTargetKey(); public CompiledTargetBinding compiledTargetBinding(); }
public interface RuntimeModelOperationPort { RuntimeFactValue read(ResolvedProtectedReadAccess a); ProtectedWriteReceipt write(ResolvedProtectedWriteAccess a); }
public enum DenialCode { POLICY_NOT_FOUND, POLICY_MISMATCH, RUNTIME_PLAN_MISMATCH, GUARD_UNAVAILABLE, CAPABILITY_ALREADY_CONSUMED, RUNTIME_CONTEXT_MISMATCH, RUNTIME_MODEL_PROVENANCE_MISMATCH, RUNTIME_TARGET_NOT_FOUND, RUNTIME_TARGET_AMBIGUOUS, WRITE_INTENT_NOT_FOUND, WRITE_INTENT_AMBIGUOUS, WRITE_INTENT_STALE, RUNTIME_SESSION_SCOPE_MISMATCH, RUNTIME_OBJECT_NOT_FOUND, RUNTIME_OBJECT_STALE, RUNTIME_OBJECT_ALREADY_REGISTERED, RUNTIME_OBJECT_OWNERSHIP_CONFLICT, RUNTIME_WRITE_FAILED }
```
All value/result classes above remain immutable, exact/case-sensitive where applicable, and retain their R29 getters/structural equality.

<a id="context-publication-aggregate"></a>
## 3. CONTEXT aggregate

```java
public final class CompiledViewMaterializationPlan { public static CompiledViewMaterializationPlan of(ViewKey k, java.util.List<CompiledMaterializationNode> fields); public ViewKey viewKey(); }
public final class CompiledViewMaterializationIndex { public static CompiledViewMaterializationIndex of(java.util.Collection<CompiledViewMaterializationPlan> plans); public java.util.Optional<CompiledViewMaterializationPlan> find(ViewKey k); public java.util.Set<ViewKey> viewKeys(); }
public final class CompiledModelSet { public CompiledViewMaterializationIndex viewMaterializationIndex(); }
public final class EngineContext { public EngineContext(CompiledModelSet set); public CompiledModelSet compiledModelSet(); public CompiledViewMaterializationIndex viewMaterializationIndex(); }
public final class ModelDataFactory { public ModelData createData(CompiledViewMaterializationPlan plan, Object originObject) throws DataNotDefineException; }
```
`CompiledViewMaterializationIndex` is a mandatory `CompiledModelSet` constructor member and participates in equality/hash/semantic digest and atomic publication. Missing/duplicate descriptor for a dynamic target blocks compile/publication. MODEL may only consume the captured aggregate; no `NormalizedBody`, XML/YAML, `ViewData`, `ModelData.name`, global/thread-local/default Context repair.

<a id="model-production-load-request"></a>
## 4. MODEL production loading

```java
package dec.core.model.runtime;
public final class RuntimeModelLoadRequest {
  public static RuntimeModelLoadRequest of(RuntimeBindingPlan plan, Object originObject, String ruleName, String connectionName);
  public RuntimeBindingPlan runtimeBindingPlan(); public Object originObject();
  public String ruleName(); public String connectionName();
}
public enum RuntimeModelLoadFailureCode { EXECUTION_CLOSED, PLAN_NOT_IN_CAPTURED_CONTEXT, MATERIALIZATION_DESCRIPTOR_NOT_FOUND, ORIGIN_NOT_MATERIALIZABLE, CONTAINER_LOAD_REJECTED }
public final class RuntimeModelLoadResult { public boolean loaded(); public java.util.Optional<RuntimeModelHandle> handle(); public java.util.Optional<RuntimeModelLoadFailure> failure(); }
public enum RuntimeModelScopeFailureCode { NO_TRUSTED_MODEL, EXECUTION_CLOSED, SCOPE_INACTIVE, SCOPE_STALE }
public final class RuntimeModelScopeResult { public boolean available(); public java.util.Optional<RuntimeModelAccessScope> scope(); public java.util.Optional<RuntimeModelScopeFailure> failure(); }
public enum ProductionContainerKind { COMMIT, SYNCHRONIZED }
public interface RuntimeModelExecutionRoot extends AutoCloseable { RuntimeModelLoadResult load(RuntimeModelLoadRequest request); RuntimeModelScopeResult accessScope(); void close(); }
public final class RuntimeModelExecutionRoots { public static RuntimeModelExecutionRoot production(EngineContext context, ProductionContainerKind kind); }
```

Trust rule: MODEL production lifecycle forms/uses the request. Request possession never creates trusted authority. Rule/Change/CustomAction/STARTER/application/business production code must not use root loading as a production entry. P2 intentionally does not add an opaque credential against deliberately bad plan+origin composition by already-trusted MODEL production code. Public production overloads accepting `Container`, `ModelData`, operation port, provider or Guard are forbidden; MODEL uses existing `ContainerFactory`.

Frozen `load(request)` order: (L01) closed -> `EXECUTION_CLOSED`; (L02) exact plan membership -> `PLAN_NOT_IN_CAPTURED_CONTEXT`; (L03) exact target ViewKey lookup in captured index -> `MATERIALIZATION_DESCRIPTOR_NOT_FOUND`; (L04) typed `ModelDataFactory.createData(plan, origin)` -> `ORIGIN_NOT_MATERIALIZABLE`; (L05) existing 3-arg `ModelLoader.load(ruleName, modelData, connectionName)` only; (L06) MODEL-owned Container.load -> `CONTAINER_LOAD_REJECTED`; (L07) freeze the **same** created/loaded ModelData reference in the Handle. Every L01-L06 failure yields handle/scope/STARTER/Guard/effect counts zero. Existing arbitrary ModelData, default connection, config reparse and A-load/B-handle are forbidden.

<a id="model-effect-provider"></a>
## 5. Scope/session/effect

```java
public final class RuntimeModelHandle { public RuntimeModelProvenance provenance(); /* no public constructor/wrap/rebind/ModelData getter */ }
public final class RuntimeModelFrame { public RuntimeExecutionFrameId frameId(); public RuntimeResolutionOwnerId ownerResolutionId(); public java.util.Optional<RuntimeCollectionCursorId> cursorId(); public java.util.List<RuntimeModelHandle> handles(); }
public enum RuntimeModelSessionFailureCode { SCOPE_INACTIVE, SESSION_CLOSED, SESSION_ALREADY_SEALED, DUPLICATE_REGISTRATION, OWNERSHIP_CONFLICT }
public interface RuntimeModelSession extends AutoCloseable { RuntimeModelSessionId sessionId(); RuntimeObjectId register(RuntimeModelHandle h) throws RuntimeModelSessionException; void seal() throws RuntimeModelSessionException; LocatedRuntimeObject locate(ResolvedRuntimeTarget t); RuntimeMutationVersion currentVersion(ResolvedRuntimeTarget t, ModelPath p); void close(); }
public enum RuntimeModelEffectBindingFailureCode { SCOPE_INACTIVE, SESSION_NOT_SEALED, SESSION_CLOSED, SESSION_SCOPE_MISMATCH }
public interface RuntimeModelEffectProvider { RuntimeModelEffectBindingResult bind(RuntimeModelSession sealedSession); }
public final class RuntimeModelAccessScope { public RuntimeModelFrame frame(); public RuntimeModelSession beginSession() throws RuntimeModelSessionException; public RuntimeModelEffectProvider effectProvider(); /* no public constructor */ }
```
Scope is the trusted cross-module object boundary. Provider binding succeeds only for the exact sealed session from that scope. The returned operation port is private to STARTER composition and rechecks session/object/registered handle before each effect.

<a id="starter-protected-composition"></a>
## 6. STARTER / FLOW-R11

```java
public final class ProtectedAccessRuntimeFactory { public static ProtectedAccessRuntimeFactory production(EngineContext context); public ProtectedAccessCompositionResult create(RuntimeModelAccessScope scope); }
public interface RuntimeTargetResolver { RuntimeTargetResolution resolve(RuntimeBindingPlan plan, ProtectedAccessInvocation invocation, RuntimeModelSession session); }
public interface RuleProtectedAccessEntry { ProtectedAccessResult invoke(ProtectedAccessInvocation i); }
public interface ChangeProtectedAccessEntry { ProtectedAccessResult invoke(ProtectedAccessInvocation i); }
public interface CustomActionProtectedAccessEntry { ProtectedAccessResult invoke(ProtectedAccessInvocation i); }
public final class ProtectedAccessComposition implements AutoCloseable { public ProtectedAccessPort protectedAccessPort(); public RuleProtectedAccessEntry ruleEntry(); public ChangeProtectedAccessEntry changeEntry(); public CustomActionProtectedAccessEntry customActionEntry(); public RuntimeModelSessionId runtimeModelSessionId(); public void close(); }
```
R29 composition/session stable failure codes remain current, including scope/provenance/session and effect-session binding failures. FLOW-R11 remains: trusted MODEL frame precondition -> STEP-01 validate -> STEP-02 register/seal + bind same-scope provider -> STEP-03 exact resolve -> STEP-04 access/intent+one-shot capability -> STEP-05 Guard -> STEP-06 composition-private operation port. The port revalidates the same `sessionId/runtimeObjectId/registered handle`; invariant is `resolve A -> Guard A -> effect A`.

## 7. Dependency / exclusions

Allowed: `compiler -> context`, `model -> context`, `starter -> context + model`, business consumer -> `starter + context`. Forbidden: business consumer -> root/load/ModelData/EffectProvider/OperationPort; caller Container/ModelData/Guard/operation injection.

Per explicit user directive, P2 does not require restoration of a POJO/Map already copied before a later legacy commit failure. Successful originData write-back and Guard-before-effect remain required. No production Java, current risk scan, TDD execution or lifecycle promotion is claimed.

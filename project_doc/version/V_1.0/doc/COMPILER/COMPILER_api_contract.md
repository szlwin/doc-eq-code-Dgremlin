# COMPILER P2 API Contract

> Revision `DESIGN-P2-R28`; base `DESIGN-P2-R27`.
> Inputs: `REQAN-P2-R01@d08612768131 + Overlay R04 + BM-R20 + FLOW-R11`; parallel projection `P2-IMPACT-R27`.
> Status: `NEEDS_REVIEW / MACHINE_BLOCKED`. This is the complete current P2 cross-module contract.

## 1. Ownership and preserved current factories

- CONTEXT `dec.core.context.runtime`: policy/binding/value/invocation/result contracts.
- CONTEXT `dec.core.context.model`: `CompiledModelSet` aggregate + typed View materialization contracts.
- CONTEXT `dec.core.context.data`: typed `ModelDataFactory` overload.
- MODEL `dec.core.model.runtime`: production execution root, trusted handle/frame/scope/session/locator and MODEL effect implementation.
- STARTER `dec.core.starter.access`: composition/result/failure, target resolution, capability/Guard and Rule/Change/CustomAction entries.

All R27/R25 public factories remain current, including `RuleKey.of`, `TargetKey.of`, `ModelPath.of`, `ModelAccessRuleKey.of`, `CompiledTargetBinding.targetMain/propertyPath`, `RuntimeBindingPlan.exact`, `CompiledModelAccessRule.of`, `ModelAccessPolicyIndex.of`, opaque-ID `of(String)`, `RuntimeMutationVersion.of`, all `RuntimeFactValue` factories, `ProtectedAccessInvocation.of`, `RuntimeBindingProof.exact`, `ResolvedRuntimeTarget.of`, `RuntimeMutationStamp.of`, `ResolvedProtectedReadAccess.of`, `ResolvedWriteIntent.of`, `ResolvedProtectedWriteAccess.of`, `ProtectedReadValue.of`, `ProtectedWriteReceipt.of`, `ProtectedAccessDenial.of`, `ProtectedAccessResult.allowRead/allowWrite/deny`, and `RuntimeTargetResolution.resolved/denied`. No current revision may remove these construction surfaces.


## 1A. Preserved current neutral runtime signatures (self-contained)

```java
package dec.core.context.runtime;

public final class RuleKey {
  public static RuleKey of(RuleViewKey owner, String localName);
  public RuleViewKey ownerRuleViewKey(); public String localRuleName();
}
public final class TargetKey { public static TargetKey of(ViewKey sourceViewKey); public ViewKey sourceViewKey(); }
public final class ModelPath { public static ModelPath of(List<String> canonicalSegments); public List<String> canonicalSegments(); }
public enum AccessOperation { READ, WRITE }
public final class ModelAccessRuleKey {
  public static ModelAccessRuleKey of(SystemKey owner, TargetKey target, ModelPath path, AccessOperation operation);
  public SystemKey authorizationOwnerSystemKey(); public TargetKey targetKey();
  public ModelPath modelPath(); public AccessOperation operation();
}
public enum PolicyStatus { STATIC_ALLOW, RUNTIME_GUARD_REQUIRED }
public enum RuntimeAccessRequirement { NONE, EXACT_RUNTIME_BINDING }
public enum ResolvedTargetKind { TARGET_MAIN, PROPERTY_PATH }
public final class CompiledTargetBinding {
  public static CompiledTargetBinding targetMain(ViewKey view, String exactValue);
  public static CompiledTargetBinding propertyPath(ViewKey view, String exactValue);
  public ViewKey targetViewKey(); public ResolvedTargetKind kind(); public String exactResolvedValue();
}
public final class RuntimeBindingPlan {
  public static RuntimeBindingPlan exact(TargetKey source, CompiledTargetBinding binding);
  public TargetKey sourceTargetKey(); public CompiledTargetBinding compiledTargetBinding();
}
public final class CompiledModelAccessRule {
  public static CompiledModelAccessRule of(ModelAccessRuleKey key, PolicyStatus status,
      RuntimeAccessRequirement requirement, Optional<RuntimeBindingPlan> plan);
  public ModelAccessRuleKey key(); public PolicyStatus policyStatus();
  public RuntimeAccessRequirement runtimeRequirement(); public Optional<RuntimeBindingPlan> runtimeBindingPlan();
}
public final class ModelAccessPolicyIndex {
  public static ModelAccessPolicyIndex of(Collection<CompiledModelAccessRule> rules);
  public Optional<CompiledModelAccessRule> find(ModelAccessRuleKey key);
}
```

Opaque IDs `ProtectedInvocationId`, `RuntimeObjectId`, `RuntimeWriteIntentId`, `RuntimeExecutionFrameId`, `RuntimeResolutionOwnerId`, `RuntimeCollectionCursorId`, `RuntimeModelSessionId` are each `public final`, immutable, exact/case-sensitive, reject null/blank, and expose `public static of(String)`, `value()`, structural `equals/hashCode`.

```java
public final class RuntimeMutationVersion { public static RuntimeMutationVersion of(long nonNegative); public long value(); }
public final class RuntimeFactValue {
  public enum Kind { NULL, BOOLEAN, INTEGER, DECIMAL, STRING, LIST, OBJECT }
  public static RuntimeFactValue nullValue(); public static RuntimeFactValue ofBoolean(boolean value);
  public static RuntimeFactValue ofInteger(BigInteger value); public static RuntimeFactValue ofDecimal(BigDecimal value);
  public static RuntimeFactValue ofString(String value); public static RuntimeFactValue ofList(List<RuntimeFactValue> value);
  public static RuntimeFactValue ofObject(Map<String,RuntimeFactValue> value);
  public Kind kind(); public String deterministicJson();
}
public final class ProtectedAccessInvocation {
  public static ProtectedAccessInvocation of(ProtectedInvocationId id, ModelAccessRuleKey key,
      RuntimeExecutionFrameId frameId, RuntimeResolutionOwnerId ownerId,
      Optional<RuntimeCollectionCursorId> cursorId);
  public ProtectedInvocationId invocationId(); public ModelAccessRuleKey modelAccessRuleKey();
  public RuntimeExecutionFrameId frameId(); public RuntimeResolutionOwnerId ownerResolutionId();
  public Optional<RuntimeCollectionCursorId> cursorId();
}
public interface ProtectedAccessPort { ProtectedAccessResult invoke(ProtectedAccessInvocation invocation); }
public final class RuntimeBindingProof { public static RuntimeBindingProof exact(String digest); public String value(); }
public final class ResolvedRuntimeTarget {
  public static ResolvedRuntimeTarget of(RuntimeModelSessionId sessionId, RuntimeObjectId objectId,
      TargetKey targetKey, CompiledTargetBinding binding, RuntimeExecutionFrameId frameId,
      RuntimeResolutionOwnerId ownerId, Optional<RuntimeCollectionCursorId> cursorId,
      RuntimeBindingProof proof);
  public RuntimeModelSessionId sessionId(); public RuntimeObjectId runtimeObjectId(); public TargetKey targetKey();
  public CompiledTargetBinding compiledTargetBinding(); public RuntimeExecutionFrameId frameId();
  public RuntimeResolutionOwnerId ownerResolutionId(); public Optional<RuntimeCollectionCursorId> cursorId();
  public RuntimeBindingProof bindingProof();
}
public final class RuntimeMutationStamp {
  public static RuntimeMutationStamp of(RuntimeModelSessionId sessionId, RuntimeObjectId objectId,
      ModelPath path, RuntimeMutationVersion version);
  public RuntimeModelSessionId sessionId(); public RuntimeObjectId runtimeObjectId();
  public ModelPath modelPath(); public RuntimeMutationVersion version();
}
public final class ResolvedProtectedReadAccess {
  public static ResolvedProtectedReadAccess of(ProtectedInvocationId id, ModelAccessRuleKey key,
      ResolvedRuntimeTarget target);
  public ProtectedInvocationId invocationId(); public ModelAccessRuleKey modelAccessRuleKey();
  public ResolvedRuntimeTarget resolvedRuntimeTarget();
}
public final class ResolvedWriteIntent {
  public static ResolvedWriteIntent of(RuntimeWriteIntentId id, ModelAccessRuleKey key,
      Optional<RuleKey> provenance, ResolvedRuntimeTarget target, RuntimeMutationStamp stamp);
  public RuntimeWriteIntentId id(); public ModelAccessRuleKey modelAccessRuleKey();
  public Optional<RuleKey> ruleKeyProvenance(); public ResolvedRuntimeTarget resolvedRuntimeTarget();
  public RuntimeMutationStamp mutationStamp();
}
public final class ResolvedProtectedWriteAccess {
  public static ResolvedProtectedWriteAccess of(ProtectedInvocationId id, ResolvedWriteIntent intent);
  public ProtectedInvocationId invocationId(); public ResolvedWriteIntent writeIntent();
}
public interface RuntimeModelOperationPort {
  RuntimeFactValue read(ResolvedProtectedReadAccess access);
  ProtectedWriteReceipt write(ResolvedProtectedWriteAccess access);
}
public final class ProtectedReadValue {
  public static ProtectedReadValue of(ProtectedInvocationId id, RuntimeFactValue value);
  public ProtectedInvocationId invocationId(); public RuntimeFactValue value();
}
public final class ProtectedWriteReceipt {
  public static ProtectedWriteReceipt of(ProtectedInvocationId id, RuntimeWriteIntentId intentId,
      RuntimeMutationVersion committedVersion);
  public ProtectedInvocationId invocationId(); public RuntimeWriteIntentId writeIntentId();
  public RuntimeMutationVersion committedVersion();
}
public enum DenialCode {
  POLICY_NOT_FOUND, POLICY_MISMATCH, RUNTIME_PLAN_MISMATCH, GUARD_UNAVAILABLE,
  CAPABILITY_ALREADY_CONSUMED, RUNTIME_CONTEXT_MISMATCH, RUNTIME_MODEL_PROVENANCE_MISMATCH,
  RUNTIME_TARGET_NOT_FOUND, RUNTIME_TARGET_AMBIGUOUS, WRITE_INTENT_NOT_FOUND,
  WRITE_INTENT_AMBIGUOUS, WRITE_INTENT_STALE, RUNTIME_SESSION_SCOPE_MISMATCH,
  RUNTIME_OBJECT_NOT_FOUND, RUNTIME_OBJECT_STALE, RUNTIME_OBJECT_ALREADY_REGISTERED,
  RUNTIME_OBJECT_OWNERSHIP_CONFLICT, RUNTIME_WRITE_FAILED
}
public final class ProtectedAccessDenial {
  public static ProtectedAccessDenial of(ProtectedInvocationId id, DenialCode code, String stableMessage);
  public ProtectedInvocationId invocationId(); public DenialCode code(); public String stableMessage();
}
public final class ProtectedAccessResult {
  public static ProtectedAccessResult allowRead(ProtectedReadValue value);
  public static ProtectedAccessResult allowWrite(ProtectedWriteReceipt receipt);
  public static ProtectedAccessResult deny(ProtectedAccessDenial denial);
  public boolean allowed(); public Optional<ProtectedReadValue> readValue();
  public Optional<ProtectedWriteReceipt> writeReceipt(); public Optional<ProtectedAccessDenial> denial();
}
```

<a id="context-publication-aggregate"></a>
## 2. CONTEXT publication aggregate

```java
package dec.core.context.model;

public enum MaterializationNodeKind { SCALAR, OBJECT, LIST }
public final class CompiledMaterializationNode {
  public static CompiledMaterializationNode scalar(String fieldName);
  public static CompiledMaterializationNode object(String fieldName, List<CompiledMaterializationNode> children);
  public static CompiledMaterializationNode list(String fieldName, List<CompiledMaterializationNode> elementShape);
  public String fieldName(); public MaterializationNodeKind kind();
  public List<CompiledMaterializationNode> children();
}
public final class CompiledViewMaterializationPlan {
  public static CompiledViewMaterializationPlan of(ViewKey viewKey, List<CompiledMaterializationNode> rootFields);
  public ViewKey viewKey(); public List<CompiledMaterializationNode> rootFields();
}
public final class CompiledViewMaterializationIndex {
  public static CompiledViewMaterializationIndex of(Collection<CompiledViewMaterializationPlan> plans);
  public Optional<CompiledViewMaterializationPlan> find(ViewKey viewKey);
  public Set<ViewKey> viewKeys();
}
```

`CompiledViewMaterializationIndex` is not EngineContext side state. It is a mandatory `CompiledModelSet` constructor member:

```java
public final class CompiledModelSet {
  public CompiledModelSet(
      PublishedSourceManifest sourceManifest,
      Registry<DefinitionKey, CompiledDefinition> definitions,
      CompiledViewMaterializationIndex viewMaterializationIndex,
      DeferredRegistry deferred,
      List<Diagnostic> diagnostics,
      DigestPair digestPair,
      String compilerVersion,
      String schemaVersion,
      String optionsVersion);
  public CompiledViewMaterializationIndex viewMaterializationIndex();
}
public final class EngineContext {
  public EngineContext(CompiledModelSet compiledModelSet);
  public CompiledModelSet compiledModelSet();
  public CompiledModelSet modelSet();
  public CompiledViewMaterializationIndex viewMaterializationIndex(); // delegates to compiledModelSet
  public CoreConfigProjection projection();
}
```

Mandatory aggregate rules:
1. compiler resolves View materialization once and builds the index before `CompiledModelSet` construction;
2. every P2 `RUNTIME_GUARD_REQUIRED + EXACT_RUNTIME_BINDING` plan target View has exactly one descriptor; missing/duplicate descriptors are compile/publication errors;
3. `CompiledModelSet.equals/hashCode` include the index;
4. canonical stable serialization of the index (ViewKey-sorted, node-order canonical) is included in semantic-digest input, so semantically different materialization plans cannot compare/publish as the same aggregate;
5. `EngineContext` stores no independent materialization field/registry; isolation follows the immutable `CompiledModelSet` instance;
6. MODEL may only obtain the index from the captured `EngineContext/CompiledModelSet`; no `NormalizedBody`, XML/YAML, `ViewData`, `ModelData.name`, thread-local/global/default Context repair is legal.

Existing typed ModelData creation stays:

```java
package dec.core.context.data;
public final class ModelDataFactory {
  public ModelData createData(CompiledViewMaterializationPlan plan, Object originObject)
      throws DataNotDefineException;
}
```

<a id="model-production-root"></a>
## 3. MODEL production integration and scope producer

The production integration is now explicit and unique; `ModelLoader.load(String, ModelData, ...)` remains the final existing load anchor but STARTER/business code does not create the trusted ModelData itself.

```java
package dec.core.model.runtime;

public final class RuntimeModelLoadRequest {
  public static RuntimeModelLoadRequest of(
      String ruleName,
      String connectionName,
      RuntimeBindingPlan exactPlan,
      Object originObject);
  public String ruleName(); public String connectionName();
  public RuntimeBindingPlan runtimeBindingPlan(); public Object originObject();
}
public enum RuntimeModelLoadFailureCode {
  EXECUTION_CLOSED,
  PLAN_NOT_IN_CAPTURED_CONTEXT,
  MATERIALIZATION_DESCRIPTOR_NOT_FOUND,
  ORIGIN_NOT_MATERIALIZABLE,
  CONTAINER_LOAD_REJECTED
}
public final class RuntimeModelLoadFailure {
  public RuntimeModelLoadFailureCode code(); public String stableMessage();
}
public final class RuntimeModelLoadResult {
  public boolean loaded();
  public Optional<RuntimeModelHandle> handle();
  public Optional<RuntimeModelLoadFailure> failure();
}
public enum RuntimeModelScopeFailureCode {
  NO_TRUSTED_MODEL,
  EXECUTION_CLOSED,
  SCOPE_INACTIVE,
  SCOPE_STALE
}
public final class RuntimeModelScopeFailure {
  public RuntimeModelScopeFailureCode code(); public String stableMessage();
}
public final class RuntimeModelScopeResult {
  public boolean available();
  public Optional<RuntimeModelAccessScope> scope();
  public Optional<RuntimeModelScopeFailure> failure();
}
public interface RuntimeModelExecutionRoot extends AutoCloseable {
  RuntimeModelLoadResult load(RuntimeModelLoadRequest request);
  RuntimeModelScopeResult accessScope();
  @Override void close();
}
public final class RuntimeModelExecutionRoots {
  public static RuntimeModelExecutionRoot production(
      EngineContext capturedEngineContext,
      dec.core.model.container.Container ownedContainer);
}
```

Mandatory `load(...)` algorithm:
1. use only the root's final captured `EngineContext`; require exact plan membership there;
2. lookup the exact descriptor through `capturedEngineContext.viewMaterializationIndex().find(plan.compiledTargetBinding().targetViewKey())`;
3. call typed `ModelDataFactory.createData(descriptor, originObject)`;
4. create an internal `ModelLoader` and call its existing three-argument `load(ruleName, modelData, connectionName)`; the two-argument default-connection overload is not used by this path;
5. call the root-owned `Container.load(loader)`;
6. freeze `RuntimeModelProvenance` + `RuntimeModelHandle` around that same `ModelData` reference;
7. after at least one successful trusted load, `accessScope()` may expose one MODEL-minted active scope; scope validity ends when the root/execution closes.

`ruleName`/`connectionName` are execution routing facts only; they never replace `RuntimeBindingPlan/CompiledViewMaterializationPlan` as target identity. No thread-local/global/default Context, plan, handle or scope lookup is permitted.

<a id="trusted-runtime-scope"></a>
## 4. MODEL trusted scope/session and stable session failures

```java
public final class RuntimeModelProvenance {
  // no public/protected constructor/factory
  public RuntimeBindingPlan runtimeBindingPlan();
}
public final class RuntimeModelHandle {
  // no public/protected constructor/factory/wrap/rebind; no public ModelData accessor
  public RuntimeModelProvenance provenance();
}
public final class RuntimeModelFrame {
  // no public/protected constructor/factory/rebind
  public RuntimeExecutionFrameId frameId();
  public RuntimeResolutionOwnerId ownerResolutionId();
  public Optional<RuntimeCollectionCursorId> cursorId();
  public List<RuntimeModelHandle> handles();
}
public enum RuntimeModelSessionFailureCode {
  SCOPE_INACTIVE,
  SESSION_CLOSED,
  SESSION_ALREADY_SEALED,
  DUPLICATE_REGISTRATION,
  OWNERSHIP_CONFLICT
}
public final class RuntimeModelSessionException extends Exception {
  public RuntimeModelSessionFailureCode code();
  public String stableMessage();
}
public final class RuntimeModelAccessScope {
  // no public/protected constructor/factory; created only by RuntimeModelExecutionRoot
  public RuntimeModelFrame frame();
  public RuntimeModelSession beginSession() throws RuntimeModelSessionException;
}
public interface RuntimeModelSession extends AutoCloseable {
  RuntimeModelSessionId sessionId();
  RuntimeObjectId register(RuntimeModelHandle trustedHandle) throws RuntimeModelSessionException;
  void seal() throws RuntimeModelSessionException;
  LocatedRuntimeObject locate(ResolvedRuntimeTarget target);
  RuntimeMutationVersion currentVersion(ResolvedRuntimeTarget target, ModelPath path);
  @Override void close();
}
```

<a id="composition-failure-algebra"></a>
## 5. STARTER composition failure algebra

```java
package dec.core.starter.access;

public enum ProtectedAccessCompositionFailureCode {
  SCOPE_INACTIVE,
  SCOPE_STALE,
  PROVENANCE_MISMATCH,
  SESSION_DUPLICATE_REGISTRATION,
  SESSION_OWNERSHIP_CONFLICT,
  SESSION_ALREADY_SEALED,
  SESSION_CLOSED
}
public final class ProtectedAccessCompositionFailure {
  public ProtectedAccessCompositionFailureCode code();
  public String stableMessage();
}
public final class ProtectedAccessCompositionResult {
  public boolean created();
  public Optional<ProtectedAccessComposition> composition();
  public Optional<ProtectedAccessCompositionFailure> failure();
}
public final class ProtectedAccessRuntimeFactory {
  public static ProtectedAccessRuntimeFactory production(EngineContext capturedEngineContext);
  public ProtectedAccessCompositionResult create(RuntimeModelAccessScope trustedScope);
}
```

`create(scope)` must: validate active scope/frame/provenance against the same captured Context; call `beginSession`; register every trusted handle exactly once; seal once; return a composition only after all setup succeeds. A stale/inactive scope or provenance mismatch returns the corresponding composition code. MODEL `RuntimeModelSessionException` duplicate/ownership/seal/closed codes are mapped one-to-one to the corresponding composition code. Failure returns `created=false`, `composition=empty`, one stable failure, and capability/Guard/MODEL effect count zero. No null/unchecked-exception-as-contract/fallback session is legal.

## 6. Remaining current STARTER/MODEL signatures

```java
package dec.core.model.runtime;
public final class LocatedRuntimeObject {
  public RuntimeModelSessionId sessionId(); public RuntimeObjectId runtimeObjectId();
  public RuntimeModelProvenance provenance();
}

package dec.core.starter.access;
public enum RuntimeTargetResolutionStatus {
  RESOLVED, NOT_FOUND, AMBIGUOUS, CONTEXT_MISMATCH, PROVENANCE_MISMATCH
}
public final class RuntimeTargetResolution {
  public static RuntimeTargetResolution resolved(ResolvedRuntimeTarget target);
  public static RuntimeTargetResolution denied(RuntimeTargetResolutionStatus status, DenialCode code);
  public RuntimeTargetResolutionStatus status(); public Optional<ResolvedRuntimeTarget> target();
  public Optional<DenialCode> denialCode();
}
public interface RuntimeTargetResolver {
  RuntimeTargetResolution resolve(RuntimeBindingPlan plan, ProtectedAccessInvocation invocation,
      RuntimeModelSession session);
}
public final class RuntimeExecutionFrameSnapshot {
  public static RuntimeExecutionFrameSnapshot from(RuntimeModelFrame trustedFrame);
  public RuntimeExecutionFrameId frameId(); public RuntimeResolutionOwnerId ownerResolutionId();
  public Optional<RuntimeCollectionCursorId> cursorId(); public RuntimeModelFrame runtimeModelFrame();
}
public interface RuleProtectedAccessEntry { ProtectedAccessResult invoke(ProtectedAccessInvocation invocation); }
public interface ChangeProtectedAccessEntry { ProtectedAccessResult invoke(ProtectedAccessInvocation invocation); }
public interface CustomActionProtectedAccessEntry { ProtectedAccessResult invoke(ProtectedAccessInvocation invocation); }
public final class ProtectedAccessComposition implements AutoCloseable {
  public ProtectedAccessPort protectedAccessPort();
  public RuntimeExecutionFrameId frameId(); public RuntimeResolutionOwnerId ownerResolutionId();
  public RuntimeModelSessionId runtimeModelSessionId();
  public RuleProtectedAccessEntry ruleEntry(); public ChangeProtectedAccessEntry changeEntry();
  public CustomActionProtectedAccessEntry customActionEntry();
  @Override public void close();
}
```

The closed result/value algebra, one-shot capability, exact mutation stamp and `ModelAccessRuleKey` sole permission authority remain unchanged.

## 7. Explicit scope exclusion from this remediation

Per the current user directive, **legacy `ModelContainer` POJO/Map copy-back behavior after a later commit failure is not changed and is not a current P2 blocker**. R28 preserves successful existing originData write-back reachability but introduces no new requirement to restore a POJO/Map already copied before a downstream legacy commit failure. TestDesign must not require that behavior for this remediation.

No production Java, TDD execution, risk Evidence or lifecycle promotion is claimed.

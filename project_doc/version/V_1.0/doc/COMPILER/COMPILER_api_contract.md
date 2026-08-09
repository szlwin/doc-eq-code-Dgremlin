# COMPILER P2 API Contract

> Revision `DESIGN-P2-R27`; base R26; inputs Overlay R04 + `BM-R20` + `FLOW-R11`; parallel Impact `P2-IMPACT-R26`.
> Status `NEEDS_REVIEW / MACHINE_BLOCKED`. This is the complete current P2 cross-module contract; R26 fresh-snapshot/open types are superseded.

<a id="current-api-contract"></a>
## 1. Ownership and visibility

- CONTEXT `dec.core.context.runtime`: policy/binding/value/invocation/result contracts.
- CONTEXT `dec.core.context.model`: compiler-published neutral View materialization descriptor.
- CONTEXT `dec.core.context.data`: existing ModelDataFactory with typed compiled-plan overload.
- MODEL `dec.core.model.runtime`: trusted provenance/handle/frame/access-scope/session/locator contracts; actual effect implementation.
- STARTER `dec.core.starter.access`: production composition, target resolution, capability/Guard and Rule/Change/CustomAction entry contracts.

Every cross-module top-level type is explicitly `public`. Every immutable value below has the shown public construction surface. MODEL trusted provenance/scope/frame/handle remain intentionally non-constructible outside MODEL.

## 2. CONTEXT policy/binding construction surface

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
  public static ModelAccessRuleKey of(SystemKey owner, TargetKey target, ModelPath path, AccessOperation op);
  public SystemKey authorizationOwnerSystemKey(); public TargetKey targetKey(); public ModelPath modelPath(); public AccessOperation operation();
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

Only `STATIC_ALLOW + NONE + no plan` and `RUNTIME_GUARD_REQUIRED + EXACT_RUNTIME_BINDING + plan` are publishable.

## 3. Compiler-published typed View materialization descriptor

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
}
```

The compiler constructs these types from resolved View semantics and publishes the index in the same immutable Context candidate as P2 policy/binding facts. Runtime consumers may not inspect `CompiledDefinition.normalizedBody()` to recover this information.

Existing CONTEXT data API gains one typed overload while preserving current object/write-back behavior:

```java
package dec.core.context.data;
public final class ModelDataFactory {
  public ModelData createData(CompiledViewMaterializationPlan plan, Object originObject)
      throws DataNotDefineException;
}
```

The overload uses only `plan` for field/relation shape; it must not call default `ConfigContextUtil`, select a View by String name, or parse raw/normalized configuration. For non-Map `originObject`, the same object is retained as `ModelData.originData`; for Map, the same Map remains the values object, matching existing production semantics.

## 4. CONTEXT runtime value/invocation/result construction surface

Opaque IDs `ProtectedInvocationId`, `RuntimeObjectId`, `RuntimeWriteIntentId`, `RuntimeExecutionFrameId`, `RuntimeResolutionOwnerId`, `RuntimeCollectionCursorId`, `RuntimeModelSessionId` are `public final`, immutable, exact/case-sensitive, reject null/blank, and each exposes `public static of(String)` + `value()` + structural equality/hash.

```java
public final class RuntimeMutationVersion { public static RuntimeMutationVersion of(long nonNegative); public long value(); }
public final class RuntimeFactValue {
  public enum Kind { NULL, BOOLEAN, INTEGER, DECIMAL, STRING, LIST, OBJECT }
  public static RuntimeFactValue nullValue(); public static RuntimeFactValue ofBoolean(boolean v);
  public static RuntimeFactValue ofInteger(BigInteger v); public static RuntimeFactValue ofDecimal(BigDecimal v);
  public static RuntimeFactValue ofString(String v); public static RuntimeFactValue ofList(List<RuntimeFactValue> v);
  public static RuntimeFactValue ofObject(Map<String,RuntimeFactValue> v);
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

ALLOW READ has read value only; ALLOW WRITE has receipt only; DENY has denial only. `RuntimeFactValue` remains a neutral closed result/value domain; it is **not** the source of a production ModelData object in R27.

<a id="trusted-runtime-model-provenance"></a>
## 5. MODEL trusted frame/scope/session contracts

```java
package dec.core.model.runtime;

public final class RuntimeModelProvenance {
  // NO public/protected constructor or public static factory.
  public RuntimeBindingPlan runtimeBindingPlan();
}
public final class RuntimeModelHandle {
  // NO public/protected constructor/factory/wrap/rebind; NO public ModelData accessor.
  public RuntimeModelProvenance provenance();
}
public final class RuntimeModelFrame {
  // NO public/protected constructor/factory/rebind.
  public RuntimeExecutionFrameId frameId(); public RuntimeResolutionOwnerId ownerResolutionId();
  public Optional<RuntimeCollectionCursorId> cursorId(); public List<RuntimeModelHandle> handles();
}
public final class RuntimeModelAccessScope {
  // NO public/protected constructor/factory. Created only by the active MODEL production execution root.
  public RuntimeModelFrame frame();
  public RuntimeModelSession beginSession();
}
public final class LocatedRuntimeObject {
  public RuntimeModelSessionId sessionId(); public RuntimeObjectId runtimeObjectId();
  public RuntimeModelProvenance provenance();
}
public interface RuntimeModelSession extends AutoCloseable {
  RuntimeModelSessionId sessionId();
  RuntimeObjectId register(RuntimeModelHandle trustedHandle);
  void seal();
  LocatedRuntimeObject locate(ResolvedRuntimeTarget target);
  RuntimeMutationVersion currentVersion(ResolvedRuntimeTarget target, ModelPath path);
  @Override void close();
}
```

MODEL implementation rule: package-private `CompiledRuntimeModelBinder` is called only by the existing ModelLoader/ModelContainer lifecycle. It validates exact plan membership, obtains the typed materialization plan from the captured Context, creates the actual ModelData via the typed ModelDataFactory overload, loads that same ModelData into the production loader/container, and freezes provenance+handle around the same reference. The active MODEL execution root then mints `RuntimeModelAccessScope` and scope IDs; no public request supplies frame/owner/cursor.

<a id="runtime-target-resolution"></a>
## 6. STARTER composition/resolution contracts

```java
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
public final class ProtectedAccessRuntimeFactory {
  public static ProtectedAccessRuntimeFactory production(EngineContext capturedEngineContext);
  public ProtectedAccessComposition create(RuntimeModelAccessScope trustedScope);
}
public final class ProtectedAccessComposition implements AutoCloseable {
  public ProtectedAccessPort protectedAccessPort();
  public RuntimeExecutionFrameId frameId(); public RuntimeResolutionOwnerId ownerResolutionId();
  public RuntimeModelSessionId runtimeModelSessionId();
  public RuleProtectedAccessEntry ruleEntry(); public ChangeProtectedAccessEntry changeEntry();
  public CustomActionProtectedAccessEntry customActionEntry();
  @Override public void close();
}
```

`create(scope)` obtains `scope.frame()`, validates all handle plans against the captured Context, calls `scope.beginSession()`, registers exactly those trusted handles, seals the session, and retains that session for the composition. It accepts no independent scope IDs, frame, handle list, ModelData, session, operation port or Guard. Frame/owner/cursor equality is therefore checked against independently MODEL-minted facts.

## 7. Superseded R26 API

`RuntimeModelMaterializationInput`, `RuntimeModelFrameRequest`, `RuntimeModelRuntime`, `RuntimeModelRuntimes`, `RuntimeModelExecution`, `RuntimeModelExecutionResult`, `RuntimeModelOpenFailure`, and `RuntimeModelOpenFailureCode` are **not current R27 contracts**. No current TestDesign may require their implementation.

## 8. Effect/dependency rule

Legal dependencies: compiler->context; model->context; starter->context+model. Forbidden: context->compiler/model/starter, model->starter, downstream-core->starter. STARTER owns resolver/intent/capability/Guard. MODEL owns trusted production handle/scope/session/locator/coordination and actual READ/WRITE over the same ModelData used by ModelContainer. Existing successful ModelContainer write-back to originData is part of the required production reachability.

No production Java/TDD/risk Evidence is claimed.

# COMPILER P2 API Contract

> Revision: `DESIGN-P2-R26`; base `DESIGN-P2-R25`.
> Inputs: Overlay R04 + `BM-R20` + `FLOW-R11`; Impact `P2-IMPACT-R25`.
> Status: `NEEDS_REVIEW / MACHINE_BLOCKED`.

This file is the complete current P2 cross-module contract. R25 authority, result algebra and owner-module visibility remain unchanged; R26 adds the concrete MODEL materialization producer and MODEL -> STARTER handoff and removes public session registration/seal.

<a id="current-api-contract"></a>
## 1. Ownership and common rules

- CONTEXT `dec.core.context.runtime`: neutral policy, compiled binding, invocation, resolved-access, values/results and operation-port contracts.
- MODEL `dec.core.model.runtime`: trusted materialization, provenance/handle/frame, execution/session/locator and production operation implementation.
- STARTER `dec.core.starter.access`: production bootstrap, target resolution, intent/capability/Guard and Rule/Change/CustomAction entries.
- Every cross-module type named below is `public`; value objects are immutable, required fields reject null, and identity is structural. `RuntimeFactValue` is deep immutable and never exposes a live arbitrary `Object`.

## 2. CONTEXT neutral authority

```java
public final class TargetKey {
  public static TargetKey of(ViewKey sourceViewKey);
  public ViewKey sourceViewKey();
}
public final class ModelPath {
  public static ModelPath of(List<String> canonicalSegments);
  public List<String> canonicalSegments();
}
public enum AccessOperation { READ, WRITE }
public final class ModelAccessRuleKey {
  public static ModelAccessRuleKey of(SystemKey owner, TargetKey target, ModelPath path, AccessOperation operation);
  public SystemKey authorizationOwnerSystemKey();
  public TargetKey targetKey();
  public ModelPath modelPath();
  public AccessOperation operation();
}
public enum PolicyStatus { STATIC_ALLOW, RUNTIME_GUARD_REQUIRED }
public enum RuntimeAccessRequirement { NONE, EXACT_RUNTIME_BINDING }
public enum ResolvedTargetKind { TARGET_MAIN, PROPERTY_PATH }
public final class CompiledTargetBinding {
  public static CompiledTargetBinding targetMain(ViewKey targetViewKey, String exactResolvedValue);
  public static CompiledTargetBinding propertyPath(ViewKey targetViewKey, String exactResolvedValue);
  public ViewKey targetViewKey();
  public ResolvedTargetKind kind();
  public String exactResolvedValue();
}
public final class RuntimeBindingPlan {
  public static RuntimeBindingPlan exact(TargetKey sourceTargetKey, CompiledTargetBinding binding);
  public TargetKey sourceTargetKey();
  public CompiledTargetBinding compiledTargetBinding();
}
public final class CompiledModelAccessRule {
  public ModelAccessRuleKey key();
  public PolicyStatus policyStatus();
  public RuntimeAccessRequirement runtimeRequirement();
  public Optional<RuntimeBindingPlan> runtimeBindingPlan();
}
public interface ModelAccessPolicyIndex {
  Optional<CompiledModelAccessRule> find(ModelAccessRuleKey key);
}
```

Only `STATIC_ALLOW + NONE + no plan` and `RUNTIME_GUARD_REQUIRED + EXACT_RUNTIME_BINDING + plan` are legal published rows. Runtime never reparses selector text or scans raw definitions to reconstruct target identity.

Opaque immutable exact/case-sensitive IDs: `ProtectedInvocationId`, `RuntimeObjectId`, `RuntimeWriteIntentId`, `RuntimeExecutionFrameId`, `RuntimeResolutionOwnerId`, `RuntimeCollectionCursorId`, `RuntimeModelSessionId`. Each exposes `of(String)` and `value()` and carries no permission semantics.

`RuleKey` is owner `RuleViewKey + localRuleName` and is optional provenance only. `RuntimeMutationVersion` is a non-negative immutable long. `RuntimeFactValue` is a closed deep-immutable algebra `{NULL, BOOLEAN, INTEGER, DECIMAL, STRING, LIST, OBJECT}` with deterministic serialization.

```java
public final class ProtectedAccessInvocation {
  public ProtectedInvocationId invocationId();
  public ModelAccessRuleKey modelAccessRuleKey();
  public RuntimeExecutionFrameId frameId();
  public RuntimeResolutionOwnerId ownerResolutionId();
  public Optional<RuntimeCollectionCursorId> cursorId();
}
public interface ProtectedAccessPort { ProtectedAccessResult invoke(ProtectedAccessInvocation invocation); }
public final class RuntimeBindingProof { public String value(); }
public final class ResolvedRuntimeTarget {
  public RuntimeModelSessionId sessionId();
  public RuntimeObjectId runtimeObjectId();
  public TargetKey targetKey();
  public CompiledTargetBinding compiledTargetBinding();
  public RuntimeExecutionFrameId frameId();
  public RuntimeResolutionOwnerId ownerResolutionId();
  public Optional<RuntimeCollectionCursorId> cursorId();
  public RuntimeBindingProof bindingProof();
}
public final class RuntimeMutationStamp {
  public RuntimeModelSessionId sessionId();
  public RuntimeObjectId runtimeObjectId();
  public ModelPath modelPath();
  public RuntimeMutationVersion version();
}
public final class ResolvedProtectedReadAccess {
  public ProtectedInvocationId invocationId();
  public ModelAccessRuleKey modelAccessRuleKey();
  public ResolvedRuntimeTarget resolvedRuntimeTarget();
}
public final class ResolvedWriteIntent {
  public RuntimeWriteIntentId id();
  public ModelAccessRuleKey modelAccessRuleKey();
  public Optional<RuleKey> ruleKeyProvenance();
  public ResolvedRuntimeTarget resolvedRuntimeTarget();
  public RuntimeMutationStamp mutationStamp();
}
public final class ResolvedProtectedWriteAccess {
  public ProtectedInvocationId invocationId();
  public ResolvedWriteIntent writeIntent();
}
public interface RuntimeModelOperationPort {
  RuntimeFactValue read(ResolvedProtectedReadAccess access);
  ProtectedWriteReceipt write(ResolvedProtectedWriteAccess access);
}
public final class ProtectedReadValue {
  public ProtectedInvocationId invocationId();
  public RuntimeFactValue value();
}
public final class ProtectedWriteReceipt {
  public ProtectedInvocationId invocationId();
  public RuntimeWriteIntentId writeIntentId();
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
  public ProtectedInvocationId invocationId();
  public DenialCode code();
  public String stableMessage();
}
public final class ProtectedAccessResult {
  public boolean allowed();
  public Optional<ProtectedReadValue> readValue();
  public Optional<ProtectedWriteReceipt> writeReceipt();
  public Optional<ProtectedAccessDenial> denial();
}
```

ALLOW READ has read value only; ALLOW WRITE has receipt only; DENY has denial only.

<a id="trusted-runtime-model-materialization"></a>
## 3. MODEL trusted producer/handoff contracts

```java
public final class RuntimeModelMaterializationInput {
  public static RuntimeModelMaterializationInput of(RuntimeBindingPlan exactPlan, RuntimeFactValue sourceSnapshot);
  public RuntimeBindingPlan runtimeBindingPlan();
  public RuntimeFactValue sourceSnapshot();
}
public final class RuntimeModelFrameRequest {
  public static RuntimeModelFrameRequest of(
      RuntimeExecutionFrameId frameId,
      RuntimeResolutionOwnerId ownerId,
      Optional<RuntimeCollectionCursorId> cursorId,
      List<RuntimeModelMaterializationInput> inputs);
  public RuntimeExecutionFrameId frameId();
  public RuntimeResolutionOwnerId ownerResolutionId();
  public Optional<RuntimeCollectionCursorId> cursorId();
  public List<RuntimeModelMaterializationInput> inputs();
}
public final class RuntimeModelProvenance {
  // NO public/protected constructor/factory.
  public RuntimeBindingPlan runtimeBindingPlan();
}
public final class RuntimeModelHandle {
  // NO public/protected constructor/factory/wrap/rebind; NO public ModelData accessor.
  public RuntimeModelProvenance provenance();
}
public final class RuntimeModelFrame {
  // NO public/protected constructor/factory/rebind.
  public RuntimeExecutionFrameId frameId();
  public RuntimeResolutionOwnerId ownerResolutionId();
  public Optional<RuntimeCollectionCursorId> cursorId();
  public List<RuntimeModelHandle> handles();
}
public final class LocatedRuntimeObject {
  public RuntimeModelSessionId sessionId();
  public RuntimeObjectId runtimeObjectId();
  public RuntimeModelProvenance provenance();
}
public interface RuntimeModelSession extends AutoCloseable {
  public RuntimeModelSessionId sessionId();
  public LocatedRuntimeObject locate(ResolvedRuntimeTarget target);
  public RuntimeMutationVersion currentVersion(ResolvedRuntimeTarget target, ModelPath path);
  @Override public void close();
}
public interface RuntimeModelRuntime {
  RuntimeModelExecutionResult open(RuntimeModelFrameRequest request);
}
public final class RuntimeModelRuntimes {
  public static RuntimeModelRuntime production(EngineContext capturedEngineContext);
}
public final class RuntimeModelExecution implements AutoCloseable {
  public RuntimeModelFrame frame();
  public RuntimeModelSession session();
  @Override public void close();
}
public enum RuntimeModelOpenFailureCode {
  PLAN_NOT_IN_CAPTURED_CONTEXT,
  TARGET_VIEW_NOT_FOUND,
  SOURCE_NOT_MATERIALIZABLE,
  DUPLICATE_PLAN,
  MATERIALIZATION_FAILED
}
public final class RuntimeModelOpenFailure {
  public RuntimeModelOpenFailureCode code();
  public String stableMessage();
}
public final class RuntimeModelExecutionResult {
  public boolean opened();
  public Optional<RuntimeModelExecution> execution();
  public Optional<RuntimeModelOpenFailure> failure();
}
```

Mandatory production algorithm: each request plan must be an exact member of the captured `EngineContext`; target identity comes only from `plan.compiledTargetBinding().targetViewKey()` and its frozen binding facts; MODEL resolves that exact view from the same Context, creates a **new internal ModelData** from that view + deep-immutable source snapshot, freezes provenance+handle atomically, and only after all inputs succeed creates frame+sealed session and returns them together. Existing ModelData, `ModelData.name`, caller ViewData, list order, raw definitions, selector reparsing and legacy default `ConfigContextUtil` lookup are forbidden identity evidence. Session register/seal have no public cross-module surface.

<a id="runtime-target-resolution"></a>
## 4. STARTER production composition

```java
public enum RuntimeTargetResolutionStatus {
  RESOLVED, NOT_FOUND, AMBIGUOUS, CONTEXT_MISMATCH, PROVENANCE_MISMATCH
}
public final class RuntimeTargetResolution {
  public RuntimeTargetResolutionStatus status();
  public Optional<ResolvedRuntimeTarget> target();
  public Optional<DenialCode> denialCode();
}
public interface RuntimeTargetResolver {
  RuntimeTargetResolution resolve(RuntimeBindingPlan plan, ProtectedAccessInvocation invocation, RuntimeModelExecution execution);
}
public interface RuleProtectedAccessEntry { ProtectedAccessResult invoke(ProtectedAccessInvocation invocation); }
public interface ChangeProtectedAccessEntry { ProtectedAccessResult invoke(ProtectedAccessInvocation invocation); }
public interface CustomActionProtectedAccessEntry { ProtectedAccessResult invoke(ProtectedAccessInvocation invocation); }
public final class ProtectedAccessRuntimeFactory {
  public static ProtectedAccessRuntimeFactory production(EngineContext capturedEngineContext);
  public ProtectedAccessCompositionResult create(RuntimeModelFrameRequest frameRequest);
}
public final class ProtectedAccessCompositionResult {
  public boolean created();
  public Optional<ProtectedAccessComposition> composition();
  public Optional<RuntimeModelOpenFailure> modelOpenFailure();
}
public final class ProtectedAccessComposition implements AutoCloseable {
  public ProtectedAccessPort protectedAccessPort();
  public RuntimeExecutionFrameId frameId();
  public RuntimeResolutionOwnerId ownerResolutionId();
  public RuntimeModelSessionId runtimeModelSessionId();
  public RuleProtectedAccessEntry ruleEntry();
  public ChangeProtectedAccessEntry changeEntry();
  public CustomActionProtectedAccessEntry customActionEntry();
  @Override public void close();
}
```

`ProtectedAccessRuntimeFactory.production(exact Context)` internally obtains `RuntimeModelRuntimes.production(the same Context)`, calls `open(frameRequest)`, retains the exact returned `RuntimeModelExecution(frame+session)`, and exposes no protected port if MODEL open fails. No production overload accepts caller-injected MODEL runtime/session/frame/operation port/Guard, existing ModelData, or independent frame/owner/cursor authority. Composition close closes the same MODEL execution.

## 5. Ownership/dependency rule

Legal directions: compiler->context, model->context, starter->context+model. Forbidden: context->compiler/model/starter, model->starter, P3/P4/P6 core->starter. STARTER owns resolver/intent/capability/Guard; MODEL owns materialization/session/locator/coordination and actual READ/WRITE. Guard is the sole permission authority and precedes MODEL effect.

No production Java/TDD/risk Evidence is claimed. Same-revision specialist Review and machine closure remain required.

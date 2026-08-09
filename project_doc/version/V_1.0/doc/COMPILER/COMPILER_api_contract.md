# COMPILER P2 API Contract

> Revision: `DESIGN-P2-R25`. Base: `DESIGN-P2-R24`.
> Authoritative inputs: Overlay R04 + `BM-R20` + `FLOW-R11`.
> CrossModule projection: `P2-IMPACT-R24`.
> Status: `NEEDS_REVIEW / MACHINE_BLOCKED`.

This is the complete current P2 public/cross-module contract. Every type referenced by a public signature is defined here with owner module/package, external visibility, construction surface and accessors. Superseded Design text is not required.

## 1. Ownership / visibility matrix

| Owner | Package | Public contract |
|---|---|---|
| CONTEXT | `dec.core.context.runtime` | policy keys/plans, opaque IDs, values, invocation, resolved access, operation port, result/denial algebra |
| MODEL | `dec.core.model.runtime` | trusted `RuntimeModelFrame`/`RuntimeModelHandle` provenance, session/locator API |
| STARTER | `dec.core.starter.access` | production composition, target resolution, representative Rule/Change/CustomAction entries |

All cross-module types below are explicitly `public`. Model-owned trusted provenance objects are externally readable but deliberately have **no public/protected constructor or static wrapping factory**; only model-internal framework materialization may create them. This prevents a caller from combining valid binding A with arbitrary existing `ModelData B`.

<a id="current-api-contract"></a>
## 2. CONTEXT public neutral contracts

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
  public static ModelAccessRuleKey of(SystemKey authorizationOwnerSystemKey, TargetKey targetKey, ModelPath modelPath, AccessOperation operation);
  public SystemKey authorizationOwnerSystemKey(); public TargetKey targetKey(); public ModelPath modelPath(); public AccessOperation operation();
}
public enum PolicyStatus { STATIC_ALLOW, RUNTIME_GUARD_REQUIRED }
public enum RuntimeAccessRequirement { NONE, EXACT_RUNTIME_BINDING }
public enum ResolvedTargetKind { TARGET_MAIN, PROPERTY_PATH }
public final class CompiledTargetBinding {
  public static CompiledTargetBinding targetMain(ViewKey targetViewKey, String exactResolvedValue);
  public static CompiledTargetBinding propertyPath(ViewKey targetViewKey, String exactResolvedValue);
  public ViewKey targetViewKey(); public ResolvedTargetKind kind(); public String exactResolvedValue();
}
public final class RuntimeBindingPlan {
  public static RuntimeBindingPlan exact(TargetKey sourceTargetKey, CompiledTargetBinding binding);
  public TargetKey sourceTargetKey(); public CompiledTargetBinding compiledTargetBinding();
}
public final class CompiledModelAccessRule {
  public static CompiledModelAccessRule of(ModelAccessRuleKey key, PolicyStatus status, RuntimeAccessRequirement requirement, Optional<RuntimeBindingPlan> plan);
  public ModelAccessRuleKey key(); public PolicyStatus policyStatus(); public RuntimeAccessRequirement runtimeRequirement(); public Optional<RuntimeBindingPlan> runtimeBindingPlan();
}
public final class ModelAccessPolicyIndex {
  public static ModelAccessPolicyIndex of(Collection<CompiledModelAccessRule> rules);
  public Optional<CompiledModelAccessRule> find(ModelAccessRuleKey key);
}
```

Only `STATIC_ALLOW + NONE + no plan` and `RUNTIME_GUARD_REQUIRED + EXACT_RUNTIME_BINDING + plan` are legal. Compiler converts P1 `targetView + TargetPropertyPath(kind,value)` to `CompiledTargetBinding` exactly once. Runtime must never parse/normalize selector text or scan raw View definitions/property trees.

The following opaque IDs are each `public final`, immutable, exact/case-sensitive, reject null/blank, expose `public static of(String)`, `public String value()`, structural `equals/hashCode`, and no permission semantics:

```text
ProtectedInvocationId
RuntimeObjectId
RuntimeWriteIntentId
RuntimeExecutionFrameId
RuntimeResolutionOwnerId
RuntimeCollectionCursorId
RuntimeModelSessionId
```

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
      RuntimeExecutionFrameId frameId, RuntimeResolutionOwnerId ownerId, Optional<RuntimeCollectionCursorId> cursorId);
  public ProtectedInvocationId invocationId(); public ModelAccessRuleKey modelAccessRuleKey();
  public RuntimeExecutionFrameId frameId(); public RuntimeResolutionOwnerId ownerResolutionId();
  public Optional<RuntimeCollectionCursorId> cursorId();
}
public interface ProtectedAccessPort { ProtectedAccessResult invoke(ProtectedAccessInvocation invocation); }
public final class RuntimeBindingProof { public static RuntimeBindingProof exact(String digest); public String value(); }
public final class ResolvedRuntimeTarget {
  public static ResolvedRuntimeTarget of(RuntimeModelSessionId sessionId, RuntimeObjectId objectId, TargetKey targetKey,
      CompiledTargetBinding binding, RuntimeExecutionFrameId frameId, RuntimeResolutionOwnerId ownerId,
      Optional<RuntimeCollectionCursorId> cursorId, RuntimeBindingProof proof);
  public RuntimeModelSessionId sessionId(); public RuntimeObjectId runtimeObjectId(); public TargetKey targetKey();
  public CompiledTargetBinding compiledTargetBinding(); public RuntimeExecutionFrameId frameId();
  public RuntimeResolutionOwnerId ownerResolutionId(); public Optional<RuntimeCollectionCursorId> cursorId();
  public RuntimeBindingProof bindingProof();
}
public final class RuntimeMutationStamp {
  public static RuntimeMutationStamp of(RuntimeModelSessionId sessionId, RuntimeObjectId objectId, ModelPath path, RuntimeMutationVersion version);
  public RuntimeModelSessionId sessionId(); public RuntimeObjectId runtimeObjectId(); public ModelPath modelPath(); public RuntimeMutationVersion version();
}
public final class ResolvedProtectedReadAccess {
  public static ResolvedProtectedReadAccess of(ProtectedInvocationId id, ModelAccessRuleKey key, ResolvedRuntimeTarget target);
  public ProtectedInvocationId invocationId(); public ModelAccessRuleKey modelAccessRuleKey(); public ResolvedRuntimeTarget resolvedRuntimeTarget();
}
public final class ResolvedWriteIntent {
  public static ResolvedWriteIntent of(RuntimeWriteIntentId id, ModelAccessRuleKey key, Optional<RuleKey> provenance,
      ResolvedRuntimeTarget target, RuntimeMutationStamp stamp);
  public RuntimeWriteIntentId id(); public ModelAccessRuleKey modelAccessRuleKey(); public Optional<RuleKey> ruleKeyProvenance();
  public ResolvedRuntimeTarget resolvedRuntimeTarget(); public RuntimeMutationStamp mutationStamp();
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
  public static ProtectedWriteReceipt of(ProtectedInvocationId id, RuntimeWriteIntentId intentId, RuntimeMutationVersion committedVersion);
  public ProtectedInvocationId invocationId(); public RuntimeWriteIntentId writeIntentId(); public RuntimeMutationVersion committedVersion();
}
public enum DenialCode {
  POLICY_NOT_FOUND, POLICY_MISMATCH, RUNTIME_PLAN_MISMATCH, GUARD_UNAVAILABLE, CAPABILITY_ALREADY_CONSUMED,
  RUNTIME_CONTEXT_MISMATCH, RUNTIME_MODEL_PROVENANCE_MISMATCH, RUNTIME_TARGET_NOT_FOUND, RUNTIME_TARGET_AMBIGUOUS,
  WRITE_INTENT_NOT_FOUND, WRITE_INTENT_AMBIGUOUS, WRITE_INTENT_STALE, RUNTIME_SESSION_SCOPE_MISMATCH,
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

Closed algebra: ALLOW READ has read value only; ALLOW WRITE has receipt only; DENY has denial only. `RuntimeFactValue` is deep immutable and never exposes arbitrary live `Object` references.

<a id="trusted-runtime-model-provenance"></a>
## 3. MODEL public trusted provenance/session contracts

```java
package dec.core.model.runtime;

public final class RuntimeModelProvenance {
  // NO public/protected constructor or public static factory.
  public TargetKey sourceTargetKey();
  public CompiledTargetBinding compiledTargetBinding();
}
public final class RuntimeModelHandle {
  // NO public/protected constructor or public static factory; no public ModelData accessor.
  public RuntimeModelProvenance provenance();
}
public final class RuntimeModelFrame {
  // NO public/protected constructor or public static factory.
  public RuntimeExecutionFrameId frameId();
  public RuntimeResolutionOwnerId ownerResolutionId();
  public Optional<RuntimeCollectionCursorId> cursorId();
  public List<RuntimeModelHandle> handles();
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

`RuntimeModelProvenance`, `RuntimeModelHandle`, and `RuntimeModelFrame` are instantiated only by model-internal framework materialization in package `dec.core.model.runtime`. That internal seam creates/loads `ModelData` and freezes compiler-produced target provenance in the **same trusted materialization operation**. There is no public `wrap(ModelData, TargetKey, CompiledTargetBinding)`, no public rebind, no public setter, and no public `ModelData` accessor on the handle. Therefore a business/application caller cannot construct `valid binding A + existing ModelData B`.

`RuntimeModelFrame` also freezes frame/owner/cursor facts with its handles, so callers cannot reuse handles from frame X while independently claiming frame Y. `RuntimeModelSession.register` accepts only a trusted handle; it never accepts separate binding and ModelData arguments.

<a id="runtime-target-resolution"></a>
## 4. STARTER public composition / resolution contracts

```java
package dec.core.starter.access;

public enum RuntimeTargetResolutionStatus { RESOLVED, NOT_FOUND, AMBIGUOUS, CONTEXT_MISMATCH, PROVENANCE_MISMATCH }
public final class RuntimeTargetResolution {
  public static RuntimeTargetResolution resolved(ResolvedRuntimeTarget target);
  public static RuntimeTargetResolution denied(RuntimeTargetResolutionStatus status, DenialCode code);
  public RuntimeTargetResolutionStatus status(); public Optional<ResolvedRuntimeTarget> target(); public Optional<DenialCode> denialCode();
}
public interface RuntimeTargetResolver {
  RuntimeTargetResolution resolve(RuntimeBindingPlan plan, ProtectedAccessInvocation invocation, RuntimeModelSession session);
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
  public static ProtectedAccessRuntimeFactory production(EngineContext engineContext);
  public ProtectedAccessComposition create(RuntimeExecutionFrameSnapshot frameSnapshot);
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

`RuntimeExecutionFrameSnapshot.from(...)` derives context and handle set from the trusted model-owned frame; it does not accept a caller-supplied list or independent frame/owner/cursor values. `ProtectedAccessRuntimeFactory.production(exact EngineContext)` validates every handle's immutable `(TargetKey, CompiledTargetBinding)` provenance against a current plan in that exact Context before session registration/seal. Metadata/order/selector inference is forbidden. A handle provenance match is not permission; `ModelAccessRuleKey + PolicyIndex + Guard` remains the sole authority.

Wrong-target substitution is fail closed:

```text
trusted handle A(provenance A, internal ModelData A) + plan A -> eligible for registration
trusted handle B(provenance B, internal ModelData B) presented for plan A -> NOT_FOUND/PROVENANCE_MISMATCH
public attempt to create/rebind handle(B) with provenance A -> no legal construction surface
cross-frame handle reuse with claimed different frame -> impossible through RuntimeExecutionFrameSnapshot.from(trustedFrame)
```

## 5. Effect ownership / dependency boundary

STARTER owns composition, selection, capability and Guard. MODEL owns actual `RuntimeModelSession`, locator, coordination cell and production READ/WRITE effect. `RuntimeModelOperationPort` is the neutral CONTEXT contract implemented by MODEL and wired by STARTER. Legal direction: compiler->context; model->context; starter->context+model; context->compiler/model/starter forbidden; model->starter forbidden; P3/P4/P6 core->context allowed and ->starter forbidden.

No production Java/TDD execution is claimed. DESIGN-P2-R25 remains candidate-only until same-revision specialist Review, current risk scan and required machine Evidence complete.

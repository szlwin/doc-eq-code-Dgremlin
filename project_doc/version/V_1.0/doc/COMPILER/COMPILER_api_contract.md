# COMPILER P2 API Contract

> Revision：`DESIGN-P2-R24`。Base：`DESIGN-P2-R23`。
> Inputs：Overlay R04 + `BM-R20` + `FLOW-R10`。
> CrossModule projection：`P2-IMPACT-R23`。
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`。

This is the complete current P2 public/cross-module contract. Pre-P2 `SystemKey`, `RuleViewKey`, `ViewKey`, `EngineContext` and `CompiledModelSet` remain source-compatible. All P2-added types below use Java-8-compatible factories/value semantics; superseded Design text is not required.

<a id="current-api-contract"></a>
## 1. Compile/policy values

```java
final class RuleKey { static RuleKey of(RuleViewKey owner, String localName); RuleViewKey ownerRuleViewKey(); String localRuleName(); }
final class TargetKey { static TargetKey of(ViewKey sourceViewKey); ViewKey sourceViewKey(); }
final class ModelPath { static ModelPath of(List<String> canonicalSegments); List<String> canonicalSegments(); }
enum AccessOperation { READ, WRITE }
final class ModelAccessRuleKey {
  static ModelAccessRuleKey of(SystemKey authorizationOwnerSystemKey, TargetKey targetKey, ModelPath modelPath, AccessOperation operation);
  SystemKey authorizationOwnerSystemKey(); TargetKey targetKey(); ModelPath modelPath(); AccessOperation operation();
}
enum PolicyStatus { STATIC_ALLOW, RUNTIME_GUARD_REQUIRED }
enum RuntimeAccessRequirement { NONE, EXACT_RUNTIME_BINDING }
enum ResolvedTargetKind { TARGET_MAIN, PROPERTY_PATH }
final class CompiledTargetBinding {
  static CompiledTargetBinding targetMain(ViewKey targetViewKey, String exactResolvedValue);
  static CompiledTargetBinding propertyPath(ViewKey targetViewKey, String exactResolvedValue);
  ViewKey targetViewKey(); ResolvedTargetKind kind(); String exactResolvedValue();
}
final class RuntimeBindingPlan {
  static RuntimeBindingPlan exact(TargetKey sourceTargetKey, CompiledTargetBinding compiledTargetBinding);
  TargetKey sourceTargetKey(); CompiledTargetBinding compiledTargetBinding();
}
final class CompiledModelAccessRule {
  static CompiledModelAccessRule of(ModelAccessRuleKey key, PolicyStatus status, RuntimeAccessRequirement requirement, Optional<RuntimeBindingPlan> plan);
  ModelAccessRuleKey key(); PolicyStatus policyStatus(); RuntimeAccessRequirement runtimeRequirement(); Optional<RuntimeBindingPlan> runtimeBindingPlan();
}
final class ModelAccessPolicyIndex {
  static ModelAccessPolicyIndex of(Collection<CompiledModelAccessRule> rules);
  Optional<CompiledModelAccessRule> find(ModelAccessRuleKey key);
}
```

Only `STATIC_ALLOW+NONE+no-plan` and `RUNTIME_GUARD_REQUIRED+EXACT_RUNTIME_BINDING+plan` are legal. `TargetKey` preserves the P1 shared source `ViewKey`; owner System and `ModelPath` remain separate axes.

Compiler adapts P1 `targetView:ViewKey + TargetPropertyPath(kind,value)` exactly once into `CompiledTargetBinding`. `SystemViewSelector` is compiler-only lexical input. `exactResolvedValue` is the resolved canonical P1 target value, not raw selector text. Runtime MUST NOT parse/trim/normalize selector syntax, scan raw definitions/View property trees, or reconstruct this meaning. Context owns only the neutral compiled value and never depends on compiler-only selector/path classes.

## 2. Runtime IDs/value domain

`ProtectedInvocationId`, `RuntimeObjectId`, `RuntimeWriteIntentId`, `RuntimeExecutionFrameId`, `RuntimeResolutionOwnerId`, `RuntimeCollectionCursorId`, and `RuntimeModelSessionId` are final immutable nonblank opaque String wrappers with `of(String)`, `value()`, exact case-sensitive equality/hash and no permission semantics.

```java
final class RuntimeMutationVersion { static RuntimeMutationVersion of(long nonNegative); long value(); }
final class RuntimeFactValue {
  enum Kind { NULL, BOOLEAN, INTEGER, DECIMAL, STRING, LIST, OBJECT }
  static RuntimeFactValue nullValue(); static RuntimeFactValue ofBoolean(boolean v); static RuntimeFactValue ofInteger(BigInteger v);
  static RuntimeFactValue ofDecimal(BigDecimal v); static RuntimeFactValue ofString(String v); static RuntimeFactValue ofList(List<RuntimeFactValue> v);
  static RuntimeFactValue ofObject(Map<String,RuntimeFactValue> v); Kind kind(); String deterministicJson();
}
```

`RuntimeFactValue` is a deep immutable canonical snapshot; arbitrary live `Object` values are forbidden.

## 3. Invocation and unique target resolution

```java
final class ProtectedAccessInvocation {
  static ProtectedAccessInvocation of(ProtectedInvocationId id, ModelAccessRuleKey key, RuntimeExecutionFrameId frameId,
                                      RuntimeResolutionOwnerId ownerId, Optional<RuntimeCollectionCursorId> cursorId);
  ProtectedInvocationId invocationId(); ModelAccessRuleKey modelAccessRuleKey(); RuntimeExecutionFrameId frameId();
  RuntimeResolutionOwnerId ownerResolutionId(); Optional<RuntimeCollectionCursorId> cursorId();
}
interface ProtectedAccessPort { ProtectedAccessResult invoke(ProtectedAccessInvocation invocation); }
final class RuntimeBindingProof { static RuntimeBindingProof exact(String digest); String value(); }
final class ResolvedRuntimeTarget {
  static ResolvedRuntimeTarget of(RuntimeModelSessionId sessionId, RuntimeObjectId objectId, TargetKey targetKey,
                                  CompiledTargetBinding binding, RuntimeExecutionFrameId frameId,
                                  RuntimeResolutionOwnerId ownerId, Optional<RuntimeCollectionCursorId> cursorId,
                                  RuntimeBindingProof proof);
  RuntimeModelSessionId sessionId(); RuntimeObjectId runtimeObjectId(); TargetKey targetKey();
  CompiledTargetBinding compiledTargetBinding(); RuntimeBindingProof bindingProof();
}
interface RuntimeTargetResolver { RuntimeTargetResolution resolve(RuntimeBindingPlan plan, ProtectedAccessInvocation invocation, RuntimeModelSession session); }
```

Bridge first requires invocation frame/owner == composition frame/owner. `RuntimeTargetResolver` is the only selector and exact-matches both plan `sourceTargetKey` and `compiledTargetBinding` against sealed-session typed registration facts. 0/N/context mismatch fail closed. ModelData name, ViewData, list order, raw definitions, selector reparse and first-match fallbacks are forbidden.

<a id="runtime-model-session"></a>
## 4. RuntimeModelSession and WRITE binding

```java
public interface RuntimeModelSession extends AutoCloseable {
  RuntimeModelSessionId sessionId();
  RuntimeObjectId register(RuntimeExecutionFrameId frameId, RuntimeResolutionOwnerId ownerId,
                           Optional<RuntimeCollectionCursorId> cursorId, TargetKey sourceTargetKey,
                           CompiledTargetBinding binding, ModelData modelData);
  void seal(); LocatedRuntimeObject locate(ResolvedRuntimeTarget target);
  RuntimeMutationVersion currentVersion(ResolvedRuntimeTarget target, ModelPath path);
}
final class RuntimeMutationStamp {
  static RuntimeMutationStamp of(RuntimeModelSessionId sessionId, RuntimeObjectId objectId, ModelPath path, RuntimeMutationVersion version);
  RuntimeModelSessionId sessionId(); RuntimeObjectId runtimeObjectId(); ModelPath modelPath(); RuntimeMutationVersion version();
}
final class ResolvedProtectedReadAccess {
  static ResolvedProtectedReadAccess of(ProtectedInvocationId id, ModelAccessRuleKey key, ResolvedRuntimeTarget target);
  ProtectedInvocationId invocationId(); ModelAccessRuleKey modelAccessRuleKey(); ResolvedRuntimeTarget resolvedRuntimeTarget();
}
final class ResolvedWriteIntent {
  static ResolvedWriteIntent of(RuntimeWriteIntentId id, ModelAccessRuleKey key, Optional<RuleKey> provenance,
                                ResolvedRuntimeTarget target, RuntimeMutationStamp stamp);
  RuntimeWriteIntentId id(); ModelAccessRuleKey modelAccessRuleKey(); Optional<RuleKey> ruleKeyProvenance();
  ResolvedRuntimeTarget resolvedRuntimeTarget(); RuntimeMutationStamp mutationStamp();
}
final class ResolvedProtectedWriteAccess {
  static ResolvedProtectedWriteAccess of(ProtectedInvocationId id, ResolvedWriteIntent intent);
  ProtectedInvocationId invocationId(); ResolvedWriteIntent writeIntent();
}
interface RuntimeModelOperationPort { RuntimeFactValue read(ResolvedProtectedReadAccess access); ProtectedWriteReceipt write(ResolvedProtectedWriteAccess access); }
```

WRITE intent is 0/1/N before capability/Guard. Exactly one freezes a stamp whose session/object equal the target and whose path equals `ModelAccessRuleKey.modelPath`; no second object/path/version authority exists.

<a id="runtime-registration-provenance"></a>
## 5. Production registration provenance/composition

```java
final class RuntimeModelRegistrationInput {
  static RuntimeModelRegistrationInput of(TargetKey sourceTargetKey, CompiledTargetBinding binding, ModelData modelData);
  TargetKey sourceTargetKey(); CompiledTargetBinding compiledTargetBinding(); ModelData modelData();
}
enum RuntimeCompositionErrorCode { REGISTRATION_BINDING_NOT_IN_CONTEXT, REGISTRATION_DUPLICATE_INPUT, REGISTRATION_MODEL_OWNERSHIP_CONFLICT }
final class RuntimeCompositionException extends IllegalStateException { RuntimeCompositionErrorCode code(); }
final class RuntimeExecutionFrameSnapshot {
  static RuntimeExecutionFrameSnapshot of(RuntimeExecutionFrameId frameId, RuntimeResolutionOwnerId ownerId,
                                          Optional<RuntimeCollectionCursorId> cursorId,
                                          List<RuntimeModelRegistrationInput> registrations);
  RuntimeExecutionFrameId frameId(); RuntimeResolutionOwnerId ownerResolutionId();
  Optional<RuntimeCollectionCursorId> cursorId(); List<RuntimeModelRegistrationInput> runtimeModelRegistrations();
}
final class ProtectedAccessRuntimeFactory {
  static ProtectedAccessRuntimeFactory production(EngineContext engineContext);
  ProtectedAccessComposition create(RuntimeExecutionFrameSnapshot frameSnapshot);
}
final class ProtectedAccessComposition implements AutoCloseable {
  ProtectedAccessPort protectedAccessPort(); RuntimeExecutionFrameId frameId(); RuntimeResolutionOwnerId ownerResolutionId();
  RuntimeModelSessionId runtimeModelSessionId(); RuleProtectedAccessEntry ruleEntry(); ChangeProtectedAccessEntry changeEntry();
  CustomActionProtectedAccessEntry customActionEntry();
}
```

`RuntimeModelRegistrationInput` is starter-owned production/internal assembly data, never a business-caller permission API. `production(engineContext)` captures the exact immutable Context. Before session registration/seal, `create(...)` MUST prove each `(sourceTargetKey,binding)` is an exact current `RuntimeBindingPlan` pair in that Context. Missing/duplicate/ownership-conflicting input fails composition before resolver/capability/Guard/model effect. Binding must never be inferred from `ModelData.getName()`, `ViewData`, list order, raw XML/YAML/definitions, selector parsing or a global mutable map. A valid registration proves provenance only; READ/WRITE authority remains exclusively `ModelAccessRuleKey + ModelAccessPolicyIndex + Guard`.

## 6. Results/failure contract

```java
enum DenialCode { POLICY_NOT_FOUND, POLICY_MISMATCH, RUNTIME_PLAN_MISMATCH, GUARD_UNAVAILABLE, CAPABILITY_ALREADY_CONSUMED,
  RUNTIME_CONTEXT_MISMATCH, RUNTIME_TARGET_NOT_FOUND, RUNTIME_TARGET_AMBIGUOUS, WRITE_INTENT_NOT_FOUND,
  WRITE_INTENT_AMBIGUOUS, WRITE_INTENT_STALE, RUNTIME_SESSION_SCOPE_MISMATCH, RUNTIME_OBJECT_NOT_FOUND,
  RUNTIME_OBJECT_STALE, RUNTIME_OBJECT_ALREADY_REGISTERED, RUNTIME_OBJECT_OWNERSHIP_CONFLICT, RUNTIME_WRITE_FAILED }
final class ProtectedAccessResult {
  static ProtectedAccessResult allowRead(ProtectedReadValue v); static ProtectedAccessResult allowWrite(ProtectedWriteReceipt r);
  static ProtectedAccessResult deny(ProtectedAccessDenial d); boolean allowed();
  Optional<ProtectedReadValue> readValue(); Optional<ProtectedWriteReceipt> writeReceipt(); Optional<ProtectedAccessDenial> denial();
}
```

Closed algebra: ALLOW READ -> value only; ALLOW WRITE -> receipt only; DENY -> denial only. Stale/write failure after capability consume leaves model state unchanged but capability CONSUMED.

## 7. P2/P7 boundary and Gate

RuntimeModelSession/registration lease/per-path coordination/one protected WRITE transaction are P2 internal execution seams, not P7 user/session lifecycle or cross-request transaction ownership.

No production Java/TDD execution is claimed. DESIGN-P2-R24 remains candidate-only until same-revision ApiContract/Architecture/Develop/Impact/CrossModule/Concurrency Reviews, current risk scan and required machine Evidence complete.

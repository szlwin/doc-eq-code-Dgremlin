# COMPILER P2 API Contract

> Revision：`DESIGN-P2-R23`。Base：`DESIGN-P2-R22`。
> Inputs：Overlay R04 + `BM-R20` + `FLOW-R10`。
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`。

This document is the complete current-revision P2 contract. DESIGN-P2-R23 changes only the RuntimeBindingPlan representation so runtime consumes compiler-resolved target semantics without lexical re-interpretation. A production implementer does not need superseded R19/R20/R21 design text to discover a P2 constructor, factory, value type, resolver, operation port, result or composition dependency.

<a id="current-api-contract"></a>
## 1. Existing stable compatibility boundary

The pre-P2 `SystemKey`, `RuleViewKey`, `ViewKey`, `EngineContext` and `CompiledModelSet` remain source-compatible. P2 does not redefine their legacy constructors. Every P2-added cross-module immutable type is frozen below with an explicit Java-8-compatible construction surface.

```java
public final class RuleKey {
    public static RuleKey of(RuleViewKey ownerRuleViewKey, String localRuleName);
    public RuleViewKey ownerRuleViewKey();
    public String localRuleName();
}

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
    public static ModelAccessRuleKey of(SystemKey authorizationOwnerSystemKey,
                                        TargetKey targetKey,
                                        ModelPath modelPath,
                                        AccessOperation operation);
    public SystemKey authorizationOwnerSystemKey();
    public TargetKey targetKey();
    public ModelPath modelPath();
    public AccessOperation operation();
}
```

`TargetKey` preserves the P1 shared `ViewKey` source identity. Owner System, source ModelPath and local target binding facts are separate.

## 2. Policy and runtime-binding contracts

```java
public enum PolicyStatus {
    STATIC_ALLOW,
    RUNTIME_GUARD_REQUIRED
}

public enum RuntimeAccessRequirement {
    NONE,
    EXACT_RUNTIME_BINDING
}

public enum ResolvedTargetKind {
    TARGET_MAIN,
    PROPERTY_PATH
}

public final class CompiledTargetBinding {
    public static CompiledTargetBinding targetMain(ViewKey targetViewKey,
                                                    String exactResolvedValue);
    public static CompiledTargetBinding propertyPath(ViewKey targetViewKey,
                                                      String exactResolvedValue);
    public ViewKey targetViewKey();
    public ResolvedTargetKind kind();
    public String exactResolvedValue();
}

public final class RuntimeBindingPlan {
    public static RuntimeBindingPlan exact(TargetKey sourceTargetKey,
                                           CompiledTargetBinding compiledTargetBinding);
    public TargetKey sourceTargetKey();
    public CompiledTargetBinding compiledTargetBinding();
}

public final class CompiledModelAccessRule {
    public static CompiledModelAccessRule of(ModelAccessRuleKey key,
                                             PolicyStatus policyStatus,
                                             RuntimeAccessRequirement runtimeRequirement,
                                             Optional<RuntimeBindingPlan> runtimeBindingPlan);
    public ModelAccessRuleKey key();
    public PolicyStatus policyStatus();
    public RuntimeAccessRequirement runtimeRequirement();
    public Optional<RuntimeBindingPlan> runtimeBindingPlan();
}

public final class ModelAccessPolicyIndex {
    public static ModelAccessPolicyIndex of(Collection<CompiledModelAccessRule> rules);
    public Optional<CompiledModelAccessRule> find(ModelAccessRuleKey key);
}
```

Only these rows are legal:

```text
STATIC_ALLOW           + NONE                  + no plan
RUNTIME_GUARD_REQUIRED + EXACT_RUNTIME_BINDING + plan
```

Construction and `ModelAccessPolicyIndex.of(...)` reject every other tuple. Runtime never repairs or widens it.

Compiler adaptation is one-way and lossless for the P1 facts relevant to runtime selection:

```text
P1 ModelAccessBinding.targetView : ViewKey
P1 SystemViewSelector            : compiler-only lexical input
P1 TargetPropertyPath            : Kind(TARGET_MAIN|PROPERTY_PATH) + exact resolved value
        |
        | compiler performs the one and only selector resolution
        v
CompiledTargetBinding(
    targetViewKey,
    ResolvedTargetKind,
    exactResolvedValue)
        |
        v
RuntimeBindingPlan(sourceTargetKey, compiledTargetBinding)
```

`exactResolvedValue` is the compiler-produced canonical resolved value copied from the resolved P1 target fact; it is **not** the raw XML/YAML selector expression. Runtime code must not parse/trim/normalize selector text, re-scan a View property tree, or consult raw definitions to reconstruct the target. `dec-core-context` owns only the neutral compiled value above and has no dependency on compiler-only `SystemViewSelector` / `TargetPropertyPath` classes.

## 3. Opaque IDs and mutation version

All String wrappers below are final, immutable, exact/case-sensitive, reject null/blank, expose `of(String)` + `value()` + structural `equals/hashCode`, and carry no permission semantics:

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
public final class RuntimeMutationVersion {
    public static RuntimeMutationVersion of(long nonNegativeValue);
    public long value();
}
```

`RuntimeModelSessionId` is an explicit scope fact. It is not parsed out of `RuntimeObjectId`.

## 4. RuntimeFactValue

```java
public final class RuntimeFactValue {
    public enum Kind { NULL, BOOLEAN, INTEGER, DECIMAL, STRING, LIST, OBJECT }

    public static RuntimeFactValue nullValue();
    public static RuntimeFactValue ofBoolean(boolean value);
    public static RuntimeFactValue ofInteger(BigInteger value);
    public static RuntimeFactValue ofDecimal(BigDecimal value);
    public static RuntimeFactValue ofString(String value);
    public static RuntimeFactValue ofList(List<RuntimeFactValue> values);
    public static RuntimeFactValue ofObject(Map<String, RuntimeFactValue> values);

    public Kind kind();
    public Optional<Boolean> booleanValue();
    public Optional<BigInteger> integerValue();
    public Optional<BigDecimal> decimalValue();
    public Optional<String> stringValue();
    public Optional<List<RuntimeFactValue>> listValue();
    public Optional<Map<String, RuntimeFactValue>> objectValue();
    public String deterministicJson();
}
```

It recursively snapshots input, normalizes numbers, returns immutable collections, provides structural equality/hash, and never exposes an arbitrary raw `Object` or live runtime reference.

## 5. Invocation and composition-bound execution context

```java
public final class ProtectedAccessInvocation {
    public static ProtectedAccessInvocation of(ProtectedInvocationId invocationId,
                                                ModelAccessRuleKey modelAccessRuleKey,
                                                RuntimeExecutionFrameId frameId,
                                                RuntimeResolutionOwnerId ownerResolutionId,
                                              Optional<RuntimeCollectionCursorId> cursorId);
    public ProtectedInvocationId invocationId();
    public ModelAccessRuleKey modelAccessRuleKey();
    public RuntimeExecutionFrameId frameId();
    public RuntimeResolutionOwnerId ownerResolutionId();
    public Optional<RuntimeCollectionCursorId> cursorId();
}

public interface ProtectedAccessPort {
    ProtectedAccessResult invoke(ProtectedAccessInvocation invocation);
}
```

Before target selection, Bridge MUST compare `invocation.frameId/ownerResolutionId` with the composition-bound frame/owner. Mismatch returns `RUNTIME_CONTEXT_MISMATCH`; resolver, capability, Guard and model operation are not invoked.

The business caller supplies no `RuleKey`, `RuntimeObjectId`, `RuntimeModelSessionId`, raw callback, Gateway/Guard, model operation port or mutable PolicyIndex.

<a id="runtime-target-resolution"></a>
## 6. Unique runtime-target selection

```java
public final class RuntimeBindingProof {
    public static RuntimeBindingProof exact(String nonSensitiveDigest);
    public String value();
}

public final class ResolvedRuntimeTarget {
    public static ResolvedRuntimeTarget of(RuntimeModelSessionId sessionId,
                                                   RuntimeObjectId runtimeObjectId,
                                                   TargetKey targetKey,
                                                   CompiledTargetBinding compiledTargetBinding,
                                                   RuntimeExecutionFrameId frameId,
                                                   RuntimeResolutionOwnerId ownerResolutionId,
                                                 Optional<RuntimeCollectionCursorId> cursorId,
                                                    RuntimeBindingProof bindingProof);
    public RuntimeModelSessionId sessionId();
    public RuntimeObjectId runtimeObjectId();
    public TargetKey targetKey();
    public CompiledTargetBinding compiledTargetBinding();
    public RuntimeExecutionFrameId frameId();
    public RuntimeResolutionOwnerId ownerResolutionId();
    public Optional<RuntimeCollectionCursorId> cursorId();
    public RuntimeBindingProof bindingProof();
}

interface RuntimeTargetResolver {
    RuntimeTargetResolution resolve(RuntimeBindingPlan plan,
                                      ProtectedAccessInvocation invocation,
                                      RuntimeModelSession session);
}

final class RuntimeTargetResolution {
    enum Kind { RESOLVED, NOT_FOUND, AMBIGUOUS, CONTEXT_MISMATCH }
    Kind kind();
    Optional<ResolvedRuntimeTarget> target();
}
```

`RuntimeTargetResolver` is the only legal selection algorithm. It exact-matches `plan.compiledTargetBinding()` against the compiler-produced `CompiledTargetBinding` recorded with sealed-session runtime registrations, together with the composition-bound typed execution facts. It returns exactly one immutable target or a deterministic 0/N/context failure. It MUST NOT read raw XML/YAML/definition text, invoke `SystemViewSelector` parsing, scan the View property tree, trim/normalize selector values, or use “first object”, ModelData-name, frame-only or other alternate selection.

#a id="runtime-model-session"></a>
## 7. RuntimeModelSession, scope and actual-model ownership

`RuntimeModelSession` is `dec-core-model` production/internal assembly API, not a business caller API:

```java
public interface RuntimeModelSession implements AutoCloseable {
    RuntimeModelSessionId sessionId();

    RuntimeObjectId register(RuntimeExecutionFrameId frameId,
                                RuntimeResolutionOwnerId ownerResolutionId,
                                Optional<RuntimeCollectionCursorId> cursorId,
                                CompiledTargetBinding compiledTargetBinding,
                                ModelData modelData); // assembly phase only

    void seal();

    LocatedRuntimeObject locate(ResolvedRuntimeTarget target);

    RuntimeMutationVersion currentVersion(ResolvedRuntimeTarget target,
                                            ModelPath path);
}
```

Registration rules are mandatory:

- one actual `ModelData`/runtime handle has exactly one model-internal `RuntimeModelCoordinationCell`;
- the coordination cell owns one active-session registration lease plus per-`ModelPath` lock/version state;
- duplicate registration of the same actual handle in one active session -> `RUNTIME_OBJECT_ALREADY_REGISTERED`;
- registration of the same actual handle in another active session -> `RUNTIME_OBJECT_OWNERSHIP_CONFLICT` ;
- session close releases the active lease but does not create a second version domain; version state remains bound to the actual model coordination cell.

Lookup classification:

- explicit target sessionId  != current sessionId -> `RUNTIME_SESSION_SCOPE_MISMATCH`;
- same active session, IDs never registered -> `RUNTIME_OBJECT_NOT_FOUND`;
- IDs previously registered in the current session but session/binding closed/revoked -> `RUNTIME_OBJECT_STALE`

An opaque ID from another session that has no explicit current-scope proof is not parsed or looked up globally; it is just not a member of the current scope and therefore `NOT_FOUND`.

## 8. Atomic WRITE target/version binding

```java
public final class RuntimeMutationStamp {
    public static RuntimeMutationStamp of(RuntimeModelSessionId sessionId,
                                          RuntimeObjectId runtimeObjectId,
                                          ModelPath modelPath,
                                          RuntimeMutationVersion version);
    public RuntimeModelSessionId sessionId();
    public RuntimeObjectId runtimeObjectId();
    public ModelPath modelPath();
    public RuntimeMutationVersion version();
}

public final class ResolvedProtectedReadAccess {
    public static ResolvedProtectedReadAccess of(ProtectedInvocationId invocationId,
                                               ModelAccessRuleKey modelAccessRuleKey,
                                                 ResolvedRuntimeTarget resolvedRuntimeTarget);
    public ProtectedInvocationId invocationId();
    public ModelAccessRuleKey modelAccessRuleKey();
    public ResolvedRuntimeTarget resolvedRuntimeTarget();
}

public final class ResolvedWriteIntent {
    public static ResolvedWriteIntent of(RuntimeWriteIntentId id,
                                         ModelAccessRuleKey modelAccessRuleKey,
                                         Optional<RuleKey> ruleKeyProvenance,
                                         ResolvedRuntimeTarget resolvedRuntimeTarget,
                                         RuntimeMutationStamp mutationStamp);
    public RuntimeWriteIntentId id();
    public ModelAccessRuleKey modelAccessRuleKey();
    public Optional<RuleKey> ruleKeyProvenance();
    public ResolvedRuntimeTarget resolvedRuntimeTarget();
    public RuntimeMutationStamp mutationStamp();
}

public final class ResolvedProtectedWriteAccess {
    public static ResolvedProtectedWriteAccess of(ProtectedInvocationId invocationId,
                                                  ResolvedWriteIntent writeIntent);
    public ProtectedInvocationId invocationId();
    public ResolvedWriteIntent writeIntent();
}
```

`ResolvedWriteIntent.of(...)` MUST reject unless:

```text
key.operation == WRITE
stamp.sessionId == resolvedTarget.sessionId
stamp.runtimeObjectId == resolvedTarget.runtimeObjectId
stamp.modelPath == key.modelPath
```

Thus object/path/version cannot be assembled from different targets. `ResolvedProtectedWriteAccess` contains no second RuntimeObjectId, ModelPath or version.

Starter-owned WRITE resolution is:

```java
interface WriteIntentResolver {
    WriteIntentResolution resolve(ModelAccessRuleKey key,
                                  ResolvedRuntimeTarget target,
                                  Optional<RuleKey> ruleKeyProvenance);
}
```

Zero candidate -> `WRITE_INTENT_NOT_FOUND`; multiple -> `WRITE_INTENT_AMBIGUOUS`; exactly one freezes the current `RuntimeMutationStamp` before capability/Guard. No post-Guard reselection is permitted.

## 9. Production model operation port

Neutral context contract:

```java
public interface RuntimeModelOperationPort {
    RuntimeFactValue read(ResolvedProtectedReadAccess access);
    ProtectedWriteReceipt write(ResolvedProtectedWriteAccess access);
}
```

No overload accepts a second object/path/version. Production implementation belongs to `dec-core-model`; callers cannot inject or replace it after Guard.

## 10. Result algebra and denials

```java
public final class ProtectedReadValue {
    public static ProtectedReadValue of(ProtectedInvocationId invocationId,
                                        ResolvedRuntimeTarget target,
                                        ModelAccessRuleKey key,
                                        RuntimeFactValue value);
    public ProtectedInvocationId invocationId();
    public ResolvedRuntimeTarget target();
    public ModelAccessRuleKey modelAccessRuleKey();
    public RuntimeFactValue value();
}

public final class ProtectedWriteReceipt {
    public static ProtectedWriteReceipt of(ProtectedInvocationId invocationId,
                                           ResolvedWriteIntent intent,
                                           RuntimeMutationVersion committedVersion);
    public ProtectedInvocationId invocationId();
    public ResolvedWriteIntent intent();
    public RuntimeMutationVersion committedVersion();
}

public enum DenialCode {
    POLICY_NOT_FOUND,
    POLICY_MISMATCH,
    RUNTIME_PLAN_MISMATCH,
    GUARD_UNAVAILABLE,
    CAPABILITY_ALREADY_CONSUMED,
    RUNTIME_CONTEXT_MISMATCH,
    RUNTIME_TARGET_NOT_FOUND,
    RUNTIME_TARGET_AMBIGUOUS,
    WRITE_INTENT_NOT_FOUND,
    WRITE_INTENT_AMBIGUOUS,
    WRITE_INTENT_STALE,
    RUNTIME_SESSION_SCOPE_MISMATCH,
    RUNTIME_OBJECT_NOT_FOUND,
    RUNTIME_OBJECT_STALE,
    RUNTIME_OBJECT_ALREADY_REGISTERED,
    RUNTIME_OBJECT_OWNERSHIP_CONFLICT,
    RUNTIME_WRITE_FAILED
}

public final class ProtectedAccessDenial {
    public static ProtectedAccessDenial of(DenialCode code, String nonSensitiveMessage);
    public DenialCode code();
    public String nonSensitiveMessage();
}

public final class ProtectedAccessResult {
    public static ProtectedAccessResult allowRead(ProtectedReadValue value);
    public static ProtectedAccessResult allowWrite(ProtectedWriteReceipt receipt);
    public static ProtectedAccessResult deny(ProtectedAccessDenial denial);

    public boolean allowed();
    public Optional<ProtectedReadValue> readValue();
    public Optional<ProtectedWriteReceipt> writeReceipt();
    public Optional<ProtectedAccessDenial> denial();
}
```

Closed algebra:

```text
ALLOW READ  -> readValue only
ALLOW WRITE -> writeReceipt only
DENY        -> denial only
```

WRITE stale/operation failure occurs after capability consume, so `modelStateChanged=false` but `capabilityStateChanged=true`.

## 11. Explicit production composition root

```java
public final class RuntimeExecutionFrameSnapshot {
    public static RuntimeExecutionFrameSnapshot of(
        RuntimeExecutionFrameId frameId,
        RuntimeResolutionOwnerId ownerResolutionId,
        Optional<RuntimeCollectionCursorId> cursorId,
        List<ModelData> runtimeModels);

    public RuntimeExecutionFrameId frameId();
    public RuntimeResolutionOwnerId ownerResolutionId();
    public Optional<RuntimeCollectionCursorId> cursorId();
    public List<ModelData> runtimeModels(); // immutable assembly snapshot
}

public final class ProtectedAccessRuntimeFactory {
    public static ProtectedAccessRuntimeFactory production(EngineContext engineContext);
    public ProtectedAccessComposition create(RuntimeExecutionFrameSnapshot frameSnapshot);
}

public final class ProtectedAccessComposition implements AutoCloseable {
    public ProtectedAccessPort protectedAccessPort();
    public RuntimeExecutionFrameId frameId();
    public RuntimeResolutionOwnerId ownerResolutionId();
    public RuntimeModelSessionId runtimeModelSessionId();
    public RuleProtectedAccessEntry ruleEntry();
    public ChangeProtectedAccessEntry changeEntry();
    public CustomActionProtectedAccessEntry customActionEntry();
}
```

`production(engineContext)` captures the exact immutable Context explicitly. `create(frameSnapshot)` supplies the exact runtime handles and execution identity explicitly. Starter internally constructs RuntimeModelSession, RuntimeTargetResolver, model adapter, Gateway and Guard. Neither method reads a global/default current Context.

## 12. P2/P7 boundary

`RuntimeModelSession`, its active registration lease, per-path coordination/version and one protected-WRITE transaction are **P2 internal authorization/execution seams only**. P2 does not define:

- user/session lifecycle;
- cross-request transactions;
- general resource ownership/lifetime;
- P7 declaration/session convergence.

Those remain P7 scope.

## 13. Gate

No production Java/TDD execution is claimed. `DESIGN-P2-R23` remains candidate-only until same-revision ApiContract/Architecture/Develop/Impact/CrossModule/Concurrency Reviews, current risk scan and required machine Evidence complete.

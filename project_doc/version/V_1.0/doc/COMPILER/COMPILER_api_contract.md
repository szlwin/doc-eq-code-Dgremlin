# COMPILER P2 API Contract

> Revision：`DESIGN-P2-R21`。Base：`DESIGN-P2-R20`。
> Inputs：Overlay R04 + `BM-R19` + `FLOW-R09`。
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`。

This document is the complete current-revision API contract. No superseded design revision is required to reconstruct P2 interfaces.

## 1. Key and policy APIs

```java
public final class TargetKey {
    public static TargetKey of(ViewKey sourceViewKey);
    public ViewKey sourceViewKey();
}

public enum AccessOperation { READ, WRITE }

public final class ModelAccessRuleKey {
    public SystemKey authorizationOwnerSystemKey();
    public TargetKey targetKey();
    public ModelPath modelPath();
    public AccessOperation operation();
}

public final class CompiledModelAccessRule {
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

PolicyIndex accepts only the two frozen legal classification rows and never repairs malformed input.

## 2. Typed runtime IDs

The following are final immutable exact/case-sensitive nonblank value wrappers with `of(String)`, `value()`, `equals/hashCode`; they carry no permission semantics:

```text
ProtectedInvocationId
RuntimeObjectId
RuntimeWriteIntentId
RuntimeExecutionFrameId
RuntimeResolutionOwnerId
RuntimeCollectionCursorId
```

`RuntimeMutationVersion` is a non-negative immutable long value scoped to one RuntimeModelSession + RuntimeObjectId + ModelPath.

## 3. Invocation

```java
public final class ProtectedAccessInvocation {
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

The caller supplies no `RuleKey`, RuntimeObjectId, raw operation callback, Gateway/Guard, RuntimeModelOperationPort or mutable PolicyIndex.

## 4. Resolved READ / WRITE

```java
public final class ResolvedProtectedReadAccess {
    public ProtectedInvocationId invocationId();
    public ModelAccessRuleKey modelAccessRuleKey(); // operation == READ
    public RuntimeObjectId runtimeObjectId();
    public RuntimeExecutionFrameId frameId();
    public RuntimeResolutionOwnerId ownerResolutionId();
    public Optional<RuntimeCollectionCursorId> cursorId();
}

public final class ResolvedWriteIntent {
    public RuntimeWriteIntentId id();
    public ModelAccessRuleKey modelAccessRuleKey(); // operation == WRITE; sole target/path authority
    public Optional<RuleKey> ruleKeyProvenance();
    public RuntimeExecutionFrameId frameId();
    public RuntimeResolutionOwnerId ownerResolutionId();
    public Optional<RuntimeCollectionCursorId> cursorId();
    public RuntimeMutationVersion expectedMutationVersion();
}

public final class ResolvedProtectedWriteAccess {
    public ProtectedInvocationId invocationId();
    public RuntimeObjectId runtimeObjectId();
    public ResolvedWriteIntent writeIntent();
}
```

Invariant: `ResolvedProtectedWriteAccess` contains no second ModelPath. The path used by Guard and mutation is exactly `writeIntent().modelAccessRuleKey().modelPath()`.

## 5. WriteIntentResolver

```java
interface WriteIntentResolver {
    WriteIntentResolution resolve(
        ModelAccessRuleKey key,
        RuntimeExecutionFrameId frameId,
        RuntimeResolutionOwnerId ownerResolutionId,
        Optional<RuntimeCollectionCursorId> cursorId);
}
```

0 candidate => `WRITE_INTENT_NOT_FOUND`; N>1 => `WRITE_INTENT_AMBIGUOUS`; exactly one freezes the typed context + expected mutation version before Guard. `RuleKey` can only be optional provenance.

## 6. RuntimeFactValue

```java
public final class RuntimeFactValue {
    public enum Kind { NULL, BOOLEAN, INTEGER, DECIMAL, STRING, LIST, OBJECT }
    public Kind kind();
    // typed immutable accessors only; no arbitrary Object accessor
}
```

Recursive deep snapshot; `BigInteger`; normalized `BigDecimal`; ordered immutable LIST; deterministic immutable OBJECT; structural equality/hash; deterministic JSON; no live mutable reference.

## 7. Production model operation port

Neutral context contract:

```java
public interface RuntimeModelOperationPort {
    RuntimeFactValue read(ResolvedProtectedReadAccess access);
    ProtectedWriteReceipt write(ResolvedProtectedWriteAccess access);
}
```

There is deliberately no overload with a separate `ModelPath`. Production implementation is supplied by `dec-core-model`; business callers never receive or inject the port.

## 8. RuntimeModelSession / locator

`dec-core-model` current implementation contract:

```java
public interface RuntimeModelSession extends AutoCloseable {
    RuntimeObjectId register(RuntimeExecutionFrameId frameId, ModelData modelData); // assembly phase only
    void seal();
    LocatedRuntimeObject locate(RuntimeObjectId objectId, RuntimeExecutionFrameId frameId);
    RuntimeMutationVersion currentVersion(RuntimeObjectId objectId, ModelPath path);
}
```

After `seal()`, registration/replacement is forbidden. The session is owned by one production composition/frame and is never static/global. `LocatedRuntimeObject` is model-internal/assembly-internal and is not exposed through `ProtectedAccessPort`.

## 9. Results

```java
public final class ProtectedReadValue {
    public ProtectedInvocationId invocationId();
    public RuntimeObjectId runtimeObjectId();
    public ModelAccessRuleKey modelAccessRuleKey();
    public RuntimeFactValue value();
}

public final class ProtectedWriteReceipt {
    public ProtectedInvocationId invocationId();
    public RuntimeObjectId runtimeObjectId();
    public RuntimeWriteIntentId writeIntentId();
    public ModelAccessRuleKey modelAccessRuleKey();
    public RuntimeMutationVersion committedVersion();
}

public final class ProtectedAccessDenial {
    public DenialCode code();
    public String nonSensitiveMessage();
}
```

`ProtectedAccessResult` is a closed algebra:

```text
ALLOW + READ  -> ProtectedReadValue only
ALLOW + WRITE -> ProtectedWriteReceipt only
DENY          -> ProtectedAccessDenial only
```

Stable denials include `WRITE_INTENT_NOT_FOUND`, `WRITE_INTENT_AMBIGUOUS`, `WRITE_INTENT_STALE`, `RUNTIME_OBJECT_NOT_FOUND`, `RUNTIME_OBJECT_STALE`, `RUNTIME_WRITE_FAILED`, policy/proof mismatch, Guard unavailable and capability consumed.

## 10. Starter production API

```java
public final class ProtectedExecutionBridge implements ProtectedAccessPort {
    public ProtectedAccessResult invoke(ProtectedAccessInvocation invocation);
}

public final class ProtectedAccessRuntimeFactory {
    public ProtectedAccessComposition create(RuntimeExecutionFrameId frameId,
                                             RuntimeResolutionOwnerId ownerResolutionId);
}

public final class ProtectedAccessComposition implements AutoCloseable {
    public ProtectedAccessPort protectedAccessPort();
    public RuleProtectedAccessEntry ruleEntry();
    public ChangeProtectedAccessEntry changeEntry();
    public CustomActionProtectedAccessEntry customActionEntry();
}
```

Factory construction/wiring of EngineContext, RuntimeModelSession factory, production model adapter, Gateway and Guard is starter composition-root responsibility, not a business-caller injection surface.

## 11. Compatibility / gate

Existing SystemKey/RuleViewKey/EngineContext/CompiledModelSet compatibility remains. No EXECUTE is added. API remains candidate-only until same-revision independent Review/risk/TDD Evidence.

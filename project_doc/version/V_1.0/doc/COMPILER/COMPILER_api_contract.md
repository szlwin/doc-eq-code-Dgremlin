# COMPILER P2 API Contract

> Revision：`DESIGN-P2-R19`。Base：`DESIGN-P2-R18`。  
> Inputs：Overlay R04 + `BM-R17` + `FLOW-R07`。  
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`。

## 1. Existing source-compatible keys

Existing public `SystemKey(String)/name()` and `RuleViewKey(SystemKey,String)/owner()/name()` MUST remain. Additive aliases are allowed only if old source keeps compiling.

## 2. RuleKey

```java
package dec.core.context.model;

public final class RuleKey {
    public RuleKey(RuleViewKey ownerRuleViewKey, String localRuleName);
    public static RuleKey of(RuleViewKey ownerRuleViewKey, String localRuleName);
    public RuleViewKey ownerRuleViewKey();
    public String localRuleName();
}
```

Value identity/equality/hash = `(ownerRuleViewKey,localRuleName)`. Owning `CompiledRuleView` closure is the canonical store.

## 3. TargetKey

```java
package dec.core.context.model.access;

public final class TargetKey {
    public TargetKey(SystemKey ownerSystemKey, String canonicalSourceModelName);
    public static TargetKey of(SystemKey ownerSystemKey, String canonicalSourceModelName);
    public SystemKey ownerSystemKey();
    public String canonicalSourceModelName();
}
```

Exact value identity only. Compiler maps source `sourceModel` to one TargetKey inside the owner System; no path-derived/fuzzy/cross-System fallback.

## 4. ModelPath / AccessOperation

```java
public final class ModelPath { /* immutable canonical exact segments */ }

public enum AccessOperation {
    READ,
    WRITE
}
```

No EXECUTE member. Runtime wildcard is invalid.

## 5. ModelAccessRuleKey / policy types

```java
public final class ModelAccessRuleKey {
    public SystemKey systemKey();
    public TargetKey targetKey();
    public ModelPath modelPath();
    public AccessOperation operation();
}

public enum PolicyStatus {
    STATIC_ALLOW,
    RUNTIME_GUARD_REQUIRED
}

public enum RuntimeAccessRequirement {
    NONE,
    EXACT_RUNTIME_BINDING
}
```

`ModelAccessRuleKey` equality/hash includes all four fields.

```java
public final class RuntimeBindingPlan {
    public TargetKey resolvedTargetKey();
    public ModelPath modelPath();
    public ViewKey targetViewKey();
    public RuntimeSelectorPlanId selectorPlanId();
    public SourceRef sourceRef();
}
```

Legal rule construction rows are exactly:

```text
STATIC_ALLOW + NONE + no plan
RUNTIME_GUARD_REQUIRED + EXACT_RUNTIME_BINDING + plan
```

All other combinations reject before publication.

```java
public final class CompiledModelAccessRule {
    public ModelAccessRuleKey key();
    public SourceRef sourceRef();
    public PolicyStatus status();
    public RuntimeAccessRequirement runtimeRequirement();
    public Optional<RuntimeBindingPlan> runtimePlan();
}

public final class ModelAccessPolicyIndex {
    public static ModelAccessPolicyIndex empty();
    public static ModelAccessPolicyIndex of(Iterable<CompiledModelAccessRule> rules);
    public Optional<CompiledModelAccessRule> find(ModelAccessRuleKey key);
    public Set<ModelAccessRuleKey> keys();
}
```

`of` revalidates duplicate/null/wildcard/READ-WRITE/truth-table invariants before collapse and returns immutable exact index.

## 6. Neutral consumer-facing runtime contract (`dec-core-context`)

```java
public interface ProtectedAccessPort {
    ProtectedAccessResult execute(ProtectedAccessInvocation invocation);
}

public final class ProtectedAccessInvocation {
    public static ProtectedAccessInvocation of(
        ModelAccessRuleKey requestedRuleKey,
        AccessOperation operation,
        RuntimeExecutionFrameId frameId,
        RuntimeResolutionOwnerId ownerResolutionId,
        Optional<RuntimeCollectionCursorId> cursorId);

    public ModelAccessRuleKey requestedRuleKey();
    public AccessOperation operation();
    public RuntimeExecutionFrameId frameId();
    public RuntimeResolutionOwnerId ownerResolutionId();
    public Optional<RuntimeCollectionCursorId> cursorId();
}
```

Invocation immutable/non-null (except optional cursor contents); operation only READ/WRITE and must equal requested rule-key operation. Opaque runtime IDs are coordinates, never permission authority.

## 7. Real operation result types

```java
public final class ProtectedReadValue {
    public RuntimeObjectId runtimeObjectId();
    public ModelPath modelPath();
    public RuntimeFactValue value();
}

public final class ProtectedWriteReceipt {
    public RuntimeObjectId runtimeObjectId();
    public ModelPath modelPath();
    public ProtectedInvocationId invocationId();
    public RuntimeWriteIntentId writeIntentId();
}

public final class ProtectedAccessResult {
    public boolean allowed();
    public AccessOperation operation();
    public Optional<ProtectedReadValue> readValue();
    public Optional<ProtectedWriteReceipt> writeReceipt();
    public Optional<ProtectedAccessDenial> denial();
}
```

Result invariant:
- ALLOW READ => readValue only;
- ALLOW WRITE => writeReceipt only;
- DENY => denial only;
- all other combinations invalid.

## 8. Starter implementation / production composition

```java
package dec.core.starter.access;

public final class ProtectedExecutionBridge implements ProtectedAccessPort {
    public ProtectedAccessResult execute(
        ModelAccessRuleKey requestedRuleKey,
        AccessOperation operation,
        RuntimeExecutionFrameId frameId,
        RuntimeResolutionOwnerId ownerResolutionId,
        Optional<RuntimeCollectionCursorId> cursorId);

    @Override
    public ProtectedAccessResult execute(ProtectedAccessInvocation invocation);
}

public final class ProtectedAccessRuntimeFactory {
    public ProtectedAccessComposition bind(EngineContext engineContext);
}

public final class ProtectedAccessComposition {
    public EngineContext engineContext();
    public ProtectedExecutionBridge bridge();
    public RuleProtectedAccessEntry ruleEntry();
    public ChangeProtectedAccessEntry changeEntry();
    public CustomActionProtectedAccessEntry customActionEntry();
}
```

All representative entries share exact same Bridge and Context. Gateway/Guard/resolver/operation port/capability are not public composition outputs.

## 9. Starter-internal operation execution

Not public API:

```java
interface ProtectedOperationExecutionPort {
    ProtectedReadValue read(ResolvedProtectedAccess access);
    ProtectedWriteReceipt write(ResolvedProtectedAccess access);
}
```

`ResolvedProtectedAccess` is capability-bound internal state. WRITE intent is resolved internally from current frame/owner state; callers cannot inject an executable callback or raw operation port.

## 10. Dependency direction

```text
dec-core-context: neutral keys/invocation/result/ProtectedAccessPort
       ^
       |
dec-core-starter: Bridge/Gateway/Guard/capability/assembly/operation adapter

P3/P4/P6 core -> dec-core-context
P3/P4/P6 core -X-> dec-core-starter
application/demo -> dec-core-starter composition
```

## 11. One-shot concurrency

Capability state transition is atomic `ISSUED -> CONSUMED`; at most one concurrent consume invokes protected operation. Loser = `CAPABILITY_ALREADY_CONSUMED` with no readValue/writeReceipt/effect.

## 12. Compatibility / gate

Legacy `CompiledModelSet`/EngineContext constructors remain source-compatible and must not reconstruct permissions. Current P2 adds no bare-name RuleView adapter and no EXECUTE operation.

API Contract R19 remains `NEEDS_REVIEW / MACHINE_BLOCKED`; exact ApiContract/Architecture/Develop/Impact/CrossModule/Concurrency Review required before closure.

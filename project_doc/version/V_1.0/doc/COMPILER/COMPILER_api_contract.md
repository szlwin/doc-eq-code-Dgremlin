# COMPILER P2 API 契约

> Revision：`DESIGN-P2-R07`。输入：`BM-R10` candidate。状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。
> 本文件是当前 canonical P2 API source；Java 示例是 signature contract，生产实现必须兼容 Java 8。

## 1. Compatibility

- Java release 8；禁止 record / `Map.of` / `Map.copyOf` 等 Java 9+ API。
- `EngineContext` 保持 `public final class`，现有单参构造器和 `compiledModelSet()/modelSet()/projection()` 保持兼容。
- P2 API additive only；禁止新增 bare-name RuleView lookup。

## 2. Exact access rule

`ModelAccessRuleKey = SystemKey + DefinitionKey target + CanonicalModelPath + AccessOperation`。PolicyIndex 只允许一次 exact lookup，无 wildcard/fallback。

```java
public enum AccessCompilationStatus { STATIC_ALLOW, RUNTIME_GUARD_REQUIRED }
public enum DynamicBindingClassification { STATIC_BOUND, RUNTIME_OBJECT_BOUND }

public final class CompiledModelAccessRule {
    public ModelAccessRuleKey key();
    public AccessCompilationStatus status();
    public Optional<RuntimeAccessRequirement> runtimeRequirement();
    public Optional<RuntimeBindingPlan> runtimeBindingPlan();
    public SourceRef sourceRef();
}
```

## 3. Production DynamicBindingClassifier

```java
public interface DynamicBindingClassifier {
    DynamicBindingResult classify(ResolvedAccessConsumerIr accessIr);
}
```

Frozen R07 rules：

- `DIRECT_EXACT` IR -> `STATIC_BOUND`；真实 fixture：`order.ordered` rule-data 的直接 `status = 1` access。
- `EVERY_COLLECTION_ELEMENT` IR (`every(collectionPath, elementExpression)`) -> `RUNTIME_OBJECT_BOUND`；真实 fixture：`every(orderDetailList, status = 1)` 中 element `status` READ。
- 其它 runtime index/key/filter/find/selector 或无法解析 element type 的动态 IR -> `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED` compile ERROR。

Test stub 不得作为 classifier correctness 或 AC-006 Evidence。

## 4. RuntimeBindingPlan + RuntimeAccessRequirement

```java
public final class RuntimeBindingPlan {
    public enum Kind { COLLECTION_ELEMENT_MEMBERSHIP }
    public static RuntimeBindingPlan collectionElementMembership(
        ModelAccessRuleKey authorizedRuleKey,
        CanonicalModelPath collectionPath,
        CanonicalModelPath elementRelativePath,
        SourceRef sourceRef,
        Digest modelShapeDigest);
    public RuntimeBindingPlanKey key();
    public ModelAccessRuleKey authorizedRuleKey();
    public Kind kind();
    public CanonicalModelPath collectionPath();
    public CanonicalModelPath elementRelativePath();
}

public final class RuntimeAccessRequirement {
    public enum Kind { EXACT_RUNTIME_BINDING }
    public static RuntimeAccessRequirement derived(
        ModelAccessRuleKey authorizedRuleKey,
        RuntimeBindingPlanKey planKey,
        Kind kind,
        SourceRef sourceRef);
    public RuntimeRequirementKey key();
    public ModelAccessRuleKey authorizedRuleKey();
    public RuntimeBindingPlanKey planKey();
    public Kind kind();
}
```

Factories are public validated context-owned factories because compiler is a different module. Authority comes only from compiler publication in the exact selected rule.

## 5. Opaque runtime-object binding proof

R06 `RuntimeAccessBinding(engineContextId,targetKey,path,operation)` is removed. It cannot distinguish two elements under the same static tuple.

```java
public final class RuntimeBindingHandle {
    // no public/protected constructor and no public mint/factory
    public String engineContextId();
    public RuntimeBindingPlanKey planKey();
    public ModelAccessRuleKey selectedRuleKey();
    public String resolutionId();
}

public interface RuntimeBindingResolver {
    RuntimeBindingHandle resolve(
        RuntimeBindingPlan plan,
        RuntimeResolutionContext executionContext);

    RuntimeBindingVerification verify(
        RuntimeBindingHandle handle,
        RuntimeBindingPlan plan,
        ModelAccessRuleKey selectedRuleKey,
        String engineContextId);
}
```

Framework resolver issues the handle while resolving the actual collection element. Business callers cannot mint a handle. Internal object/collection identity may be retained by resolver/verifier but is never exposed as a raw POJO through Guard API. A foreign/stale/replayed/unknown handle fails closed even when System/target/path/operation are otherwise identical.

## 6. ModelAccessRequest / Guard

```java
public final class ModelAccessRequest {
    public String engineContextId();
    public SystemKey systemKey();
    public DefinitionKey targetKey();
    public CanonicalModelPath modelPath();
    public AccessOperation operation();
    public Optional<RuntimeBindingHandle> runtimeBindingHandle();
    public RuntimeFacts runtimeFacts();
    public Duration timeoutBudget();
}

public interface ModelAccessGuard {
    ModelAccessDecision authorize(ModelAccessRequest request);
}
```

All protected READ/WRITE/EXECUTE call Guard. `STATIC_ALLOW` is internal fast path. For `RUNTIME_GUARD_REQUIRED`, Guard validates the handle against exact selected rule/plan/current Context. Request cannot submit replacement rule/requirement/plan.

## 7. RuntimeFactValue / optional evaluator

`RuntimeFactValue` remains one public final tagged-value class with private constructor and six typed immutable factories. Current AC-006 does not use a business predicate evaluator. A future accepted Requirement may add predicate semantics only in a new revision; any evaluator must receive exact selected rule and may not re-query PolicyIndex.

## 8. EngineContext additive surfaces

May include `contextId()`, owner-qualified System/RuleView lookup, policy status, non-null fail-closed Guard, and framework runtime binding resolver access. No `findRuleView(String bareName)`.

## 9. Stable reasons

Compile: `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED` plus existing P2 diagnostics. Runtime: `RUNTIME_BINDING_REQUIRED`, `RUNTIME_BINDING_PROOF_INVALID`, `RUNTIME_BINDING_STALE`, `RUNTIME_BINDING_PLAN_MISMATCH`, `CONTEXT_IDENTITY_MISMATCH`, `POLICY_NOT_FOUND`, `GUARD_UNAVAILABLE`, `STATIC_ALLOW`, `RUNTIME_ALLOW`, `RUNTIME_DENY`.
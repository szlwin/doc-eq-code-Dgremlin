# COMPILER P2 API 契约

> Revision：`DESIGN-P2-R06`。输入：`BM-R09` candidate。状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。
> 本文件是当前 canonical P2 API source；Java 示例是 signature contract，生产实现必须兼容 Java 8。

## 1. Compatibility first

- root compiler target: Java 8；禁止 record / `Map.of` / `Map.copyOf` 等 Java 9+ API。
- `EngineContext` 保持 `public final class`；保留现有单参构造器及 `compiledModelSet()/modelSet()/projection()`。
- P2 新 API 只能 additive/compatible；禁止新增 bare-name RuleView lookup。

## 2. Access keys and compiled rule

`ModelAccessRuleKey = SystemKey + DefinitionKey target + CanonicalModelPath + AccessOperation`，四元组 exact identity。

```java
public enum AccessCompilationStatus { STATIC_ALLOW, RUNTIME_GUARD_REQUIRED }

public final class CompiledModelAccessRule {
    public ModelAccessRuleKey key();
    public AccessCompilationStatus status();
    public Optional<RuntimeAccessRequirement> runtimeRequirement();
    public SourceRef sourceRef();
}
```

PolicyIndex performs one exact lookup only; no wildcard/fallback。

## 3. RuntimeAccessRequirement: compiler-derived binding constraint

P2 does **not** define a source-authored permission predicate DSL。

```java
public final class RuntimeAccessRequirement {
    public enum Kind { EXACT_RUNTIME_BINDING }

    public static RuntimeAccessRequirement derived(
        ModelAccessRuleKey authorizedRuleKey,
        Kind kind,
        SourceRef sourceRef);

    public RuntimeRequirementKey key();
    public ModelAccessRuleKey authorizedRuleKey();
    public Kind kind();
    public SourceRef sourceRef();
    public String canonicalForm();
}
```

The factory is public because `dec-core-compiler` depends on `dec-core-context` across package/module boundaries。It performs strict validation and deterministic key generation。A caller-created value has no authority unless embedded by compiler publication in the selected rule of the current `CompiledModelSet`。

## 4. RuntimeAccessBinding

```java
public final class RuntimeAccessBinding {
    public static RuntimeAccessBinding resolved(
        String engineContextId,
        DefinitionKey targetKey,
        CanonicalModelPath resolvedPath,
        AccessOperation operation);

    public String engineContextId();
    public DefinitionKey targetKey();
    public CanonicalModelPath resolvedPath();
    public AccessOperation operation();
}
```

No raw model object/POJO is exposed。Guard validates the binding against the exact selected rule。

## 5. ModelAccessRequest / Guard

```java
public final class ModelAccessRequest {
    public String engineContextId();
    public SystemKey systemKey();
    public DefinitionKey targetKey();
    public CanonicalModelPath modelPath();
    public AccessOperation operation();
    public Optional<RuntimeAccessBinding> runtimeBinding();
    public RuntimeFacts runtimeFacts();
    public Duration timeoutBudget();
}

public interface ModelAccessGuard {
    ModelAccessDecision authorize(ModelAccessRequest request);
}
```

Every protected READ/WRITE/EXECUTE calls Guard。`STATIC_ALLOW` is internal fast path only。

## 6. Optional evaluator seam

If a later Requirement revision introduces an accepted runtime predicate, the evaluator must receive the exact selected rule：

```java
public interface RuntimeFactEvaluator {
    ModelAccessDecision evaluate(
        CompiledSystem system,
        CompiledModelAccessRule selectedRule,
        ModelAccessRequest request);
}
```

Current P2 AC-006 does not depend on such a predicate。The evaluator may not re-query PolicyIndex or infer hidden policy。

## 7. RuntimeFactValue

`RuntimeFactValue` is one `public final` tagged-value class with private constructor and typed factories for STRING/BOOLEAN/DECIMAL/INSTANT/LIST/OBJECT。Collections are recursively immutable；there is no generic `Object` getter and no external subclassing。

## 8. EngineContext P2 additions

Additive compatible surfaces may include：

- `contextId()`
- `findSystem(SystemKey)`
- `findRuleView(RuleViewKey)`
- `findRuleView(SystemKey,String)`
- `policyCompilationStatus(ModelAccessRuleKey)`
- `modelAccessGuard()` (non-null；unavailable sentinel fail-closes)

`findRuleView(String bareName)` is forbidden for P2 consumers。

## 9. Decisions/reasons

Decision code: ALLOW/DENY。Stable reasons include `STATIC_ALLOW`, `RUNTIME_ALLOW`, `RUNTIME_DENY`, `POLICY_NOT_FOUND`, `CONTEXT_IDENTITY_MISMATCH`, `RUNTIME_BINDING_REQUIRED`, `RUNTIME_BINDING_MISMATCH`, `GUARD_UNAVAILABLE`, evaluator unavailable/exception/null/timeout/unknown。

No API in this contract permits a runtime caller to submit a replacement rule or requirement。

# COMPILER P2 API Contract

> Revision：`DESIGN-P2-R18`  
> Inputs：`BM-R16` + `REQAN-P2-R01+DEC-OVERLAY-20260809-R04` + `FLOW-R06@p2-system-ruleview-protected-access`  
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`

## 1. Existing API compatibility

Existing public surfaces MUST remain source-compatible：

```java
public SystemKey(String name);
public String name();
public RuleViewKey(SystemKey owner, String name);
public SystemKey owner();
public String name();
```

Aliases (`of/value/systemKey/localName`) are additive only。Existing `EngineContext(CompiledModelSet)` and legacy 8-arg `CompiledModelSet` constructor remain。

## 2. SystemVersion / CompiledSystem

```java
public final class SystemVersionIdentity {
    public Optional<String> declaredVersion();
    public String sourceSemanticDigest();
    public String schemaVersion();
    public String compilerVersion();
}

public final class CompiledSystem {
    public SystemKey key();
    public SourceRef sourceRef();
    public SystemVersionIdentity versionIdentity();
    public Set<DataKey> ownedDataKeys();
    public Set<ViewKey> ownedViewKeys();
    public Set<RuleViewKey> ownedRuleViewKeys();
    public Set<RuleKey> ownedRuleKeys();
    public Set<InformationKey> ownedInformationKeys();
    public Set<ModelAccessRuleKey> ownedModelAccessRuleKeys();
}
```

Sets are immutable deterministic projections, not authorities。

## 3. RuleKey / CompiledRuleView

```java
public final class RuleKey {
    public RuleKey(RuleViewKey ownerRuleViewKey, String localRuleName);
    public static RuleKey of(RuleViewKey ownerRuleViewKey, String localRuleName);
    public RuleViewKey ownerRuleViewKey();
    public String localRuleName();
}
```

Identity/equality/hash = exact owner RuleView + exact local name。Authoritative store is the owning `CompiledRuleView` closure；no P2 global Rule registry。

```java
public final class CompiledRuleView {
    public RuleViewKey key();
    public ViewKey resolvedViewKey();
    public List<RuleKey> resolvedRuleKeys();
    public SourceRef sourceRef();
}
```

<a id="7-ruleviewresolver"></a>
## 4. RuleViewResolver

```java
public interface RuleViewResolver {
    Optional<CompiledRuleView> find(RuleViewKey key);
    CompiledRuleView require(SystemKey systemKey, String localName);
}
```

No new bare-name adapter/fallback is part of P2 canonical API。

## 5. ModelPath / TargetKey

`TargetKey` and `ModelPath` are immutable exact value types in `dec-core-context`。ModelPath contains canonical exact segments only; wildcard never reaches runtime policy。

```java
public enum ModelPathConsumerKind { RULE, CHANGE, QUERY_CONTRACT, MODEL_ACCESS }
public interface ModelPathCompiler { ModelPath compile(ModelPathInput input); }
```

Consumer kind is provenance only。

## 6. AccessOperation — exact enum

```java
public enum AccessOperation {
    READ,
    WRITE
}
```

**No EXECUTE member exists in current P2.** No source/raw/policy/runtime API accepts an EXECUTE operation。

P1 compatibility conversion is exactly `AccessMode.READ -> READ`, `AccessMode.WRITE -> WRITE`。

## 7. ModelAccess rule/index closure

`ModelAccessRuleKey` immutable identity：

```text
SystemKey + TargetKey + ModelPath + AccessOperation(READ|WRITE)
```

`CompiledModelAccessRule` immutable read contract：exact key + SourceRef + status + runtime requirement + optional runtime plan。Compiler owns construction。

```java
public final class ModelAccessPolicyIndex {
    public static ModelAccessPolicyIndex empty();
    public static ModelAccessPolicyIndex of(Iterable<CompiledModelAccessRule> rules);
    public Optional<CompiledModelAccessRule> find(ModelAccessRuleKey key);
    public Set<ModelAccessRuleKey> keys();
}
```

Only READ/WRITE keys valid；no secondary permission map。

## 8. Protected invocation / runtime IDs

```java
public final class ProtectedAccessInvocation {
    public static ProtectedAccessInvocation of(
        ModelAccessRuleKey requestedRuleKey,
        AccessOperation operation,
        RuntimeExecutionFrameId frameId,
        RuntimeResolutionOwnerId ownerResolutionId,
        Optional<RuntimeCollectionCursorId> cursorId);
    // immutable getters for the same fields
}
```

Runtime frame/owner/cursor IDs and resolved RuntimeObjectId are immutable opaque identity values, never permissions。Starter creates internal `ProtectedInvocationId` per bridge call。

## 9. ProtectedExecutionBridge / result

```java
public final class ProtectedExecutionBridge {
    public ProtectedAccessResult execute(
        ModelAccessRuleKey requestedRuleKey,
        AccessOperation operation,
        RuntimeExecutionFrameId frameId,
        RuntimeResolutionOwnerId ownerResolutionId,
        Optional<RuntimeCollectionCursorId> cursorId);
    public ProtectedAccessResult execute(ProtectedAccessInvocation invocation);
}
```

No token API。Operation must be READ/WRITE and exactly match key。`ProtectedAccessResult` is immutable terminal ALLOW/DENY and never exposes capability。

## 10. Production composition / acquisition

Owner：`dec-core-starter`。

```java
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

Factory itself is built by normal starter production composition root。All three entries returned by one composition are bound to exactly the same Bridge instance and EngineContext authority snapshot。AC-007 production Evidence must obtain entries through this path; manually constructing `new Entry(testBridge)` is not production reachability Evidence。

Entry execute API：

```java
public final class RuleProtectedAccessEntry {
    public ProtectedAccessResult execute(ProtectedAccessInvocation invocation);
}
public final class ChangeProtectedAccessEntry {
    public ProtectedAccessResult execute(ProtectedAccessInvocation invocation);
}
public final class CustomActionProtectedAccessEntry {
    public ProtectedAccessResult execute(ProtectedAccessInvocation invocation);
}
```

Business consumers do not receive Gateway、Guard、resolver、raw operation port、mutable PolicyIndex、issued-pair or capability mint。

## 11. Internal capability and concurrency

One-shot capability is starter-internal, immutable authority binding `(ProtectedInvocationId, context, ruleKey, READ|WRITE, actual RuntimeObjectId, runtime plan/proof identity)` plus atomic consume state。

```text
ISSUED --atomic CAS--> CONSUMED
```

Same capability concurrent consume succeeds at most once。Loser gets stable `CAPABILITY_ALREADY_CONSUMED` DENY with zero effects。Check-then-set is invalid；Java-8 atomic primitive or equivalent required。

## 12. Runtime denial

Stable fields：code、SystemKey、optional RuleView provenance、READ/WRITE、canonical ModelPath、policy SourceRef。No sensitive runtime value/object dump。Include `CAPABILITY_ALREADY_CONSUMED` in addition to policy/proof/target/Guard denial families。

## 13. Bare-name compatibility

P2 adds no bare-name resolver adapter。Any surviving historical read compatibility remains outside canonical P2 API, read-only, ambiguous-name reject, no Registry/Policy mutation and no protected WRITE path。

## 14. Gate

Candidate only until Requirement/BM/Flow/ApiContract/Architecture/Develop/Impact/CrossModule/Concurrency exact Reviews and machine risk scan complete。

# COMPILER P2 API Contract

> Revision：`DESIGN-P2-R15`  
> Inputs：`BM-R13` + `REQAN-P2-R01+DEC-OVERLAY-20260809`  
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`

## 1. SystemVersionIdentity

```java
public final class SystemVersionIdentity {
    public Optional<String> declaredVersion();
    public String sourceSemanticDigest();
    public String schemaVersion();
}
```

Contract：
- `declaredVersion` 只反映输入显式声明；未声明返回 empty；
- `sourceSemanticDigest` mandatory、deterministic；
- equality/hashCode 必须按 value semantics；
- 不允许 timestamp/random/load-order version。

## 2. CompiledSystem

```java
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

All returned sets are immutable deterministic snapshots. They must exactly match current compiled registries for that owner.

## 3. RuleViewKey

```java
public final class RuleViewKey {
    public static RuleViewKey of(SystemKey systemKey, String localName);
    public SystemKey systemKey();
    public String localName();
}
```

No public bare-name constructor/factory.

## 4. CompiledRuleView

```java
public final class CompiledRuleView {
    public RuleViewKey key();
    public ViewKey resolvedViewKey();
    public List<RuleKey> resolvedRuleKeys();
    public SourceRef sourceRef();
}
```

`resolvedViewKey` and every rule key must be compiler-resolved exact keys before publication. List order is canonical and immutable.

## 5. RuleViewResolver

<a id="7-ruleviewresolver"></a>
```java
public interface RuleViewResolver {
    Optional<CompiledRuleView> find(RuleViewKey key);
    CompiledRuleView require(SystemKey systemKey, String localName);
}
```

New production path has no `find(String bareName)` or equivalent fallback.

## 6. Shared ModelPath compiler

```java
public enum ModelPathConsumerKind {
    RULE,
    CHANGE,
    QUERY_CONTRACT,
    MODEL_ACCESS
}

public final class ModelPathInput {
    public ModelPathConsumerKind consumerKind();
    public SystemKey systemKey();
    public TargetKey targetKey();
    public String rawPath();
    public SourceRef sourceRef();
}

public interface ModelPathCompiler {
    ModelPath compile(ModelPathInput input);
}
```

For equal System/target/raw path, consumer kind cannot change canonical result. Query uses compile-contract only in P2; execution remains P6.

## 7. ModelAccessPolicyIndex

```java
public final class ModelAccessPolicyIndex {
    public static ModelAccessPolicyIndex empty();
    public static ModelAccessPolicyIndex of(Iterable<CompiledModelAccessRule> rules);
    public Optional<CompiledModelAccessRule> find(ModelAccessRuleKey key);
    public Set<ModelAccessRuleKey> keys();
}
```

`of` rejects null, duplicate exact keys, wildcard/fuzzy runtime keys and invalid STATIC/RUNTIME state before collapse.

## 8. CompiledModelSet publication

Existing eight-argument public constructor remains source compatible and produces `ModelAccessPolicyIndex.empty()`.

P2 path:

```java
public static CompiledModelSet published(
    PublishedSourceManifest sourceManifest,
    Registry<DefinitionKey, CompiledDefinition> definitions,
    DeferredRegistry deferred,
    ModelAccessPolicyIndex modelAccessPolicyIndex,
    List<Diagnostic> diagnostics,
    DigestPair digestPair,
    String compilerVersion,
    String schemaVersion,
    String optionsDigest);

public Optional<CompiledSystem> system(SystemKey key);
public Set<SystemKey> systemKeys();
public Optional<CompiledRuleView> ruleView(RuleViewKey key);
public ModelAccessPolicyIndex modelAccessPolicyIndex();
```

Production P2 compiler candidate must use `published(...)`; it must not call legacy constructor after computing a policy-aware digest.

## 9. EngineContext

Existing `EngineContext(CompiledModelSet)` remains. Additive read-through only:

```java
public Optional<CompiledSystem> system(SystemKey key);
public Optional<CompiledRuleView> ruleView(RuleViewKey key);
public ModelAccessPolicyIndex modelAccessPolicyIndex();
```

Returned values are the same immutable authority/snapshots held by its `CompiledModelSet`.

## 10. ProtectedExecutionBridge

```java
public final class ProtectedExecutionBridge {
    public ProtectedAccessResult execute(
        ModelAccessRuleKey requestedRuleKey,
        AccessOperation operation,
        RuntimeExecutionFrameId frameId,
        RuntimeResolutionOwnerId ownerResolutionId,
        Optional<RuntimeCollectionCursorId> cursorId);
}
```

Decision `DEC-P2-DIRECT-BRIDGE-AUTHORITY-001` applies. Caller may choose exact published rule/op; absent rule or operation mismatch is fail-closed. No token API exists.

## 11. Protected production seam

Only supported runtime route:

```text
ProtectedExecutionBridge
 -> internal issueInvocation
 -> internal target resolution
 -> internal one-shot capability mint
 -> ProtectedAccessGateway
 -> ModelAccessGuard
 -> bound operation
```

Must not expose public/protected APIs for:

- issued-pair construction/mint;
- capability construction/mint;
- direct post-Guard target operation;
- secondary permission-map selection;
- compatibility adapter writes.

Public composition SPI may register trusted target/operation adapters, but registration must not return raw operation authority to business consumers.

## 12. Operation independence

Exact rule key includes operation semantics. Implementations must behave as if policy lookup key were `(System,target,path,operation)`; any permission on the same path for another operation is irrelevant.

## 13. Runtime denial result

```java
public interface ProtectedAccessDenial {
    ProtectedAccessDenialCode code();
    SystemKey systemKey();
    Optional<RuleViewKey> ruleViewKey();
    AccessOperation operation();
    ModelPath modelPath();
    SourceRef policySourceRef();
}
```

Stable repeated-denial fields required. Runtime actual values, object dumps, credentials or raw sensitive configuration must not be returned.

Minimum stable denial classes:
- `POLICY_NOT_FOUND`
- `RUNTIME_BINDING_STALE`
- `RUNTIME_PLAN_MISMATCH`
- `TARGET_SUBSTITUTION`
- `GUARD_UNAVAILABLE`

## 14. Java 8 / compatibility

No records/sealed types/module-system requirement. New APIs use Java 8-compatible final classes/interfaces, `Optional`, immutable collection copies and additive methods. Existing EngineContext and legacy CompiledModelSet constructor remain source compatible.

## 15. Gate

This contract is candidate-only until exact ApiContract/Architecture/Develop/Impact/CrossModule/Concurrency Review and machine risk scan complete.

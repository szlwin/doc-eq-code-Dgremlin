# COMPILER P2 API Contract

> Revision：`DESIGN-P2-R16`  
> Inputs：`BM-R14` + `REQAN-P2-R01+DEC-OVERLAY-20260809-R02` + `FLOW-R04@p2-system-ruleview-protected-access`  
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED / AC007_PENDING_USER_DECISION`

## 1. Existing key source compatibility

### SystemKey

```java
public final class SystemKey {
    // existing; MUST remain
    public SystemKey(String name);
    public String name();

    // additive aliases are allowed
    public static SystemKey of(String name);
    public String value();
}
```

### RuleViewKey

```java
public final class RuleViewKey {
    // existing; MUST remain
    public RuleViewKey(SystemKey owner, String name);
    public SystemKey owner();
    public String name();

    // additive aliases are allowed
    public static RuleViewKey of(SystemKey systemKey, String localName);
    public SystemKey systemKey();
    public String localName();
}
```

Alias getters return the same value as existing getters. R16 does not permit removing/renaming existing public constructor/accessors.

## 2. SystemVersionIdentity

```java
public final class SystemVersionIdentity {
    public Optional<String> declaredVersion();
    public String sourceSemanticDigest();
    public String schemaVersion();
    public String compilerVersion();
}
```

Contract：
- absent source-declared version -> empty；
- `schemaVersion()` exact equals enclosing published `CompiledModelSet.schemaVersion`；
- `compilerVersion()` exact equals enclosing published `CompiledModelSet.compilerVersion`；
- options digest/version remains enclosing compilation identity；
- value semantics deterministic; no time/random/load-order fields。

## 3. CompiledSystem derived ownership read model

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

Returned sets are immutable deterministic derived snapshots, not authorities. Truth mapping:
- Data/View/RuleView/Information -> final owner-qualified typed registries；
- Rule -> final CompiledRuleView rule closure；
- ModelAccessRule -> final `ModelAccessPolicyIndex.keys()`/compiled policy rule set filtered System。

No public mutation/rebuild API exists on `CompiledSystem`.

## 4. CompiledRuleView

```java
public final class CompiledRuleView {
    public RuleViewKey key();
    public ViewKey resolvedViewKey();
    public List<RuleKey> resolvedRuleKeys();
    public SourceRef sourceRef();
}
```

Resolved View/rules exact before publication. Rule closure is authoritative for System `ownedRuleKeys` derivation.

<a id="7-ruleviewresolver"></a>
## 5. RuleViewResolver

```java
public interface RuleViewResolver {
    Optional<CompiledRuleView> find(RuleViewKey key);
    CompiledRuleView require(SystemKey systemKey, String localName);
}
```

No new bare-name fallback.

## 6. P1 SharedModelPath compatibility conversion

`SharedModelPath` remains accepted only at legacy/source compilation boundary.

```java
public interface ModelPathCompiler {
    ModelPath compile(ModelPathInput input);
}
```

Required conversion semantics:
- exact P1 SharedModelPath -> exact P2 ModelPath through shared compiler；
- `SharedModelPath("*")` -> finite deterministic exact-child expansion before `CompiledModelAccessRule` creation；
- no wildcard `ModelPath` in PolicyIndex/Bridge/Guard；
- no runtime authorization method accepts `SharedModelPath`。

## 7. P1 AccessMode compatibility conversion

```text
AccessMode.READ  -> AccessOperation.READ
AccessMode.WRITE -> AccessOperation.WRITE
```

No AccessMode value maps to EXECUTE. EXECUTE requires explicit P2 source declaration. `ModelAccessPolicyIndex.find` and Bridge/Guard use `AccessOperation` only.

## 8. Shared P2 ModelPath compiler

```java
public enum ModelPathConsumerKind { RULE, CHANGE, QUERY_CONTRACT, MODEL_ACCESS }

public final class ModelPathInput {
    public ModelPathConsumerKind consumerKind();
    public SystemKey systemKey();
    public TargetKey targetKey();
    public String rawPath();
    public SourceRef sourceRef();
}
```

Consumer kind is provenance only; equal System/target/path => value-equal ModelPath.

## 9. ModelAccessPolicyIndex

```java
public final class ModelAccessPolicyIndex {
    public static ModelAccessPolicyIndex empty();
    public static ModelAccessPolicyIndex of(Iterable<CompiledModelAccessRule> rules);
    public Optional<CompiledModelAccessRule> find(ModelAccessRuleKey key);
    public Set<ModelAccessRuleKey> keys();
}
```

Exact operation-qualified lookup. `of` rejects duplicate/null/wildcard/invalid status-plan before collapse. No secondary permission map.

## 10. CompiledModelSet / EngineContext

Existing eight-argument public `CompiledModelSet` constructor MUST remain and attaches `ModelAccessPolicyIndex.empty()`; it never reconstructs policy from definitions/registries.

P2 `CompiledModelSet.published(...)` keeps `compilerVersion`, `schemaVersion`, options digest and the same immutable PolicyIndex/digest closure. Additive reads expose System/RuleView/PolicyIndex only.

Existing `EngineContext(CompiledModelSet)` MUST remain source compatible; additive read-through only.

## 11. ProtectedExecutionBridge

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

`DEC-P2-DIRECT-BRIDGE-AUTHORITY-001` ACTIVE. No token API. Exact policy miss/op mismatch fail closed.

## 12. FLOW-PROTECTED-ACCESS-EXECUTE public/private boundary

Only supported P2 runtime route:

```text
Bridge -> internal issued invocation -> resolver -> internal one-shot capability -> Gateway -> Guard -> bound operation
```

No public/protected issued-pair mint, capability mint, post-Guard caller-selected target operation, secondary permission authority, or compatibility write bypass.

This seam exists regardless of AC-007 A/B choice. **Whether seam-only is sufficient P2 final acceptance remains PENDING_USER_DECISION.**

## 13. Runtime denial

Stable denial fields: code/SystemKey/optional RuleViewKey/AccessOperation/ModelPath/policy SourceRef. No sensitive actual values. Minimum classes: POLICY_NOT_FOUND, RUNTIME_BINDING_STALE, RUNTIME_PLAN_MISMATCH, TARGET_SUBSTITUTION, GUARD_UNAVAILABLE.

## 14. Java 8 / additive compatibility

No record/sealed/module-system requirement. Existing constructors/accessors listed above remain. Additive aliases/methods cannot force source migration.

## 15. Gate

Candidate only until Requirement AC-007 user decision, BusinessFlow, ApiContract/Architecture/Develop/Impact/CrossModule/Concurrency exact Reviews and machine risk scan complete.

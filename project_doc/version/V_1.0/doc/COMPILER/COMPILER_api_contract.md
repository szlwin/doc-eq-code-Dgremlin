# COMPILER P2 API Contract

> Revision：`DESIGN-P2-R17`  
> Inputs：`BM-R15` + `REQAN-P2-R01+DEC-OVERLAY-20260809-R03` + `FLOW-R05@p2-system-ruleview-protected-access`  
> Status：`NEEDS_REVIEW / MACHINE_BLOCKED`

## 1. Existing key source compatibility

### SystemKey

```java
public final class SystemKey {
    public SystemKey(String name);      // existing; MUST remain
    public String name();               // existing; MUST remain
    public static SystemKey of(String name); // optional additive alias
    public String value();              // optional additive alias
}
```

### RuleViewKey

```java
public final class RuleViewKey {
    public RuleViewKey(SystemKey owner, String name); // existing; MUST remain
    public SystemKey owner();                         // existing; MUST remain
    public String name();                             // existing; MUST remain
    public static RuleViewKey of(SystemKey systemKey, String localName); // additive
    public SystemKey systemKey();
    public String localName();
}
```

Alias getters return the same values as existing getters。R17 不允许删除/改名 existing public constructor/accessors。

## 2. SystemVersionIdentity

```java
public final class SystemVersionIdentity {
    public Optional<String> declaredVersion();
    public String sourceSemanticDigest();
    public String schemaVersion();
    public String compilerVersion();
}
```

Absent source-declared version -> empty；schema/compiler version exact equals enclosing published `CompiledModelSet` values；no time/random/load-order identity。

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

Returned sets immutable deterministic derived snapshots。Data/View/RuleView/Information -> final typed registries；Rule -> final CompiledRuleView rule closure；ModelAccessRule -> final PolicyIndex keys/compiled policy set filtered by System。No public mutation/rebuild API。

## 4. CompiledRuleView

```java
public final class CompiledRuleView {
    public RuleViewKey key();
    public ViewKey resolvedViewKey();
    public List<RuleKey> resolvedRuleKeys();
    public SourceRef sourceRef();
}
```

Resolved View/rules exact before publication。

<a id="7-ruleviewresolver"></a>
## 5. RuleViewResolver

```java
public interface RuleViewResolver {
    Optional<CompiledRuleView> find(RuleViewKey key);
    CompiledRuleView require(SystemKey systemKey, String localName);
}
```

No new bare-name fallback。

## 6. P1 SharedModelPath compatibility conversion

`SharedModelPath` accepted only at legacy/source compilation boundary。Exact path -> shared P2 `ModelPathCompiler` -> exact ModelPath；`SharedModelPath("*")` -> finite deterministic exact-child expansion before `CompiledModelAccessRule` creation。No runtime authorization method accepts `SharedModelPath`。

## 7. P1 AccessMode compatibility conversion

```text
AccessMode.READ  -> AccessOperation.READ
AccessMode.WRITE -> AccessOperation.WRITE
```

No AccessMode value maps to EXECUTE；EXECUTE requires explicit P2 source declaration。PolicyIndex/Bridge/Guard use `AccessOperation` only。

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

public interface ModelPathCompiler {
    ModelPath compile(ModelPathInput input);
}
```

Consumer kind is provenance only；equal System/target/path => value-equal ModelPath。

## 9. ModelAccessPolicyIndex

```java
public final class ModelAccessPolicyIndex {
    public static ModelAccessPolicyIndex empty();
    public static ModelAccessPolicyIndex of(Iterable<CompiledModelAccessRule> rules);
    public Optional<CompiledModelAccessRule> find(ModelAccessRuleKey key);
    public Set<ModelAccessRuleKey> keys();
}
```

Exact operation-qualified lookup；`of` rejects duplicate/null/wildcard/invalid status-plan before collapse；no secondary permission map。

## 10. CompiledModelSet / EngineContext

Existing eight-argument public `CompiledModelSet` constructor MUST remain and attaches `ModelAccessPolicyIndex.empty()`；it never reconstructs policy from definitions/registries。

P2 `CompiledModelSet.published(...)` retains compilerVersion/schemaVersion/options digest and same immutable PolicyIndex/digest closure。Existing `EngineContext(CompiledModelSet)` MUST remain source compatible；additive read-through only。

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

`DEC-P2-DIRECT-BRIDGE-AUTHORITY-001` ACTIVE。No token API。Exact policy miss/op mismatch fail closed。

<a id="p2-ac007-consumer-api"></a>
## 12. AC-007 Option B production representative consumer API

User-selected `DEC-P2-AC007-STAGE-BOUNDARY-001:OPTION_B` is ACTIVE。P2 main production source must expose additive representative entry types equivalent to this frozen API：

```java
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

public final class RuleProtectedAccessEntry {
    public RuleProtectedAccessEntry(ProtectedExecutionBridge bridge);
    public ProtectedAccessResult execute(ProtectedAccessInvocation invocation);
}

public final class ChangeProtectedAccessEntry {
    public ChangeProtectedAccessEntry(ProtectedExecutionBridge bridge);
    public ProtectedAccessResult execute(ProtectedAccessInvocation invocation);
}

public final class CustomActionProtectedAccessEntry {
    public CustomActionProtectedAccessEntry(ProtectedExecutionBridge bridge);
    public ProtectedAccessResult execute(ProtectedAccessInvocation invocation);
}
```

### 12.1 Authority boundary

The only protected-access authority dependency allowed in these entry constructors is `ProtectedExecutionBridge`。They MUST NOT accept/expose/store `ProtectedAccessGateway`、`ModelAccessGuard`、target resolver、raw operation port、mutable/secondary PolicyIndex、issued-pair/capability mint/factory。

`ProtectedAccessInvocation` is immutable and carries no permission result/allow flag/consumer-specific override。Consumer category is represented by the concrete entry type/provenance only and is not an authorization key dimension。

### 12.2 Execution semantics

Each entry forwards the same exact tuple into its bound Bridge。For equal Context + invocation + resolved runtime facts：
- authorization classification must be equal across Rule/change/custom-action entries；
- permission cannot be upgraded/downgraded by consumer type；
- DENY occurs before operation/effects；
- ALLOW reaches only capability-bound target/operation through Gateway/Guard。

### 12.3 Production evidence constraint

AC-007 cannot be closed using a test-only wrapper/fake consumer, reflection/package-private invocation of internals, manual issued pair/capability, or hand-built secondary permission authority。The three main-source entry types must be used by public production construction/composition in integration evidence。

## 13. Protected production seam

Only supported authority route：

```text
Rule/Change/CustomAction production entry
 -> ProtectedExecutionBridge
 -> internal issued invocation
 -> internal target resolution
 -> internal one-shot capability
 -> ProtectedAccessGateway
 -> ModelAccessGuard
 -> bound operation / DENY
```

No public/protected issued-pair mint、capability mint、post-Guard caller-selected target operation、secondary permission authority、compatibility write bypass。

## 14. Runtime denial

Stable denial fields：code/SystemKey/optional RuleViewKey/AccessOperation/ModelPath/policy SourceRef。No sensitive actual values。Minimum：POLICY_NOT_FOUND、RUNTIME_BINDING_STALE、RUNTIME_PLAN_MISMATCH、TARGET_SUBSTITUTION、GUARD_UNAVAILABLE。Same authorization facts across the three representative entries have the same authorization denial classification/code；entry provenance may differ only as diagnostic provenance。

## 15. Java 8 / additive compatibility

No record/sealed/module-system requirement。Existing constructors/accessors remain。New representative consumer APIs are additive and must compile under Java 8-compatible source level。

## 16. Gate

Candidate only until Requirement/BM/BusinessFlow and ApiContract/Architecture/Develop/Impact/CrossModule/Concurrency exact Reviews plus machine risk scan complete。AC-007 user decision is satisfied by Option B, but no execution Evidence exists yet。

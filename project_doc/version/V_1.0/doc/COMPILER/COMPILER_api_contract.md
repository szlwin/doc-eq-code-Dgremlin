# COMPILER P2 API 契约

> Revision：`DESIGN-P2-R14`。Inputs：`REQAN-P2-R01` + `DEC-P2-DIRECT-BRIDGE-AUTHORITY-001` + `BM-R12`。状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。
> 本文是 consolidated API contract；恢复 System/RuleView API，同时保留 PolicyIndex/publication/direct bridge runtime contract。Java 8 only。

## 1. Maven / package ownership

| Concern | Module | Owner package |
|---|---|---|
| SystemKey / RuleViewKey / compiled neutral facts | `dec-core-context` | existing context model packages / `dec.core.context.model.*` |
| ModelAccessPolicyIndex / compiled access neutral contracts | `dec-core-context` | `dec.core.context.model.access.*` |
| System/RuleView/model-access compiler passes | `dec-core-compiler` | existing compiler/pass/modelaccess packages |
| CompiledModelSet / EngineContext publication/read API | `dec-core-context` | existing packages |
| Protected runtime/Bridge/Gateway/Guard | `dec-core-starter` | `dec.core.starter.access.*` |
| Target/operation adapters | `dec-core-starter` | `dec.core.starter.access.spi.*` |

No new runtime Maven module and no reverse context -> compiler/starter dependency。

## 2. Java 8 compatibility

- no records/sealed types/`Map.of`/`Map.copyOf`；
- `EngineContext` remains `public final` with existing `EngineContext(CompiledModelSet)`；
- existing 8-arg public `CompiledModelSet` constructor remains unchanged；
- additive APIs only。

<a id="3-systemkey-compiledsystem"></a>
## 3. SystemKey / CompiledSystem

```java
public final class SystemKey implements Comparable<SystemKey> {
    public static SystemKey of(String value);
    public String value();
}

public final class CompiledSystem {
    public SystemKey key();
    public SourceRef sourceRef();
}
```

Contract：non-null/non-blank canonical key；exact equality；deterministic compare/order；no filename/path-derived identity。

<a id="4-ruleviewkey"></a>
## 4. RuleViewKey / CompiledRuleView

```java
public final class RuleViewKey implements Comparable<RuleViewKey> {
    public static RuleViewKey of(SystemKey systemKey, String localName);
    public SystemKey systemKey();
    public String localName();
}

public final class CompiledRuleView {
    public RuleViewKey key();
    public SourceRef sourceRef();
    public List<RuleKey> resolvedRuleRefs();
}
```

`equals/hashCode/compareTo` include systemKey + localName。No public constructor/factory accepting only bare name for canonical P2 key。

<a id="5-modelaccessrule"></a>
## 5. CompiledModelAccessRule / ModelAccessPolicyIndex

```java
public enum AccessOperation { READ, WRITE, EXECUTE }
public enum AccessCompilationStatus { STATIC_ALLOW, RUNTIME_GUARD_REQUIRED }
public enum DynamicBindingClassification { STATIC_BOUND, RUNTIME_OBJECT_BOUND }

public final class CompiledModelAccessRule {
    public ModelAccessRuleKey key();
    public AccessCompilationStatus status();
    public Optional<RuntimeAccessRequirement> runtimeRequirement();
    public Optional<RuntimeBindingPlan> runtimeBindingPlan();
    public SourceRef sourceRef();
}

public final class ModelAccessPolicyIndex {
    public static ModelAccessPolicyIndex empty();
    public static ModelAccessPolicyIndex of(Iterable<CompiledModelAccessRule> rules);
    public Optional<CompiledModelAccessRule> find(ModelAccessRuleKey key);
    public Set<ModelAccessRuleKey> keys();
}
```

`of(...)` validates duplicate exact key/null/state/path and returns immutable deterministic snapshot。STATIC_ALLOW cannot carry runtime requirement/plan；runtime-required must carry exact requirement/plan。

## 6. CompiledModelSet / EngineContext publication

Existing constructor remains exact existing signature and attaches empty policy index。

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
    String optionsVersion);

public ModelAccessPolicyIndex modelAccessPolicyIndex();
```

`published(...)` requires same immutable index used by digest-bound compiler input。`equals/hashCode` include policy index semantics。

```java
public final class EngineContext {
    public EngineContext(CompiledModelSet compiledModelSet); // existing
    public CompiledModelSet compiledModelSet();              // existing
    public ModelAccessPolicyIndex modelAccessPolicyIndex();  // additive read-through
}
```

<a id="7-ruleviewresolver"></a>
## 7. RuleViewResolver

```java
public interface RuleViewResolver {
    Optional<CompiledRuleView> find(RuleViewKey key);
    CompiledRuleView require(SystemKey systemKey, String localName);
}
```

Required behavior：

- exact composite key only；
- same local name across Systems resolves independently；
- unknown System/RuleView -> stable failure；
- no `find(String name)` / `require(String name)` canonical new path；
- legacy bare-name adapter, if retained, is a separate read-only compatibility API and cannot mutate/register canonical RuleView registry。

## 8. System / RuleView compiler internal contract

Implementation-ready compiler seams：

```text
registerSystem(SystemKey, SourceRef)
registerRuleView(RuleViewKey, SourceRef, unresolvedRuleRefs)
resolveRuleViewReferences(...)
publish deterministic immutable registries
```

Exact class names may follow existing pass conventions; semantic contract is frozen：all System symbols register before cross-reference resolution；forward refs allowed；duplicates/unknown refs are stable ERROR；candidate publication occurs only after complete semantic validation。

## 9. Compiler digest/publication internal contract

```java
static DigestBoundCompiledInput bind(
    CompilerDigestService digestService,
    SourceManifest sources,
    PublishedSourceManifest sourceManifest,
    Registry<DefinitionKey, CompiledDefinition> definitions,
    DeferredRegistry deferred,
    ModelAccessPolicyIndex modelAccessPolicyIndex,
    String compilerVersion,
    CompilationOptions options);

public ModelAccessPolicyIndex modelAccessPolicyIndex();
```

SemanticDigestInput must include canonical System identities, RuleView composite identities and PolicyIndex authorization-significant entries before digest computation。

P2 candidate publication must call `CompiledModelSet.published(...)`, not legacy 8-arg constructor。

## 10. ProtectedExecutionBridge direct invocation API

Formal decision：`DEC-P2-DIRECT-BRIDGE-AUTHORITY-001`。

```java
public final class ProtectedExecutionBridge {
    public ProtectedAccessResult execute(
        ModelAccessRuleKey requestedRuleKey,
        AccessOperation operation,
        RuntimeExecutionFrameId frameId,
        RuntimeResolutionOwnerId ownerResolutionId,
        Optional<RuntimeCollectionCursorId> collectionCursorId);
}
```

No token/recognizes/claim API。

Bridge creation occurs from current EngineContext/runtime composition and binds：

```text
EngineContext/runtime identity
AccessConsumerIrKey (provenance only in current P2)
ProtectedTargetResolutionPort
ProtectedOperationExecutionPort
```

Per call rule/op/frame/owner/cursor are explicit arguments。

Current P2 authorization interpretation：requested exact key/op is allowed only if current `ModelAccessPolicyIndex.find(key)` returns the matching compiler-published rule；no consumer->rule/op binding is required in this Revision。

## 11. Internal issued-pair contract

External caller does not call `issueInvocation(...)` or construct issued objects。Starter may keep package-private internal：

```text
issueInvocation(ruleKey,op,frame,owner,cursor,...)
IssuedProtectedAccessResolutionContext
IssuedProtectedOperationIntent
IssuedInvocationRecord
requireIssuedPair(...)
```

Internal pair exists to bind one invocation to resolved target/capability and prevent implementation substitution after issuance；it is not a token authority system。

## 12. Guard / Gateway

Guard exact policy selection：

```text
engineContext.modelAccessPolicyIndex().find(requestedRuleKey)
```

exactly once。

- key absent -> `POLICY_NOT_FOUND`；
- operation inconsistent with key -> invalid invocation / DENY；
- STATIC_ALLOW -> verifier/evaluator 0；
- RUNTIME_GUARD_REQUIRED -> exact selected plan/requirement -> verifier；
- resolver/gateway/verifier/adapters perform zero policy lookup。

Gateway reserves/consumes one capability atomically and executes only capability-bound target + operation port。

## 13. Concurrency API semantics

`ProtectedExecutionBridge.execute(...)` is thread-safe/stateless with respect to independent invocations。Identical scalar arguments submitted concurrently are independent calls and may both succeed if each independently passes policy/proof。P2 does not expose duplicate-suppression/idempotency API。

Same capability concurrent terminal execution must succeed at most once。

## 14. Stable compile/runtime failures

Compile：`MIX-SYSTEM-DUPLICATE`、`MIX-SYSTEM-UNKNOWN`、`MIX-RULEVIEW-SYSTEM-REQUIRED`、`MIX-RULEVIEW-DUPLICATE`、`MIX-RULEVIEW-UNKNOWN-SYSTEM`、`MIX-RULEVIEW-UNKNOWN-RULE`、`MIX-MODEL-PATH-INVALID`、`MIX-MODEL-ACCESS-DENIED`、`MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED`。

Runtime：`POLICY_NOT_FOUND`、`CONTEXT_IDENTITY_MISMATCH`、`MODEL_ACCESS_GUARD_BYPASS`、`PROTECTED_ACCESS_ADAPTER_UNAVAILABLE`、`RUNTIME_BINDING_REQUIRED`、`RUNTIME_BINDING_PROOF_INVALID`、`RUNTIME_BINDING_STALE`、`RUNTIME_BINDING_PLAN_MISMATCH`、`RUNTIME_BINDING_OPERATION_TARGET_MISMATCH`、`RUNTIME_BINDING_CAPABILITY_CONSUMED`、`GUARD_UNAVAILABLE`。

## 15. Gate

`DESIGN-P2-R14 = NEEDS_REVIEW / MACHINE_BLOCKED`。Exact Architecture/ApiContract/Develop/Impact/CrossModule/Concurrency Review required before TestDesign can be accepted or TDD may begin。

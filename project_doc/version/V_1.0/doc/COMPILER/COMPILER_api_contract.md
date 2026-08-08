# COMPILER P2 API 契约

> Revision：`DESIGN-P2-R11`。输入：`BM-R12` candidate。状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。
> 本 Revision 在 R10 repository ownership 基础上冻结两个剩余 implementation-ready API contract：trusted issued input authority 与 single immutable policy authority。生产实现必须 Java 8 compatible。

## 1. Maven / package ownership

| Concern | Maven module | Package / owner |
|---|---|---|
| Neutral access contracts/facts | `dec-core-context` | `dec.core.context.model.access.*` |
| Immutable policy index | `dec-core-context` | `dec.core.context.model.access.ModelAccessPolicyIndex` |
| Classifier/rule/plan/policy publication | `dec-core-compiler` | `dec.core.compiler.access.*` |
| Concrete protected runtime | `dec-core-starter` | `dec.core.starter.access.*` |
| Package-private issued input implementations | `dec-core-starter` | `dec.core.starter.access.*` |
| Trusted framework adapter SPI | `dec-core-starter` | `dec.core.starter.access.spi.*` |
| Real source integration fixture | `dec-demo` | tests/resources |

No P2 `dec-core-runtime` module。No context -> compiler/starter reverse dependency；no compiler -> starter dependency；starter does not add a P2-only `dec-core-model` dependency。

## 2. Compatibility

- Java release 8；禁止 record / sealed class / `Map.of` / `Map.copyOf` 等 Java 9+ API。
- `EngineContext` 保持 `public final class`、现有单参 constructor 与 `compiledModelSet()/modelSet()/projection()`。
- P2 additive only；no bare-name RuleView lookup。
- Context remains neutral；starter concrete runtime is composed around immutable EngineContext。

## 3. Exact rule / classifier

```java
public enum AccessCompilationStatus { STATIC_ALLOW, RUNTIME_GUARD_REQUIRED }
public enum DynamicBindingClassification { STATIC_BOUND, RUNTIME_OBJECT_BOUND }

public interface DynamicBindingClassifier {
    DynamicBindingResult classify(ResolvedAccessConsumerIr accessIr);
}
```

Frozen：
- `DIRECT_EXACT -> STATIC_BOUND -> STATIC_ALLOW`；
- `EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND -> RUNTIME_GUARD_REQUIRED`；
- unsupported dynamic form -> `MIX-MODEL-ACCESS-DYNAMIC-BINDING-UNSUPPORTED`。

```java
public final class CompiledModelAccessRule {
    public ModelAccessRuleKey key();
    public AccessCompilationStatus status();
    public Optional<RuntimeAccessRequirement> runtimeRequirement();
    public Optional<RuntimeBindingPlan> runtimeBindingPlan();
    public SourceRef sourceRef();
}
```

STATIC_ALLOW has no plan/requirement；runtime-required has exactly one compiler-published plan + requirement。

## 4. `ModelAccessPolicyIndex` — unique runtime policy authority

```java
package dec.core.context.model.access;

public final class ModelAccessPolicyIndex {
    public Optional<CompiledModelAccessRule> find(ModelAccessRuleKey key);
    public Set<ModelAccessRuleKey> keys();
}
```

Contract：

- immutable exact-key snapshot；
- duplicate key / key != rule.key -> publication failure；
- no wildcard/fuzzy/parent/bare-name runtime lookup；
- runtime wildcard keys absent；
- `keys()` deterministic/read-only；
- constructing another instance does not make it authority：only the instance embedded in current `CompiledModelSet` is authoritative。

## 5. `CompiledModelSet` / `EngineContext` policy surfaces

Additive API：

```java
public final class CompiledModelSet {
    public ModelAccessPolicyIndex modelAccessPolicyIndex();
}

public final class EngineContext {
    public ModelAccessPolicyIndex modelAccessPolicyIndex();
}
```

Normative：

- compiler assembles/publishes `ModelAccessPolicyIndex` in the same immutable CompiledModelSet closure；
- `EngineContext.modelAccessPolicyIndex()` returns the same current immutable authority as `compiledModelSet().modelAccessPolicyIndex()`；
- starter must not copy it into a second mutable/independently-derived policy Map；
- semantic digest covers canonical ordered index entries and all authorization-semantic fields (exact key/status/runtime requirement identity/runtime plan identity/stable rule semantics)；
- equivalent source order -> same canonical index/digest；policy semantic change -> changed semantic digest。

## 6. Public read contracts for execution inputs

```java
public interface ProtectedAccessResolutionContext {
    String engineContextId();
    AccessConsumerIrKey accessConsumerIrKey();
    RuntimeExecutionFrameId frameId();
    RuntimeResolutionOwnerId ownerResolutionId();
    Optional<RuntimeCollectionCursorId> collectionCursorId();
}

public interface ProtectedOperationIntent {
    ModelAccessRuleKey requestedRuleKey();
    AccessOperation operation();
}
```

These interfaces are **read views, not authorization/mint contracts**. A caller-created implementation with arbitrary getters is never trusted merely because it satisfies the Java interface。

Production implementation classes are package-private starter implementation details：

```text
dec.core.starter.access.IssuedProtectedAccessResolutionContext
dec.core.starter.access.IssuedProtectedOperationIntent
```

No public/protected constructor/factory on these implementations；no `ProtectedAccessRuntime` mint API。

## 7. Issued pair registry contract

`ContextLocalProtectedAccessRegistry` owns a private/package-private `IssuedInvocationRecord` keyed by the exact object identities of the issued context + intent pair。

The record binds：

```text
EngineContext identity + engineContextId
AccessConsumerIrKey
RuntimeExecutionFrameId
RuntimeResolutionOwnerId
optional RuntimeCollectionCursorId
ModelAccessRuleKey
AccessOperation
hidden payload/action identity
trusted adapter/consumer binding
issuance lifecycle
```

Production runtime MUST NOT trust public getters as authoritative values. It must first resolve the exact issued record by reference identity/pair membership and then use record values downstream。

## 8. Trusted issuance contract

Issued context+intent can only originate from starter internal issuance driven by composition-time trusted framework execution adapters/state：

```text
trusted adapter registry frozen at runtime composition
 -> starter internal issuance
 -> ContextLocalProtectedAccessRegistry.issueInvocation(...)
 -> exact issued context + exact issued intent
 -> authoritative IssuedInvocationRecord
```

`issueInvocation(...)` is not public API. Per-call business code cannot ask starter to sign arbitrary `consumerIrKey/frame/owner/cursor/rule/op` facts. Adapter registration is framework/application composition trust boundary；Rule/change/custom-action/business caller is not。

## 9. Public runtime facade and mandatory authenticity gate

```java
package dec.core.starter.access;

public final class ProtectedAccessRuntime {
    public ProtectedAccessResult execute(
        ProtectedAccessResolutionContext context,
        ProtectedOperationIntent intent);
}
```

First runtime action MUST be：

```text
ContextLocalProtectedAccessRegistry.requireIssuedPair(context,intent)
```

Required behavior：

- unknown/caller-implemented context or intent -> `PROTECTED_ACCESS_INPUT_UNTRUSTED`；
- issued context A + issued intent B -> `PROTECTED_ACCESS_INPUT_PAIR_MISMATCH`；
- expired/foreign frame/owner/cursor/context or issued-record inconsistency -> fail closed；
- on either authenticity failure: target resolver calls=0, capability issuance=0, PolicyIndex lookup=0, protected operation=0, external effects=0。

After PASS, resolver consumes authoritative issued record rather than re-deriving authority from caller getters。

## 10. `ResolvedProtectedAccess`

```java
public final class ResolvedProtectedAccess {
    public String capabilityId();
    public String engineContextId();
    public ModelAccessRuleKey requestedRuleKey();
    public AccessOperation operation();
    public RuntimeExecutionFrameId executionFrameId();
    // no public/protected constructor/factory/raw target getter/selected-policy setter
}
```

Issued only after input-authenticity PASS. Hidden registry state binds actual target, operation payload/action identity, owner/cursor/provenance, adapter and one-shot lifecycle。

## 11. Starter concrete implementation ownership

Package `dec.core.starter.access`：

```text
ProtectedAccessRuntime
ProtectedAccessRuntimeFactory (composition helper; public exposure may be narrowed)
DefaultProtectedAccessResolver
DefaultProtectedAccessGateway
DefaultModelAccessGuard
DefaultRuntimeBindingVerifier
ContextLocalProtectedAccessRegistry
IssuedProtectedAccessResolutionContext (package-private)
IssuedProtectedOperationIntent (package-private)
```

### 11.1 Resolver

```text
input authenticity PASS record
 -> target resolution through trusted adapter
 -> target+operation+frame/owner/cursor/provenance/adapter binding
 -> one-shot capability
```

PolicyIndex lookup count = 0。

### 11.2 Guard

Only supported policy read：

```java
engineContext.modelAccessPolicyIndex()
    .find(access.requestedRuleKey())
```

Exactly once。

- STATIC_ALLOW -> plan/requirement absent, RuntimeBindingVerifier=0, evaluator=0；
- RUNTIME_GUARD_REQUIRED -> exact plan/requirement, RuntimeBindingVerifier=1 before ALLOW；
- no definitions scan / TypedDefinitionRegistries policy reconstruction / starter secondary Map。

### 11.3 Gateway

Guard exactly once；Gateway PolicyIndex lookup=0；same capability-bound target+operation only；terminal consume；no `execute(capability,target)`/raw-object/callback substitution。

### 11.4 Runtime verifier

PolicyIndex lookup=0；selected runtime rule/plan only；STATIC_ALLOW calls=0。

## 12. Trusted adapter SPI

```text
dec.core.starter.access.spi.ProtectedTargetResolutionPort
dec.core.starter.access.spi.ProtectedOperationExecutionPort
dec.core.starter.access.spi.ProtectedAccessAdapterRegistry
```

Adapters are registered/frozen at runtime composition. No per-call adapter/raw target callback. Future execution modules depend on starter SPI; starter never depends upward on P3-P7 business executor modules。

## 13. Production consumer integration

```text
framework trusted execution state
 -> starter-issued exact context+intent pair
 -> ProtectedAccessRuntime.execute(pair)
 -> input authenticity gate
 -> resolver capability
 -> gateway
 -> Guard single immutable policy-index lookup
 -> static fast path OR runtime proof
 -> same-bound operation
```

Unsupported/forbidden：caller-created context/intent authority、READ->WRITE/EXECUTE intent substitution、second policy registry、caller policy lookup、capability mint、second target after ALLOW。

## 14. Stable reasons

Runtime at least：

- `PROTECTED_ACCESS_INPUT_UNTRUSTED`
- `PROTECTED_ACCESS_INPUT_PAIR_MISMATCH`
- `POLICY_NOT_FOUND`
- `CONTEXT_IDENTITY_MISMATCH`
- `MODEL_ACCESS_GUARD_BYPASS`
- `PROTECTED_ACCESS_ADAPTER_UNAVAILABLE`
- `RUNTIME_BINDING_REQUIRED`
- `RUNTIME_BINDING_PROOF_INVALID`
- `RUNTIME_BINDING_STALE`
- `RUNTIME_BINDING_PLAN_MISMATCH`
- `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH`
- `RUNTIME_BINDING_CAPABILITY_CONSUMED`
- `GUARD_UNAVAILABLE`
- `STATIC_ALLOW`
- `RUNTIME_ALLOW`
- `RUNTIME_DENY`

## 15. Review gate

This API remains candidate contract, not implementation Evidence。FND-004 remains `PARTIAL_FIX_PROPOSED / OPEN` until exact ApiContract/Architecture/Develop/Impact/CrossModule/Concurrency Reviews and RC9 machine/risk Evidence validate trusted issuance + single policy authority。Implementation Plan/TDD/Development remain BLOCKED。
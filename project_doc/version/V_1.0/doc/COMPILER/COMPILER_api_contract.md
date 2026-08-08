# COMPILER P2 API 契约

> Revision：`DESIGN-P2-R12`。输入：`BM-R12` candidate。状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。
> 本 Revision 在 R11 trusted-input + single-policy-authority 基础上，冻结 production-reachable bridge、validated policy-index construction、policy-aware CompiledModelSet publication 与 legacy fail-closed compatibility。生产实现必须 Java 8 compatible。

## 1. Maven / package ownership

| Concern | Maven module | Package / owner |
|---|---|---|
| Neutral access contracts/policy index | `dec-core-context` | `dec.core.context.model.access.*` |
| `CompiledModelSet` / `EngineContext` additive publication/read API | `dec-core-context` | existing packages |
| Classifier/rule/plan/index publication + digest binding | `dec-core-compiler` | existing compiler/modelaccess/pass/compiled packages |
| Concrete protected runtime + bridge | `dec-core-starter` | `dec.core.starter.access.*` |
| Trusted execution/target/operation SPI | `dec-core-starter` | `dec.core.starter.access.spi.*` |
| Real source/bridge integration fixture | `dec-demo` | tests/resources |

No new Maven runtime module. No context -> starter/compiler reverse dependency; no compiler -> starter dependency; starter does not add a P2-only `dec-core-model` dependency.

## 2. Compatibility

- Java release 8 only；禁止 record / sealed / `Map.of` / `Map.copyOf` 等 Java 9+ API。
- `EngineContext` 保持 `public final class`、现有 `EngineContext(CompiledModelSet)` constructor、`compiledModelSet()/modelSet()/projection()`。
- `CompiledModelSet` 现有八参数 public constructor 保留原 signature。
- P2 新增 API additive；legacy constructor 语义固定为 empty-policy fail closed，不从 definitions 重建 policy。
- P2 production compiler 使用新的 policy-aware publication path，不把 legacy constructor 当作新 P2 publication path。

## 3. Exact compiled access rule

```java
public enum AccessCompilationStatus {
    STATIC_ALLOW,
    RUNTIME_GUARD_REQUIRED
}

public enum DynamicBindingClassification {
    STATIC_BOUND,
    RUNTIME_OBJECT_BOUND
}

public final class CompiledModelAccessRule {
    public ModelAccessRuleKey key();
    public AccessCompilationStatus status();
    public Optional<RuntimeAccessRequirement> runtimeRequirement();
    public Optional<RuntimeBindingPlan> runtimeBindingPlan();
    public SourceRef sourceRef();
}
```

Invariant：

- `DIRECT_EXACT -> STATIC_BOUND -> STATIC_ALLOW`；plan/requirement absent。
- `EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND -> RUNTIME_GUARD_REQUIRED`；exact plan + EXACT_RUNTIME_BINDING requirement present。
- unsupported dynamic form compile ERROR。

## 4. `ModelAccessPolicyIndex` construction/read contract

```java
package dec.core.context.model.access;

public final class ModelAccessPolicyIndex {
    private ModelAccessPolicyIndex(...);

    public static ModelAccessPolicyIndex empty();

    public static ModelAccessPolicyIndex of(
        Iterable<CompiledModelAccessRule> rules);

    public Optional<CompiledModelAccessRule> find(
        ModelAccessRuleKey key);

    public Set<ModelAccessRuleKey> keys();
}
```

### 4.1 `of(...)` validation

`of(...)` 是 compiler 跨模块调用的正式 validated construction API：

1. null iterable/rule/key -> `NullPointerException`/stable validation failure；
2. key 只从 `rule.key()` 读取，不接受 separate map key；
3. duplicate exact key -> reject；
4. STATIC_ALLOW + runtime plan/requirement -> reject；
5. RUNTIME_GUARD_REQUIRED 缺 plan/requirement 或 requirement kind 不为 EXACT_RUNTIME_BINDING -> reject；
6. wildcard/fuzzy/non-canonical ModelPath key -> reject；
7. defensive immutable snapshot；
8. deterministic key iteration/canonical serialization；
9. no public mutator / no builder continuation after freeze。

使用 `Iterable` 而不是 `Map` 是规范要求，用于让 factory 自己看见并拒绝 duplicate。

### 4.2 `empty()`

`empty()` 返回 immutable deterministic empty index。它只用于明确的 no-policy/legacy compatibility；它不扫描 `definitions()` 或 `TypedDefinitionRegistries`。

## 5. `CompiledModelSet` publication contract

### 5.1 Legacy constructor — signature retained, fail closed

现有 public constructor 保持：

```java
public CompiledModelSet(
    PublishedSourceManifest sourceManifest,
    Registry<DefinitionKey, CompiledDefinition> definitions,
    DeferredRegistry deferred,
    List<Diagnostic> diagnostics,
    DigestPair digestPair,
    String compilerVersion,
    String schemaVersion,
    String optionsVersion);
```

Normative implementation：等价于使用当前 snapshot/validation 逻辑并固定：

```text
modelAccessPolicyIndex = ModelAccessPolicyIndex.empty()
```

禁止：

- 从 `definitions()` 推断/重建 access policy；
- 从 `typedRegistries()` 推断/重建 access policy；
- 自动扫描 compiler-private modelaccess classes；
- 为 legacy caller 注入 starter-side policy Map。

因此 legacy constructed `EngineContext` 的 protected access 会 exact miss -> `POLICY_NOT_FOUND`，这是明确的 fail-closed compatibility。

### 5.2 P2 policy-aware publication factory

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
```

Production P2 compiler MUST use this factory after model-access compilation。Factory snapshots/validates all existing facts and requires non-null immutable policy index。

```java
public ModelAccessPolicyIndex modelAccessPolicyIndex();
```

`equals/hashCode` 必须包含 policy index；`toString` 至少可稳定显示 policy rule count，但不得泄露 runtime token/target state。

## 6. `EngineContext` additive read surface

```java
public final class EngineContext {
    // existing API retained
    public ModelAccessPolicyIndex modelAccessPolicyIndex();
}
```

Return semantics：直接返回 `compiledModelSet().modelAccessPolicyIndex()` 的 immutable authority；不复制、不 rebuild、不 cache second policy map。

## 7. Compiler digest/publication internal contract

虽然以下不是 cross-module public API，但属于 implementation-ready signature contract。

`SemanticDigestInput` 必须持有 immutable policy index semantic view；`DigestBoundCompiledInput` 必须把 index 与 digest 原子绑定：

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

P2 production sequence：

```text
ModelAccessPolicyIndex.of(compiledRules)
 -> SemanticDigestInput(...same index...)
 -> digest compute
 -> DigestBoundCompiledInput(...same index + digest...)
 -> CompiledModelSetBuilder.FrozenInput
 -> CompiledModelSet.published(...same index + digest...)
```

`CompiledModelSetBuilder.FrozenInput.candidate(...)` 在 P2 production path 不得继续调用 legacy 8-arg constructor。

## 8. Semantic digest contract

Policy canonical contribution 至少包含：

- exact `ModelAccessRuleKey`；
- `AccessCompilationStatus`；
- runtime requirement identity/kind；
- runtime binding plan identity/semantic fields；
- 影响授权语义的稳定 compiled rule fields。

规则：

- equivalent source ordering -> same canonical policy entries + same semantic digest；
- policy add/remove/status/plan/requirement semantic change -> semantic digest changes；
- capability/bridge/token/issued pair/target identity/one-shot state 不进入 semantic digest。

Legacy 8-arg direct constructor 保留 caller supplied `DigestPair`，但该路径不被 production compiler 当作 P2 policy-aware publication Evidence。

## 9. Protected execution bridge — public production invocation API

### 9.1 Opaque token

```java
package dec.core.starter.access.spi;

public interface ProtectedExecutionToken {
    // marker only
    // NO consumerIrKey/ruleKey/operation/frame/owner/cursor getters
}
```

Caller 可以实现 marker，但这不产生 authority；只有 exact bridge 已绑定的 trusted state port 能识别的 token 才合法。

### 9.2 Public bridge capability

```java
package dec.core.starter.access;

public final class ProtectedExecutionBridge {
    // no public/protected constructor
    // no public bind/rebind/mint API

    public ProtectedAccessResult execute(
        ProtectedExecutionToken token);
}
```

Bridge construction/registration 发生于 `ProtectedAccessRuntimeFactory` composition。每个 bridge immutable 绑定：

```text
EngineContext/runtime identity
AccessConsumerIrKey
exact ModelAccessRuleKey
AccessOperation
ProtectedExecutionStatePort
ProtectedTargetResolutionPort
ProtectedOperationExecutionPort
```

Per-call API **不得**接受上述 scalar authority facts。

### 9.3 Trusted state port + receiver

```java
package dec.core.starter.access.spi;

public interface ProtectedExecutionStatePort {
    boolean recognizes(ProtectedExecutionToken token);

    RuntimeExecutionFrameId frameId(
        ProtectedExecutionToken token);

    RuntimeResolutionOwnerId ownerResolutionId(
        ProtectedExecutionToken token);

    Optional<RuntimeCollectionCursorId> collectionCursorId(
        ProtectedExecutionToken token);
}

public interface ProtectedExecutionBridgeReceiver {
    void bind(ProtectedExecutionBridge bridge);
}
```

- ports/receiver 仅在 composition-time registry 注册并 freeze；
- factory 为 exact registration 创建 bridge 并一次性交给 receiver；
- execution adapter 通过 receiver 持有该 exact bridge capability，不通过 public `bridgeFor(ruleKey)` 查询任意 bridge；
- token 的 frame/owner/cursor 只能由该 bound state port 从其可信 execution state 派生；
- consumer/rule/operation 永远取 bridge immutable binding，不从 token 或 caller input 读取。

## 10. `ProtectedAccessRuntime` visibility contract

```java
public final class ProtectedAccessRuntime {
    // public composition holder/factory product
    // no public execute(context,intent) production entry in R12
}
```

以下均为 package-private starter internal seam：

```text
issueInvocation(...)
executeIssuedPair(...)
IssuedProtectedAccessResolutionContext
IssuedProtectedOperationIntent
IssuedInvocationRecord
```

Public callers/future modules 不需要也不能直接调用这些 seam。外部 production path 只使用 `ProtectedExecutionBridge.execute(token)`。

## 11. Internal issued-input authority

Neutral read interfaces 可保留：

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

它们不是 public invocation/mint authority。Internal registry 按 object identity + exact pair relationship 验证：

```text
requireIssuedPair(context,intent)
```

Unknown implementation -> `PROTECTED_ACCESS_INPUT_UNTRUSTED`；A-context+B-intent -> `PROTECTED_ACCESS_INPUT_PAIR_MISMATCH`。失败发生在 target resolution/capability/policy lookup 前。

## 12. Bridge execution sequence

```text
bridge.execute(token)
 -> bridge/context still active
 -> bound statePort.recognizes(token)
      false -> PROTECTED_EXECUTION_TOKEN_UNTRUSTED
 -> derive frame/owner/cursor from bound port
 -> use bridge-bound consumer/rule/operation
 -> internal issueInvocation
 -> internal requireIssuedPair
 -> resolver binds target + operation -> ResolvedProtectedAccess
 -> gateway
 -> guard exact EngineContext.modelAccessPolicyIndex().find(ruleKey) = 1
 -> STATIC_ALLOW OR runtime verifier
 -> same bound target operation
 -> consume capability
```

Token-untrusted failure counters：issued pair=0、resolver=0、capability=0、Guard=0、policy lookup=0、operation=0、effects=0。

Forbidden public shapes：

- `execute(context,intent)` external production entry；
- `issueInvocation(...)` public；
- `bridgeFor(ruleKey)` arbitrary lookup；
- `execute(token, ruleKey)`；
- `execute(token, operation)`；
- `execute(token, frame/owner/cursor)`；
- `execute(capability,target)`；
- per-call target/executor callback。

## 13. Guard / Gateway responsibilities

`DefaultModelAccessGuard`：

```text
engineContext.modelAccessPolicyIndex().find(access.requestedRuleKey())
```

exact lookup exactly once。No definitions scan、typed-registry rebuild、secondary policy Map。

- STATIC_ALLOW -> no plan/requirement；RuntimeBindingVerifier=0；evaluator=0。
- RUNTIME_GUARD_REQUIRED -> exact plan/requirement -> verifier。
- resolver/gateway/verifier/adapter policy lookup=0。

Gateway executes only registry-bound same target/operation/port and consumes one-shot capability。

## 14. Stable runtime reasons

At least：

- `PROTECTED_EXECUTION_TOKEN_UNTRUSTED`
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

本 API 是 candidate contract，不是 implementation Evidence。FND-004 / FND-015 / FND-016 继续 `PARTIAL_FIX_PROPOSED / OPEN`，直到 exact DESIGN-P2-R12 ApiContract/Architecture/Develop/Impact/CrossModule/Concurrency Review 接受 bridge reachability、validated policy construction/publication 和 legacy compatibility。Implementation Plan/TDD/Development remain BLOCKED。
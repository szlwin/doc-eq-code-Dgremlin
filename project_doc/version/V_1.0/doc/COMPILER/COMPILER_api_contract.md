# COMPILER P2 API 契约

> Revision：`DESIGN-P2-R13`。输入：`BM-R12` candidate。状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。
> 本 Revision 保持 R12 的 PolicyIndex construction/publication 结论，撤销 `ProtectedExecutionToken` 模型，改为 direct-argument `ProtectedExecutionBridge.execute(...)`。生产实现必须 Java 8 compatible。

## 1. Maven / package ownership

| Concern | Maven module | Package / owner |
|---|---|---|
| Neutral access contracts/policy index | `dec-core-context` | `dec.core.context.model.access.*` |
| `CompiledModelSet` / `EngineContext` publication/read API | `dec-core-context` | existing packages |
| Rule/plan/index publication + digest binding | `dec-core-compiler` | compiler/modelaccess/pass/compiled packages |
| Bridge/runtime/Guard/Gateway/registry | `dec-core-starter` | `dec.core.starter.access.*` |
| Target/operation SPI | `dec-core-starter` | `dec.core.starter.access.spi.*` |
| Real direct-bridge integration | `dec-demo` | tests/resources |

No new Maven runtime module；no context -> starter/compiler reverse dependency；no compiler -> starter dependency；starter 不新增 P2-only `dec-core-model` 业务依赖。

## 2. Java / compatibility

- Java release 8 only；禁止 record/sealed/Java9+ collection factories。
- `EngineContext` 保持 final、现有 `EngineContext(CompiledModelSet)`、`compiledModelSet()/modelSet()/projection()`。
- `CompiledModelSet` 现有八参数 public constructor 保持原 signature。
- P2 API additive；legacy constructor = empty-policy fail closed。

## 3. Compiled access rule

```java
public enum AccessCompilationStatus {
    STATIC_ALLOW,
    RUNTIME_GUARD_REQUIRED
}

public final class CompiledModelAccessRule {
    public ModelAccessRuleKey key();
    public AccessCompilationStatus status();
    public Optional<RuntimeAccessRequirement> runtimeRequirement();
    public Optional<RuntimeBindingPlan> runtimeBindingPlan();
    public SourceRef sourceRef();
}
```

STATIC_ALLOW 无 plan/requirement；RUNTIME_GUARD_REQUIRED 必须有 exact plan + EXACT_RUNTIME_BINDING requirement。

## 4. ModelAccessPolicyIndex

```java
public final class ModelAccessPolicyIndex {
    public static ModelAccessPolicyIndex empty();
    public static ModelAccessPolicyIndex of(
        Iterable<CompiledModelAccessRule> rules);
    public Optional<CompiledModelAccessRule> find(
        ModelAccessRuleKey key);
    public Set<ModelAccessRuleKey> keys();
}
```

`of(...)` 必须验证 duplicate/null/exact-key/canonical path/STATIC-RUNTIME state invariant，并冻结 immutable deterministic snapshot。不得暴露 mutable raw-map authority constructor。

## 5. CompiledModelSet publication

Legacy constructor 保持：

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

其 policy 语义固定：

```text
ModelAccessPolicyIndex.empty()
no reconstruction from definitions()/typedRegistries()
protected access exact miss -> POLICY_NOT_FOUND
```

P2 production path：

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

`equals/hashCode` 必须把 policy index 视为 published model fact。

## 6. EngineContext read surface

```java
public final class EngineContext {
    public ModelAccessPolicyIndex modelAccessPolicyIndex();
}
```

直接返回 `compiledModelSet().modelAccessPolicyIndex()` 的 immutable authority，不复制、不 rebuild。

## 7. Compiler digest/publication contract

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

Production sequence：

```text
ModelAccessPolicyIndex.of(compiledRules)
 -> SemanticDigestInput(same index)
 -> digest compute
 -> DigestBoundCompiledInput(same index + digest)
 -> CompiledModelSetBuilder.FrozenInput
 -> CompiledModelSet.published(...same index + digest...)
```

Policy authorization semantics 进入 semantic digest；runtime bridge/capability/registry state不进入 digest。

## 8. ProtectedExecutionBridge — R13 public production API

```java
package dec.core.starter.access;

public final class ProtectedExecutionBridge {
    // no public/protected constructor

    public ProtectedAccessResult execute(
        ModelAccessRuleKey requestedRuleKey,
        AccessOperation operation,
        RuntimeExecutionFrameId frameId,
        RuntimeResolutionOwnerId ownerResolutionId,
        Optional<RuntimeCollectionCursorId> collectionCursorId);
}
```

Bridge 由 `ProtectedAccessRuntimeFactory` 创建，composition 时固定：

```text
EngineContext/runtime identity
AccessConsumerIrKey
ProtectedTargetResolutionPort
ProtectedOperationExecutionPort
```

Per-call 显式接受：

```text
requestedRuleKey
operation
frameId
ownerResolutionId
optional collectionCursorId
```

当前 Revision 按用户决策允许 caller 提供这些值；API 不增加 token/claim/recognized-execution authority 层。

## 9. 参数校验

以下必须在 resolver/capability/Guard/policy lookup/operation 前拒绝：

- null ruleKey；
- null operation；
- null frameId；
- null ownerResolutionId；
- null Optional wrapper；
- cursor 与 runtime-required plan 明确不兼容时的 stable validation/runtme DENY；
- bridge/runtime 已关闭或 Context identity 不可用。

Stable candidate reason：

```text
PROTECTED_ACCESS_ARGUMENT_INVALID
```

该 reason 是 direct invocation 输入形态错误，不是 policy DENY。

## 10. Removed token API

R13 明确**不存在**以下 public P2 contract：

```text
ProtectedExecutionToken
ProtectedExecutionStatePort
ProtectedExecutionBridgeReceiver
recognizes(token)
claim(token)
beginExecution(token)
token lease/replay/consumed state
PROTECTED_EXECUTION_TOKEN_UNTRUSTED
```

也不存在 `bridge.execute(token)`。

## 11. ProtectedAccessRuntime / internal issuance

`ProtectedAccessRuntime` 可以继续作为 public composition holder。以下为 starter package-private internal seam：

```text
issueInvocation(...)
executeIssuedPair(...)
IssuedProtectedAccessResolutionContext
IssuedProtectedOperationIntent
IssuedInvocationRecord
```

Public bridge invocation 参数与 bridge-bound consumer/context/ports 一起生成 internal issued pair。

## 12. Execution sequence

```text
bridge.execute(ruleKey, operation, frameId, ownerId, cursorId)
 -> validate call arguments
 -> internal issueInvocation(
      bridge consumer,
      ruleKey/op/frame/owner/cursor)
 -> internal pair check
 -> resolver binds actual target
 -> one-shot capability
 -> Gateway
 -> Guard EngineContext.modelAccessPolicyIndex().find(ruleKey) exactly once
 -> STATIC_ALLOW or runtime verifier
 -> same capability-bound target operation
 -> consume capability
```

## 13. Concurrency contract

`ProtectedExecutionBridge` 必须可被多线程并发调用。

不同参数并发：独立 invocation。

相同参数并发：**仍视为两个独立 invocation**；R13 不提供 execution-occurrence replay suppression，也不要求只成功一次。

唯一 one-shot 原子性要求：

```text
same ResolvedProtectedAccess capability
 -> concurrent reserve/execute
 -> terminal success <= 1
```

runtime verifier 仍需在实际 operation 前做 Context/frame/cursor/rule/plan/membership stale revalidation。

## 14. Guard / Gateway

Guard exact lookup：

```text
engineContext.modelAccessPolicyIndex().find(requestedRuleKey) = 1
```

Resolver/Gateway/verifier/target port/operation port policy lookup = 0。

Gateway 只能执行 resolver/capability 已绑定的 actual target 与 operation port；禁止 `execute(capability,targetB)` 等二次 target substitution。

## 15. Stable runtime reasons

至少：

- `PROTECTED_ACCESS_ARGUMENT_INVALID`
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

## 16. Review gate

本 API 是 candidate contract。FND-004/FND-015/FND-016 仍 formal OPEN；FND-007/FND-019 不再承担 token replay/atomic-claim 问题。Implementation Plan/TDD/Development 在 exact `DESIGN-P2-R13` specialist Review 与 machine lifecycle 前继续 BLOCKED。
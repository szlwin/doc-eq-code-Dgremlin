# COMPILER P2 详细设计

> Revision：`DESIGN-P2-R13`。Base：`DESIGN-P2-R12`，输入 Business Model candidate：`BM-R12`。
> 状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。本 Revision 按用户明确决策撤销 R12 的 `ProtectedExecutionToken / ProtectedExecutionStatePort.recognizes(...) / bridge receiver` 模型，改为 production consumer 直接调用参数式 `ProtectedExecutionBridge.execute(ruleKey, operation, frameId, ...)`。当前阶段**不把 caller 可选择 rule/op 作为阻断问题**；该取舍属于本 Revision 明确接受的 P2 设计边界。不新增 FND-020，不改变 BM-R12 的 Guard/PolicyIndex/target-binding 业务语义。
> 当前 canonical Business Model 仍是历史 BM-R07；正式 RC9 reopen/publish、current-revision risk Evidence 与 exact independent Review 完成前，本 Design 不得 PASSED。

## 1. 设计目标与本轮取舍

1. 所有 protected READ/WRITE/EXECUTE 继续通过 starter-owned runtime -> Gateway -> Guard；`STATIC_ALLOW` 仍只是 Guard exact lookup 后的内部 fast path。
2. Runtime ModelPath lookup exact-only；wildcard 只允许 compiler finite canonical expansion。
3. 唯一权限 authority 继续是 compiler-published、`CompiledModelSet`-owned immutable `ModelAccessPolicyIndex`。
4. `DefaultModelAccessGuard` 继续只允许 current `EngineContext.modelAccessPolicyIndex()` exact lookup 一次。
5. actual target 与最终 operation target 必须由同一个 invocation/capability 绑定；capability A 不得执行 target B。
6. **撤销 execution-token trust model。不存在 `ProtectedExecutionToken`、`recognizes(token)`、token replay/claim/lease/snapshot contract。**
7. **外部 production API 改成显式参数式 bridge invocation。caller 可以在当前 Revision 直接提交 `ModelAccessRuleKey`、`AccessOperation`、frame/owner/cursor 等调用事实。**
8. 本轮按用户明确授权，暂不把“caller 可以选择另一个 ruleKey / READ 改 WRITE/EXECUTE”的 authority 扩大问题作为 P1；后续若重新要求收紧，再单独 Review，不在本 Revision 通过 token 隐式解决。
9. 每次 `bridge.execute(...)` 都是一个独立 invocation；相同参数被调用两次时默认可产生两个独立 invocation/capability，不定义 execution-token replay DENY。
10. capability 自身仍 one-shot；同一 capability 并发执行最多一个 terminal success。
11. P2 不实现 P3～P7 完整 Rule/change/action/query 业务执行语义。

## 2. Repository-valid Maven ownership

```text
dec-core-context
  dec.core.context.model.access.*
  -> neutral access contracts / rule / plan / policy index
  -> CompiledModelSet / EngineContext policy read surface

       ^ existing dependency
       |
dec-core-compiler
  -> model-access compilation/classification
  -> ModelAccessPolicyIndex construction
  -> semantic digest + DigestBoundCompiledInput publication

       ^ existing starter composition dependency
       |
dec-core-starter
  dec.core.starter.access.*
  -> ProtectedAccessRuntime / Factory
  -> ProtectedExecutionBridge
  -> resolver / gateway / guard / verifier / context-local registry
  dec.core.starter.access.spi.*
  -> target-resolution / operation-execution ports

       ^ application/composition dependency
dec-demo / future P3-P7 execution modules
  -> obtain/use public ProtectedExecutionBridge
  -> call execute(ruleKey, operation, frame/owner/cursor...)
```

不新增 `dec-core-runtime`。禁止 context -> compiler/starter、compiler -> starter reverse dependency；starter 不为 P2 新增对 `dec-core-model` 的业务耦合。

## 3. Compile-time access rule invariant

```java
public enum AccessCompilationStatus { STATIC_ALLOW, RUNTIME_GUARD_REQUIRED }
public enum DynamicBindingClassification { STATIC_BOUND, RUNTIME_OBJECT_BOUND }
```

- `DIRECT_EXACT -> STATIC_BOUND -> STATIC_ALLOW`；无 RuntimeBindingPlan/RuntimeAccessRequirement。
- `EVERY_COLLECTION_ELEMENT -> RUNTIME_OBJECT_BOUND -> RUNTIME_GUARD_REQUIRED`；exact plan + EXACT_RUNTIME_BINDING requirement。
- unsupported dynamic selector compile ERROR。

## 4. 唯一 immutable ModelAccessPolicyIndex authority（R12 保持）

Context-owned API：

```java
public final class ModelAccessPolicyIndex {
    public static ModelAccessPolicyIndex empty();
    public static ModelAccessPolicyIndex of(
        Iterable<CompiledModelAccessRule> rules);
    public Optional<CompiledModelAccessRule> find(ModelAccessRuleKey key);
    public Set<ModelAccessRuleKey> keys();
}
```

`of(...)` 必须拒绝 duplicate/null/key mismatch/illegal STATIC-RUNTIME state/non-canonical runtime key，并返回 deterministic immutable snapshot。

唯一 publication chain：

```text
compiled access rules
 -> ModelAccessPolicyIndex.of(...)
 -> SemanticDigestInput(same immutable index)
 -> digest compute
 -> DigestBoundCompiledInput(same index + digest)
 -> CompiledModelSetBuilder.FrozenInput
 -> CompiledModelSet.published(...same index + digest...)
 -> EngineContext.modelAccessPolicyIndex()
 -> DefaultModelAccessGuard.find(exact key) once
```

禁止 Guard 扫 `definitions()`、从 typed registries 重建权限、starter 维护第二份 authorization Map、resolver/gateway/verifier/adapter 重查 policy。

## 5. CompiledModelSet publication + legacy compatibility（R12 保持）

现有八参数 constructor 保持 source compatibility：

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

其确定性语义：

```text
legacy 8-arg constructor
 -> existing facts behavior unchanged
 -> ModelAccessPolicyIndex.empty()
 -> never reconstruct policy from definitions()/typedRegistries()
 -> protected access exact miss => POLICY_NOT_FOUND
```

P2 production compiler 使用：

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

`EngineContext.modelAccessPolicyIndex()` 直接转交 `compiledModelSet().modelAccessPolicyIndex()`，不得复制或重建 authority。

## 6. ProtectedExecutionBridge public API — 直接参数式调用

R13 正式 production invocation contract：

```java
package dec.core.starter.access;

public final class ProtectedExecutionBridge {
    // created by ProtectedAccessRuntimeFactory; no public/protected constructor

    public ProtectedAccessResult execute(
        ModelAccessRuleKey requestedRuleKey,
        AccessOperation operation,
        RuntimeExecutionFrameId frameId,
        RuntimeResolutionOwnerId ownerResolutionId,
        Optional<RuntimeCollectionCursorId> collectionCursorId);
}
```

如 WRITE/EXECUTE 后续需要业务 payload/action 参数，由后续 P3～P7 executor 与 `ProtectedOperationExecutionPort` 定义；P2 当前不把业务 payload 语义塞进 access-control API。

一个 bridge 在 composition 时只固定：

```text
EngineContext / ProtectedAccessRuntime identity
AccessConsumerIrKey
ProtectedTargetResolutionPort
ProtectedOperationExecutionPort
```

每次调用显式提供：

```text
requestedRuleKey
operation
frameId
ownerResolutionId
optional collectionCursorId
```

本 Revision 明确允许 caller 提供这些事实；starter 负责 null/context/shape 校验并进入 Guard，不再通过 token/state-port 验证“这些事实是否由 framework 签发”。

## 7. 删除的 R12 token contracts

以下类型/行为不再属于 P2 Design：

```text
ProtectedExecutionToken
ProtectedExecutionStatePort
ProtectedExecutionBridgeReceiver
recognizes(token)
frameId(token)
ownerResolutionId(token)
collectionCursorId(token)
token claim / lease / replay / stale-token authority
PROTECTED_EXECUTION_TOKEN_UNTRUSTED
```

Test Design 也不得继续要求 fake-token、foreign-token、same-token concurrent claim 等 oracle。

## 8. Per-invocation execution flow

```text
future executor
 -> bridge.execute(ruleKey, operation, frameId, ownerId, optional cursorId)
 -> validate required call arguments
 -> internal issueInvocation(
      bridge-bound consumer,
      caller-supplied rule/op/frame/owner/cursor)
 -> ContextLocalProtectedAccessRegistry stores exact internal issued pair
 -> requireIssuedPair(pair)
 -> DefaultProtectedAccessResolver resolves actual target
 -> one-shot ResolvedProtectedAccess capability
 -> Gateway
 -> Guard exact ModelAccessPolicyIndex lookup once
 -> STATIC_ALLOW fast path OR runtime proof verification
 -> same capability-bound target operation
 -> terminal capability consume
```

参数 validation 失败时必须在 target resolution/capability/Guard/policy lookup/operation/effects 前 fail closed。

## 9. Internal issued pair

`ProtectedAccessResolutionContext` / `ProtectedOperationIntent` 可继续作为 starter 内部 read contracts；其 production issued implementations 与 `IssuedInvocationRecord` 仍可 package-private。

区别是：R13 中 internal pair 的 authoritative facts 来自**本次 public bridge 调用参数 + bridge-bound consumer/context/ports**，而不是 execution token/state port。

低层实现仍必须阻止 internal A-pair/B-pair object substitution，但不再把 caller-selected rule/op 本身视为伪造。

## 10. Guard / Gateway / target binding

`DefaultModelAccessGuard`：

```text
engineContext.modelAccessPolicyIndex().find(requestedRuleKey)
```

exact lookup = 1。

- STATIC_ALLOW：plan/requirement absent；verifier=0；evaluator=0。
- RUNTIME_GUARD_REQUIRED：selected exact rule/plan/requirement -> verifier once。
- resolver/gateway/verifier/ports policy lookup=0。

`DefaultProtectedAccessGateway` 只能执行 resolver/capability 已绑定的 actual target + operation execution port。即使 caller 提交同一组参数两次，也形成两个独立 invocation；但单个 capability 内禁止 target A -> B substitution。

## 11. Concurrency semantics（R13）

### 11.1 支持的并发

`ProtectedExecutionBridge` 是 immutable/stateless facade，可被多个线程并发调用：

```text
Thread A -> execute(ruleA, READ, frameA, ownerA, cursorA)
Thread B -> execute(ruleB, WRITE, frameB, ownerB, cursorB)
```

每次调用创建独立 internal issued pair/capability。

### 11.2 相同参数并发

R13 **不定义** frame/rule/op 参数组合为 one-shot execution occurrence：

```text
Thread A -> execute(ruleX, READ, frameF, ownerO, cursorC)
Thread B -> execute(ruleX, READ, frameF, ownerO, cursorC)
```

两次都是独立 invocation；如果两次均满足 Guard/target/runtime proof，可以各自产生 operation。当前阶段不以 replay/duplicate execution 为 access-control DENY 条件。

### 11.3 仍必须原子的一层

单个 `ResolvedProtectedAccess` capability 仍必须 reserve/consume atomic：

```text
same capability + concurrent gateway execute
 -> terminal success <= 1
```

runtime-required operation 前仍 revalidate current Context/frame/cursor/rule/plan/membership；如果 stale 则 DENY，不自动换成别的 target/frame。

## 12. Stable runtime reasons

至少保留：

- `PROTECTED_ACCESS_ARGUMENT_INVALID`
- `PROTECTED_ACCESS_INPUT_UNTRUSTED` / `PROTECTED_ACCESS_INPUT_PAIR_MISMATCH`（仅 starter internal invariant）
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

`PROTECTED_EXECUTION_TOKEN_UNTRUSTED` 从 R13 移除。

## 13. Finding interpretation

- FND-004：direct public bridge 使 `dec-demo` / future executor production-reachable；仍需 exact specialist Review，formal OPEN。
- FND-015：validated PolicyIndex construction + policy-aware publication/legacy compatibility 保持 candidate-fixed；formal OPEN。
- FND-016：source -> runtime reachability 改为 direct bridge 参数调用；formal OPEN。
- FND-019：不再包含 token claim/recognizes TOCTOU 问题；只保留 capability actual-target/operation atomic binding，candidate 为 `FIX_PROPOSED / OPEN`。
- FND-007：不再要求 token replay matrix；现有 fail-closed matrix按 direct-argument API 重新定义，candidate 为 `FIX_PROPOSED / OPEN`。
- 不创建 FND-020。

## 14. Review / lifecycle gate

本 Design 是新的 candidate，不是 PASSED Evidence。下一步需要 exact `DESIGN-P2-R13` 的 Architecture / ApiContract / Develop / Impact / CrossModule / Concurrency Review。`risk_detection.json` 与 `task_state.md` 未经正式 lifecycle 不得手工推进。Implementation Plan/TDD/Development remain BLOCKED。
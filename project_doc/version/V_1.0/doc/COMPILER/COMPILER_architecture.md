# COMPILER P2 架构增量

> Revision：`DESIGN-P2-R13`。Base：`DESIGN-P2-R12`。状态：`NEEDS_REVIEW / MACHINE_BLOCKED`。
> 本 Revision 保持真实 Maven ownership、PolicyIndex publication 与 Guard/Gateway 主架构；按用户明确决策撤销 execution-token trust layer，改成 direct-argument bridge invocation。不新增 FND-020。

## 1. Dependency direction

```text
dec-core-context
  <- neutral access rules / plan / policy index
  <- CompiledModelSet / EngineContext policy read API
       ^
       | existing compiler dependency
dec-core-compiler
  <- access compilation / classifier
  <- ModelAccessPolicyIndex construction
  <- semantic digest + DigestBoundCompiledInput publication
       ^
       | existing starter composition dependency
dec-core-starter
  <- ProtectedAccessRuntime / Factory
  <- ProtectedExecutionBridge
  <- resolver / gateway / guard / verifier / context-local registry
  <- target-resolution / operation-execution SPI
       ^
       | application/composition dependency
dec-demo / future execution modules
  <- call public bridge.execute(ruleKey, operation, frame/owner/cursor...)
```

Root reactor unchanged；no `dec-core-runtime`；no context/compiler/starter reverse dependency inversion。

## 2. Single policy authority

```text
compiler model-access compilation
 -> exact CompiledModelAccessRule iterable
 -> ModelAccessPolicyIndex.of(...)
 -> immutable index
 -> SemanticDigestInput
 -> DigestBoundCompiledInput(index + digest)
 -> CompiledModelSet.published(...same index...)
 -> EngineContext.modelAccessPolicyIndex()
 -> DefaultModelAccessGuard.find(exact key) once
```

禁止第二权限 Map、definitions scan、typed-registry policy rebuild、resolver/gateway/verifier/adapter policy re-selection。

## 3. Policy construction/publication boundary

Legacy compatibility：

```text
existing CompiledModelSet 8-arg constructor
 -> existing model facts
 -> ModelAccessPolicyIndex.empty()
 -> no policy reconstruction
 -> protected access exact miss fail closed
```

P2 production：

```text
compiled rules
 -> validated index
 -> digest-bound closure
 -> CompiledModelSet.published(...index...)
 -> EngineContext
```

当前真实 `DigestBoundCompiledInput -> CompiledModelSetBuilder.FrozenInput` seam 继续作为 P2 改造入口。

## 4. R13 direct bridge architecture

R12 的 token/receiver/state-port 架构全部移除。R13 external production path：

```text
future executor
 -> public ProtectedExecutionBridge
 -> execute(ruleKey, operation, frameId, ownerId, optional cursorId)
 -> starter validates call arguments
 -> internal issuance
 -> internal issued pair
 -> resolver
 -> one-shot capability
 -> Gateway
 -> Guard
 -> operation
```

Bridge composition 时固定：

```text
EngineContext/runtime
AccessConsumerIrKey
ProtectedTargetResolutionPort
ProtectedOperationExecutionPort
```

调用时显式传：

```text
ModelAccessRuleKey
AccessOperation
RuntimeExecutionFrameId
RuntimeResolutionOwnerId
Optional<RuntimeCollectionCursorId>
```

当前 Design 明确接受 caller 对这些调用事实的选择；不通过 token/recognized execution occurrence 限制它们。

## 5. Removed architecture elements

以下不再存在：

```text
ProtectedExecutionToken
ProtectedExecutionStatePort
ProtectedExecutionBridgeReceiver
composition-issued per-rule bridge capability requirement
recognizes(token)
frameId(token)/owner(token)/cursor(token)
token replay/claim/lease state
```

因此也不存在 `recognizes -> multiple live getters` 的 TOCTOU 架构问题；它通过删除整层 contract 消失，而不是通过加锁/claim 修复。

## 6. Internal issuance architecture

Public bridge 参数与 bridge-bound facts 合成 internal invocation：

```text
bridge-bound:
  EngineContext/runtime
  AccessConsumerIrKey
  target port
  operation port

caller-supplied:
  requestedRuleKey
  operation
  frameId
  ownerResolutionId
  optional cursor

         ↓
internal issueInvocation(...)
         ↓
IssuedInvocationRecord + internal read pair
```

Internal pair/record 仍只为 starter 实现隔离与 capability binding 服务，不再承担外部 caller authenticity 证明。

## 7. Resolver / capability / Gateway

`DefaultProtectedAccessResolver` 使用 internal invocation facts 解析 actual target，并把：

```text
EngineContext
consumer
requested rule
operation
frame/owner/cursor
actual target
operation execution port
runtime provenance
```

绑定进 one-shot `ResolvedProtectedAccess` registry state。

Gateway 只能执行该 capability 已绑定的 same target + operation port，禁止 Guard 后重新选择 target。

## 8. Guard architecture

```text
Gateway
 -> DefaultModelAccessGuard
 -> current EngineContext.modelAccessPolicyIndex().find(requestedRuleKey) exactly once
```

STATIC_ALLOW：verifier=0/evaluator=0。

RUNTIME_GUARD_REQUIRED：exact selected rule/plan/requirement -> verifier，且 actual member/frame/cursor/provenance 在 operation 前 revalidate。

## 9. Concurrency model

Bridge 为 immutable/stateless facade，允许多线程共享。

```text
Thread A -> execute(argsA) -> invocation A -> capability A
Thread B -> execute(argsB) -> invocation B -> capability B
```

如果 argsA == argsB，仍是两个独立 invocation；**R13 不把相同 frame/rule/op 解释为同一个 one-shot execution occurrence**。

因此：

- 不存在 token claim success <= 1 Gate；
- 不存在 same-token replay DENY；
- 不存在 token state snapshot tearing；
- duplicate business invocation 是否应抑制，留给 future executor/business idempotency，不属于当前 P2 access-control contract。

仍必须保证：

```text
same capability concurrent Gateway execution
 -> terminal success <= 1
```

以及 capability A 永远不能替换成 target B。

## 10. Real source reachability

`dec-demo` 真实 E2E：

```text
systems.xml
 -> production compiler
 -> ModelAccessPolicyIndex
 -> digest-bound CompiledModelSet
 -> EngineContext
 -> starter runtime/factory
 -> obtain public ProtectedExecutionBridge
 -> bridge.execute(exactRuleKey, READ, frame, owner, cursor)
 -> resolver/Gateway/Guard
 -> static or runtime proof branch
 -> operation
```

不需要 token、receiver、state port、reflection、package-private starter access 或 test-only mint helper。

## 11. P2 / later phase boundary

P2 负责 access-control plumbing、policy publication、Guard/Gateway、runtime proof 与 protected target binding。

P3～P7 负责具体 Rule/change/action/query business execution 和是否需要额外 duplicate/idempotency/execution-occurrence identity 语义。

当前 P2 不实现 token/idempotency framework。

## 12. Finding interpretation

- FND-004：direct public bridge 解决 production reachability，formal OPEN pending exact Review。
- FND-015：PolicyIndex construction/publication/legacy compatibility 维持 candidate-fixed，formal OPEN。
- FND-016：source->runtime E2E 改为 direct bridge，formal OPEN。
- FND-019：R12 token atomicity concern 不再适用；只审 capability target/operation atomic binding，candidate `FIX_PROPOSED / OPEN`。
- FND-007：token concurrent replay matrix 不再适用；direct-argument fail-closed matrix 继续 candidate `FIX_PROPOSED / OPEN`。
- no FND-020。

## 13. Review gate

下一轮 exact `DESIGN-P2-R13` 重点 Review：direct bridge API 可实现性、parameter validation、Maven/module boundary、PolicyIndex publication、capability one-shot 与 A/B target binding。Implementation Plan/TDD/Development remain BLOCKED until exact specialist Review + machine lifecycle closure。
# COMPILER P2 设计测试接缝

> Revision：`DESIGN-P2-R13`。正式 Test Design candidate：`TESTDESIGN-P2-R14`。
> 本 Revision 删除 token/state-port/receiver seams，改为 direct-argument bridge reachability；PolicyIndex construction/publication seams 保持。

## 1. Module seams

| Seam | Exact module | Production owner |
|---|---|---|
| Policy-index factory / CompiledModelSet compatibility | `dec-core-context` | access contracts + model classes |
| Classifier/rule/plan/index publication/digest | `dec-core-compiler` | modelaccess/compiled/pass |
| Direct bridge/runtime/Guard/Gateway/registry | `dec-core-starter` | `dec.core.starter.access.*` |
| Target/operation ports | `dec-core-starter` | `dec.core.starter.access.spi.*` |
| Real direct-bridge reachability | `dec-demo` | existing `systems.xml` + starter dependency |

## 2. Policy-index construction seam — FND-015

必须覆盖：

```text
ModelAccessPolicyIndex.empty()
ModelAccessPolicyIndex.of(Iterable<CompiledModelAccessRule>)
find(exact key)
keys()
```

Negative：duplicate/null/key mismatch/illegal STATIC-RUNTIME state/wildcard runtime key/mutability 均拒绝。

## 3. CompiledModelSet publication compatibility seam — FND-015

冻结：

```text
existing 8-arg constructor -> empty policy index
CompiledModelSet.published(...policyIndex...)
CompiledModelSet.modelAccessPolicyIndex()
EngineContext.modelAccessPolicyIndex()
```

Legacy 不重建 policy；P2 production path 必须走 policy-aware factory；equals/hashCode 区分 policy index。

## 4. Compiler digest/publication seam — FND-015

```text
compiled access rules
 -> ModelAccessPolicyIndex.of
 -> SemanticDigestInput(same index)
 -> DigestBoundCompiledInput(same index + digest)
 -> FrozenInput
 -> CompiledModelSet.published(same index + digest)
```

必须证明 index 在 digest 前建立、same snapshot 被 digest 与 publication 共同使用、policy semantic change 改变 semantic digest。

## 5. Direct execution bridge API seam — FND-004/FND-016

Starter API inspection 必须存在：

```java
ProtectedAccessResult execute(
    ModelAccessRuleKey requestedRuleKey,
    AccessOperation operation,
    RuntimeExecutionFrameId frameId,
    RuntimeResolutionOwnerId ownerResolutionId,
    Optional<RuntimeCollectionCursorId> collectionCursorId);
```

必须不存在：

```text
ProtectedExecutionToken
ProtectedExecutionStatePort
ProtectedExecutionBridgeReceiver
bridge.execute(token)
recognizes(token)
claim(token)
```

Bridge 可由 runtime factory 创建并供 external module 直接调用；不要求 external caller 先取得 issued pair。

## 6. Direct argument validation seam

Negative：

- null ruleKey；
- null operation；
- null frameId；
- null ownerId；
- null Optional wrapper；
- closed runtime/context；
- runtime-required path 上不满足 exact plan/cursor/membership 的事实。

API shape/argument failure 必须在 resolver/capability/Guard/policy lookup/operation/effects 前失败；候选 reason：`PROTECTED_ACCESS_ARGUMENT_INVALID`。

当前 Revision **不把 caller 选择另一个 valid ruleKey/operation 当成 forged-input test**。

## 7. Direct bridge reachability seam — FND-016

`dec-demo` 必须只使用 public production API：

```text
EngineContext
 -> ProtectedAccessRuntimeFactory
 -> public ProtectedExecutionBridge
 -> bridge.execute(ruleKey, READ, frame, owner, cursor)
 -> internal issuance
 -> resolver
 -> Gateway
 -> Guard
 -> operation
```

禁止 reflection、package-private access、test-only mint helper、manual issued context/intent、direct Guard/Gateway shortcut。

## 8. Internal issued-pair seam

Starter same-package tests只验证 internal object/pair consistency：

- unknown internal implementation rejected；
- A-context+B-intent rejected；
- issued record/capability target binding 不可替换；
- failure before resolver/policy when internal invariant is broken。

这些不是 external caller authority tests。

## 9. STATIC_ALLOW Guard path — FND-001

```text
bridge.execute(staticRuleKey, READ, frame, owner, cursor)
 -> internal issuance
 -> resolver target A
 -> Gateway=1
 -> Guard=1
 -> PolicyIndex exact lookup=1
 -> STATIC_ALLOW
 -> RuntimeBindingVerifier=0
 -> operation A=1
```

Direct operation outside Guard/runtime 仍必须被 bypass gate 阻止。

## 10. Runtime binding / target substitution — FND-017/FND-019

- valid element A -> verifier match -> A operation=1；
- foreign B -> DENY；
- capability A + forced target B -> `RUNTIME_BINDING_OPERATION_TARGET_MISMATCH`；
- stale Context/frame/cursor/rule/plan/membership -> DENY；
- same capability replay/concurrent execute -> at most one terminal success。

**不测试 same bridge arguments / same frame 的 replay suppression**；R13 将其定义为独立 invocation。

## 11. Bridge concurrency seam

必须证明一个 immutable bridge 可被并发共享：

```text
Thread A -> execute(argsA)
Thread B -> execute(argsB)
```

两次都建立独立 invocation/capability，状态不得串线。

如果 `argsA == argsB`，测试不得要求 successes <= 1；允许两个独立 invocation 分别完成。P2 只要求每个 invocation 的 capability one-shot。

不得再存在 token barrier/latch/claim replay Case。

## 12. Single policy authority seam

Spy counts：

```text
Guard current EngineContext policy lookup = 1
Resolver = 0
Gateway = 0
RuntimeBindingVerifier = 0
TargetResolutionPort = 0
OperationExecutionPort = 0
```

Repository inspection 禁止 starter secondary policy Map 或 definitions/typed-registry reconstruction。

## 13. Real source integration

```text
systems.xml
 -> production compiler/classifier
 -> policy index + semantic digest
 -> policy-aware CompiledModelSet
 -> EngineContext
 -> starter runtime
 -> public bridge.execute(...)
 -> static status branch: Guard=1/verifier=0/op=1
 -> every(orderDetailList,status=1) runtime branch: Guard=1/verifier=1/op=1 for valid member
 -> invalid runtime proof/target substitution: op=0/effects=0
```

No manual compiled rule, no token, no direct Gateway shortcut。

## 14. Planned test ownership

至少：

- `dec-core-context`: `ModelAccessPolicyIndexContractTest`
- `dec-core-context`: `ModelAccessPolicyPublicationCompatibilityTest`
- `dec-core-compiler`: `ModelAccessPolicyIndexPublicationTest`
- `dec-core-starter`: `ProtectedExecutionBridgeContractTest`
- `dec-core-starter`: `ProtectedExecutionBridgeConcurrencyTest`
- `dec-core-starter`: `ProtectedAccessInputAuthorityTest`
- `dec-core-starter`: `ModelAccessPolicyAuthorityIntegrationTest`
- `dec-core-starter`: static/runtime/proof/substitution tests
- `dec-demo`: `P2DirectBridgeReachabilityTest`
- `dec-demo`: `P2DynamicSourceToOperationTest`

## 15. RED validity

```bash
./mvnw -pl <EXACT-MODULE> -am -Dmaven.test.skip=true install
./mvnw -pl <EXACT-MODULE> -Dtest=<EXACT-TESTCLASS> -Dsurefire.failIfNoSpecifiedTests=true test
```

第二步禁止 `-am`。Missing module/test/symbol/setup/compile failure = INVALID_RED。

## 16. Review gate

这些 seams 只是 `DESIGN-P2-R13 / TESTDESIGN-P2-R14` candidate。FND-004/FND-015/FND-016 formal OPEN；FND-007/FND-019 不再包含 execution-token concurrency/replay gate。TDD 仍 BLOCKED。
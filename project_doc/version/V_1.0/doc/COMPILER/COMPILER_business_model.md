# COMPILER 业务模型

> Revision：`BM-R19`。Base：`BM-R18`。
> Authoritative Inputs：`REQAN-P2-R01@d08612768131` + Overlay R04 + ACTIVE Direct Bridge + ACTIVE AC-007 Option B + ACTIVE READ/WRITE-only。
> Status：`NEEDS_EXACT_REVIEW / MACHINE_BLOCKED`。

R19 只修复本轮独立 Review 剩余的 runtime authority / atomicity / concurrency 闭环；R18 已确认正确的 P1-compatible TargetKey、structured schema、READ/WRITE-only、PolicyIndex authority、Option B 均保持不变。历史 PASSED lifecycle 不被覆盖。

## 1. Candidate revision direction

```text
REQAN-P2-R01 + Overlay R04
 -> BM-R19
 -> FLOW-R09
 -> DESIGN-P2-R21
 -> TESTDESIGN-P2-R22
```

## 2. Source identity remains frozen

```text
authorization owner System
+ TargetKey(shared ViewKey)
+ exact ModelPath
+ READ/WRITE
= ModelAccessRuleKey
```

TargetKey 不包含 authorization owner System；local targetView/selector 仍是独立 binding fact。

## 3. WRITE authority is ModelAccessRuleKey

Direct Bridge 与 WRITE intent 统一使用 `ModelAccessRuleKey`。`RuleKey` 仅允许作为 `Optional<RuleKey>` provenance；Rule / Change / CustomAction 都不依赖 RuleKey 才能成立。

WRITE resolution input：

```text
ModelAccessRuleKey
+ RuntimeExecutionFrameId
+ RuntimeResolutionOwnerId
+ Optional<RuntimeCollectionCursorId>
```

0 candidate=`WRITE_INTENT_NOT_FOUND`；1 candidate=Guard 前 immutable freeze；N>1=`WRITE_INTENT_AMBIGUOUS`。

## 4. One WRITE has one ModelPath authority

`ResolvedWriteIntent.modelAccessRuleKey().modelPath()` 是唯一 WRITE path。`ResolvedProtectedWriteAccess` 只携带 invocationId、runtimeObjectId 和 writeIntent；production operation port 不再接受第二个 ModelPath 参数。

## 5. Scoped runtime object location

`RuntimeObjectId` 只在当前 `ProtectedAccessComposition` 所绑定的 sealed `RuntimeModelSession` 中解析。Session 在 composition/frame 建立时注册现有 ModelData/runtime handle，开始 protected execution 前 seal；seal 后 locator table 不再新增/替换映射。

- missing：`RUNTIME_OBJECT_NOT_FOUND`；
- closed/cross-session/stale：`RUNTIME_OBJECT_STALE`；
- 不允许 static/global mutable registry；
- 不允许从 RuntimeObjectId 推断权限；
- composition/context/session 之间不得跨域解析。

## 6. Typed execution identity

Invocation → resolved access → write intent 全链保持：

```text
RuntimeExecutionFrameId
RuntimeResolutionOwnerId
Optional<RuntimeCollectionCursorId>
```

禁止 raw String、null、空串或 `N/A` sentinel 表示 optional cursor。

## 7. Transactional WRITE failure

Guard ALLOW 后 WRITE 进入 dec-core-model 的一个 transaction boundary：

```text
SUCCESS
 -> exactly one committed mutation
 -> increment RuntimeMutationVersion
 -> receipt

FAILURE
 -> rollback / isolated-working-copy restore
 -> externally observable ModelData/origin state unchanged
 -> receipt absent
 -> capability remains CONSUMED
 -> RUNTIME_WRITE_FAILED
```

现有 `ModelContainer` 的 commit/rollback/close 能力可作为实现基础，但实现不得在 commit 成功前把 working mutation 不可逆地暴露到 origin object；若复用现有 pre-commit copy 行为，必须通过 working-copy/deferred-publication 或显式 restore 满足本 invariant。

## 8. Different-capability same-path concurrency

每个 RuntimeModelSession 对 `(RuntimeObjectId, ModelPath)` 维护 monotonic `RuntimeMutationVersion` 和 serialization boundary。WRITE intent freeze 当前 expected version。

两个 capability 在同一 version 下并发写同一 object/path：至多一个 commit；winner 将 version +1；loser 在锁内发现 version mismatch，返回 `WRITE_INTENT_STALE`，mutation=0、receipt absent。Capability 已消费，不自动 retry。之后重新 resolve 的新 invocation 可基于新 version 再执行。

## 9. Gate

`FND-P2-REV-020` 的 P1 source identity semantic fix 已被本轮独立 Review 确认正确，但 formal closure 仍等待 lifecycle/risk Evidence。其余相关 finding 继续 OPEN，等待 same-revision independent Review/Evidence。Implementation Plan/TDD/Development 仍 BLOCKED。

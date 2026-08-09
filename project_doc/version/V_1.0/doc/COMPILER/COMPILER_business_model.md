# COMPILER 业务模型

> Revision：`BM-R18`。Base：`BM-R17`。
> Authoritative Inputs：`REQAN-P2-R01@d08612768131` + Overlay `R04` + ACTIVE Direct Bridge + ACTIVE AC-007 Option B + ACTIVE READ/WRITE-only。
> Status：`NEEDS_EXACT_REVIEW / MACHINE_BLOCKED`。

R18 是针对 PR36 独立 Review 的 remediation revision。它保留 R17 已确认的 READ/WRITE-only、PolicyIndex 单一权限 authority、Option B、one-shot capability 和 neutral `ProtectedAccessPort`，只收敛 structured schema、P1 source-model identity、WRITE intent 唯一性、production runtime operation 与 API value contract。历史 `BM-R07 PASSED` 不被覆盖。

## 1. Authoritative revision direction

```text
REQAN-P2-R01 + Overlay R04 + active Decisions
 -> BM-R18
 -> FLOW-R08
 -> DESIGN-P2-R20
 -> TESTDESIGN-P2-R21
```

BM authoritative inputs 不包含 Flow/Design/TestDesign。下游只能 trace 回 BM，不得反向成为输入。

## 2. P1-compatible source-model identity

P1 model-access 必须继续区分四个轴：

```text
authorization owner System
+ shared source View identity
+ source ModelPath
+ local targetView / selector mapping
```

R18 因此冻结：

```text
sourceModel -> existing shared ViewKey -> TargetKey(ViewKey)
sourcePath  -> shared ModelPathCompiler -> exact ModelPath
```

`TargetKey` **不包含 authorization owner System**。同一个共享 `ViewKey("OrderInfo")` 被不同 System 授权时得到 value-equal TargetKey；不同授权归属通过 `ModelAccessRuleKey.authorizationOwnerSystemKey` 区分。

```text
ModelAccessRuleKey =
  authorizationOwnerSystemKey
  + TargetKey(sharedSourceViewKey)
  + ModelPath
  + AccessOperation(READ|WRITE)
```

`targetView/selector/resolvedTarget` 继续是 owner-System 内部绑定事实，不与 sourceModel identity 混为一体。这样兼容 P1 的 `ownerSystem/sourceModel/sourcePath/targetView/selector/resolvedTarget` 分离模型，不自行引入 System-qualified source namespace。

## 3. ModelPath / READ-WRITE / policy classification

- `sourcePath` 与 TargetKey 正交；合法 wildcard 只在 compile-time finite expansion 后生成 exact ModelPath。
- `AccessOperation` 只有 READ / WRITE；没有 EXECUTE。
- PolicyIndex 仅接受两种 representation：

```text
STATIC_ALLOW           + NONE                  + no RuntimeBindingPlan
RUNTIME_GUARD_REQUIRED + EXACT_RUNTIME_BINDING + RuntimeBindingPlan
```

其他组合必须在 construction/publication 失败，runtime 不得 repair/reclassify。

## 4. WRITE intent implementation uniqueness

WRITE 在 Guard 前执行唯一选择：

```text
(ruleKey + TargetKey + ModelPath + frameId + ownerResolutionId + cursorId)
 -> candidate write intents
```

- 0 个：`WRITE_INTENT_NOT_FOUND`，deterministic DENY；
- 1 个：冻结 immutable `ResolvedWriteIntent` 到 `ResolvedProtectedAccess`；
- >1 个：`WRITE_INTENT_AMBIGUOUS`，deterministic DENY。

Guard 后禁止重新读取 frame/cursor 后选择另一个 intent。若 proof/staleness 检查发现绑定已失效，只能 DENY，不能 reselect。

## 5. Real production READ / WRITE

Production operation 不能只是测试 callback：

```text
dec-core-context
  ProtectedAccessPort + RuntimeModelOperationPort contract + immutable values/IDs
        ^
        |
dec-core-model
  DefaultRuntimeModelOperationAdapter
  RuntimeObjectId -> actual runtime object
  ModelPath       -> actual member/value
  frozen intent   -> actual mutation
        ^
        |
dec-core-starter
  ProtectedExecutionBridge / Gateway / Guard
  ProtectedOperationExecutionAdapter
  production composition wiring
```

`dec-core-starter` 的 production assembly 必须使用 `dec-core-model` 的真实 runtime adapter；测试 fake 只能证明 unit seam，不能证明 production reachability。未来 P3/P4/P6 core 仍只依赖 neutral `dec-core-context ProtectedAccessPort`，不依赖 starter。

READ：Guard ALLOW 后读取 capability-bound runtime object/path，返回 immutable value snapshot，不产生 mutation。
WRITE：Guard ALLOW 后只消费 Guard 前冻结的 intent，执行一次实际 mutation，返回 receipt。

## 6. RuntimeFactValue / opaque IDs

`RuntimeFactValue` 是 closed domain：`NULL / BOOLEAN / INTEGER / DECIMAL / STRING / LIST / OBJECT`。

- 输入递归 snapshot；不得泄露 live mutable object；
- INTEGER 使用 canonical `BigInteger`；DECIMAL 使用 canonical normalized `BigDecimal`；
- LIST 保序 immutable；OBJECT key 唯一并按确定顺序序列化；
- equality/hash 基于 canonical structural value；
- deterministic JSON serialization；不允许 arbitrary Java object。

`RuntimeObjectId`、`ProtectedInvocationId`、`RuntimeWriteIntentId` 是 immutable nonblank opaque String value wrappers：exact/case-sensitive equality，不 parse、不 case-fold、不从 ID 推断权限。

## 7. AC-007 / concurrency / denial

Option B 保持 ACTIVE：Rule / Change / CustomAction representative entries 必须从同一 production composition 获得相同 Bridge 与 EngineContext authority snapshot。

One-shot capability：`ISSUED -> CONSUMED` atomic transition；相同 capability 最多一个 operation/mutation 成功。任何 DENY 都发生在 actual operation 前，且不得暴露 READ value 或 WRITE receipt。

## 8. Structured business model authority

`COMPILER_business_model.yaml` 为 BM-R18 machine authority；本文件是同 revision normative mirror。YAML 已恢复 common-develop 2.44 `business_model` schema 的 scenarios/entities/valueObjects/aggregates/invariants/stateMachines/businessErrors/traceability 结构。

## 9. Gate

- 新 `FND-P2-REV-020`：TargetKey source-model identity 与 frozen P1 semantics 冲突；本 revision 给出兼容修复，但 finding 仍 OPEN，等待 same-revision independent Review/Evidence。
- FND-004/011/013/014/015/017/019 等只标记 FIX/PARTIAL_FIX_PROPOSED，不关闭。
- `risk_detection.json` 继续 `NOT_SCANNED`。
- Implementation Plan / TDD / Development 继续 BLOCKED。

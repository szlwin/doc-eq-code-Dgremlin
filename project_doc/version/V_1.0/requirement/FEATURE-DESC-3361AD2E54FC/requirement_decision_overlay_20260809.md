# P2 Requirement Decision Overlay

> Overlay ID：`REQAN-P2-R01+DEC-OVERLAY-20260809`  
> Base Requirement：`REQAN-P2-R01@d08612768131`（历史内容保持不改写）  
> Effective decisions：`DEC-P2-DIRECT-BRIDGE-AUTHORITY-001`、`DEC-P2-AC007-STAGE-BOUNDARY-001`  
> Status：`CANDIDATE / NEEDS_REQUIREMENT_REVIEW / MACHINE_BLOCKED`

本文件不声称 `REQAN-P2-R01` 原本就包含以下语义，而是把用户确认/阶段边界冲突消解后对当前 P2 candidate 生效的 Requirement delta 显式化，供 BM/Design/TestDesign 使用。

## 1. Direct bridge authority delta

当前 P2 protected-access runtime 使用：

```java
bridge.execute(
    requestedRuleKey,
    operation,
    frameId,
    ownerResolutionId,
    optionalCursorId);
```

有效 Requirement delta：

1. caller 可以逐次选择 exact `ModelAccessRuleKey` 与 `AccessOperation`；
2. `AccessConsumerIrKey` 当前只用于 provenance/diagnostic，不作为 authorization-key 维度；
3. 该选择不能扩大 compiler-published authority：请求 rule/op 必须 exact 命中当前 immutable `ModelAccessPolicyIndex`，状态/plan/proof/target binding 任一无法证明即 DENY；
4. 不允许默认 allow、fuzzy key、operation fallback、另建 secondary permission map；
5. 后续如要引入 per-consumer rule/op binding，必须创建新的 Requirement/Decision revision，不能由 Development 私自收紧/放宽。

## 2. AC-007 stage-boundary delta

`REQAN-P2-R01` 原 AC-007 literal 文本要求 Rule/change/custom-action concrete 入口都实际执行并证明不可旁路；与此同时同一 Requirement 又明确 P3/P4/P6/P7 concrete execution semantics 属于后续阶段。当前 P2 candidate 采用 `DEC-P2-AC007-STAGE-BOUNDARY-001` 解决这一冲突。

### AC-P2-SYSTEM-RULEVIEW-007（current candidate interpretation）

**所有 P2 protected-access production 入口无合法旁路**

Given：
- 当前 P2 发布的 immutable EngineContext / ModelAccessPolicyIndex；
- starter 提供的 production `ProtectedExecutionBridge`；
- 一个允许访问和一个未授权/无法证明的访问请求。

When：
- caller 通过所有公开、受支持的 production P2 API 尝试 protected READ/WRITE/EXECUTE；
- 同时检查 public/protected visibility、依赖方向、compatibility adapter 和 starter composition 暴露面。

Then：
1. 唯一受支持的 protected-operation 路径必须是 `ProtectedExecutionBridge -> internal issuance -> target resolution -> one-shot capability -> Gateway -> Guard -> operation`；
2. 任何合法 production caller 都不能直接调用 Guard 后置 operation、直接 mint capability、直接调用 package-private issuance、通过 compatibility adapter 写入新 registry 或获得第二套权限 authority；
3. policy missing/invalid、Guard unavailable、runtime proof invalid/stale、target/operation substitution 均在副作用前稳定 DENY；
4. P2 不要求提前实现 P3 Rule evaluator、P4 Action/Produce executor 或 P6 QueryPlan executor 的完整业务状态机。

### Downstream obligations

- P3：真实 Rule/Information 读取 consumer 接入 P2 seam 的 integration acceptance；
- P4：真实 change/custom action/produce mutation consumer 接入同一 seam 的 integration acceptance；
- P6：真实 QueryPlan/model read consumer 接入同一 ModelPath/permission seam 的 integration acceptance。

这些 downstream acceptance 不能反向改变 P2 已冻结的 System/RuleView/ModelPath/PolicyIndex/Guard contract。

## 3. 仍保持不变的 R01 语义

以下 R01 语义不被 overlay 改写：

- System 一等身份与 ownership boundary；
- RuleView `(system,name)` 唯一身份；
- READ/WRITE/EXECUTE 相互独立且未声明默认拒绝；
- rule/change/query 等消费者共享同一 canonical ModelPath contract；
- static illegal access compile-time fail；dynamic legal access runtime fail-closed；
- atomic publication、old Context preservation、Context isolation；
- source-aware deterministic Diagnostic/denial；
- declaration compatibility 只保留到 P7 的迁移边界。

## 4. Gate

本 overlay 是当前 BM-R13 / DESIGN-P2-R15 / TESTDESIGN-P2-R16 的显式输入，但在 Requirement exact Review 与 machine lifecycle 完成前不得把 Requirement delta 描述为 PASSED。

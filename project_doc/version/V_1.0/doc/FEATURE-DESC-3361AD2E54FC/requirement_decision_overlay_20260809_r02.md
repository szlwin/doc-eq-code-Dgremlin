# P2 Requirement Decision Overlay R02

> Overlay ID：`REQAN-P2-R01+DEC-OVERLAY-20260809-R02`  
> Base Requirement：`REQAN-P2-R01@d08612768131`（历史内容保持不改写）  
> Effective decision：`DEC-P2-DIRECT-BRIDGE-AUTHORITY-001`  
> Pending decision：`DEC-P2-AC007-STAGE-BOUNDARY-001`  
> Status：`NEEDS_USER_DECISION / NEEDS_REQUIREMENT_REVIEW / MACHINE_BLOCKED`

本 overlay 只把当前真实决策状态显式化，不替用户改变原 Requirement 的 scope 或 acceptance semantics。

## 1. 已获用户授权：Direct bridge authority

`DEC-P2-DIRECT-BRIDGE-AUTHORITY-001` 继续 ACTIVE：

```java
bridge.execute(
    requestedRuleKey,
    operation,
    frameId,
    ownerResolutionId,
    optionalCursorId);
```

当前 P2 caller 可以逐次选择 exact compiler-published `ModelAccessRuleKey` 与 `AccessOperation`；`AccessConsumerIrKey` 只用于 provenance/diagnostic，不是 authorization-key 维度。请求必须 exact 命中当前 immutable `ModelAccessPolicyIndex` 且通过状态/plan/proof/target binding；无法证明即 DENY。不得默认 allow、fuzzy key、operation fallback 或建立 secondary permission authority。

## 2. AC-007 当前未决，不得由 Agent 代替用户选择

原 `REQAN-P2-R01` AC-007 要求 Rule、change、custom action 三类真实入口分别执行允许/未授权场景并证明没有 Guard bypass；同时 R01 又把 P3/P4/P6 concrete execution semantics 放在后续阶段。两者存在阶段边界冲突。

`DEC-P2-AC007-STAGE-BOUNDARY-001` 当前仅为 `PROPOSED / PENDING_USER_DECISION`。正式可选方案为：

### Option A — P2 seam acceptance

P2 验收唯一 production protected-access seam、API visibility/dependency structure 无合法旁路，以及 P2 当前可执行 protected access 全部经 `Bridge -> Gateway -> Guard`。真实 Rule/change/custom-action/query consumer integration 明确下沉 P3/P4/P6。

### Option B — P2 concrete consumer acceptance

P2 必须提供足以执行原 AC-007 的代表性 production Rule/change/custom-action consumers，并在 P2 本阶段真实执行 allow/deny/no-bypass integration acceptance；不得仅以 seam contract 代替。

### 当前 Gate

在用户明确选择 A 或 B 之前：

- 原 R01 AC-007 不被 supersede；
- Option A/B 均不得描述为 ACTIVE acceptance；
- `TR-P2-SYSTEM-RULEVIEW-007` 必须为 `PENDING_USER_DECISION`；
- BM/Design/TestDesign 可准备两种方案共用的 Guard/no-bypass 基础契约，但不得把任一方案当作 closure Evidence；
- Implementation Plan/TDD/Development 继续 BLOCKED。

## 3. 其他 R01 语义保持不变

- System 是 first-class identity/ownership boundary；
- RuleView 唯一身份为 `(system,name)`；
- READ/WRITE/EXECUTE 相互独立且未声明默认拒绝；
- rule/change/query/model-access 共享同一个 canonical ModelPath contract；
- static illegal access compile-time fail；合法 dynamic access runtime fail-closed；
- atomic publication、old Context preservation、Context isolation；
- deterministic/source-aware compile Diagnostic 与 runtime denial；
- declaration compatibility 只作为到 P7 的 read-only migration boundary。

## 4. Review status

本 R02 overlay 是 `BM-R14 / DESIGN-P2-R16 / TESTDESIGN-P2-R17` 的输入，但它本身仍 `NEEDS_USER_DECISION / NEEDS_REQUIREMENT_REVIEW / MACHINE_BLOCKED`。不得将其描述为 PASSED。
# P2 Requirement Decision Overlay R03

> Overlay ID：`REQAN-P2-R01+DEC-OVERLAY-20260809-R03`  
> Base Requirement：`REQAN-P2-R01@d08612768131`（历史内容保持不改写）  
> Effective decisions：`DEC-P2-DIRECT-BRIDGE-AUTHORITY-001` + `DEC-P2-AC007-STAGE-BOUNDARY-001:OPTION_B`  
> Status：`NEEDS_REQUIREMENT_REVIEW / MACHINE_BLOCKED`

本 overlay materialize 用户已经明确授权的两项 P2 acceptance/runtime 决策。它不把 Requirement 标记为 PASSED；exact Requirement Review 与 machine lifecycle 仍必须发生。

## 1. Direct bridge authority — ACTIVE

`DEC-P2-DIRECT-BRIDGE-AUTHORITY-001` 继续 ACTIVE：

```java
bridge.execute(
    requestedRuleKey,
    operation,
    frameId,
    ownerResolutionId,
    optionalCursorId);
```

P2 caller 可以逐次选择 exact compiler-published `ModelAccessRuleKey` 与 `AccessOperation`；`AccessConsumerIrKey`/consumer provenance 不进入 authorization key。请求必须 exact 命中当前 immutable `ModelAccessPolicyIndex` 并通过 status/plan/proof/actual-target binding；无法证明即 DENY。不得 default allow、fuzzy key、operation fallback 或建立 secondary permission authority。

## 2. AC-007 stage boundary — Option B ACTIVE

用户明确选择：

> **B. P2 就必须提供足以执行原 AC-007 的 concrete production consumers。**

因此原 `REQAN-P2-R01` AC-007 的 concrete-entry acceptance 不被 seam-only interpretation supersede。P2 当前 scope 增加以下**最小但真实的 production acceptance surface**：

1. production main-source `Rule` representative protected-access consumer；
2. production main-source `change` representative protected-access consumer；
3. production main-source `custom-action` representative protected-access consumer。

三类入口必须由正常 production composition / public production API 可达，不得用 test-only wrapper、reflection、package-private backdoor、手工 issued pair/capability 或测试专用 permission map 代替。

### 2.1 三类 consumer 的共同验收语义

对每一类 consumer 都必须真实执行：

- exact authorized case：允许到达 capability-bound protected operation，并产生 exactly one expected effect；
- exact unauthorized case：稳定 DENY，operation/effect count = 0；
- same authorization facts parity：consumer kind 仅是 provenance/entry category，不得改变 PolicyIndex key、Guard decision 或 target/operation binding；
- no bypass：入口不得直接持有/调用 Gateway、Guard、raw operation port、PolicyIndex mutation API、issued-pair mint 或 capability mint；
- all paths：consumer -> same `ProtectedExecutionBridge` -> internal issuance -> resolver -> one-shot capability -> Gateway -> Guard -> bound operation/DENY。

### 2.2 P2 与 P3/P4/P6 的边界

Option B 扩大的是 **P2 AC-007 验收入口**，不是后续阶段的完整业务执行语义：

- P2 representative Rule consumer 不实现 P3 Information 求值、依赖 DAG、物化/失效/增量重算；
- P2 representative change/custom-action consumers 不实现 P4 Action/Produce 完整状态机；
- P6 QueryPlan 完整 compile/execute 仍留在 P6；P2 不因 Option B 新增 query concrete consumer 要求；
- 后续 P3/P4/P6 real executors 必须继续复用 P2 protected-access authority seam，不能建立新的旁路或第二权限 authority。

## 3. AC-007 final acceptance set

AC-007 的 current candidate completion 需要同时具备：

```text
production Rule consumer allow + deny
production change consumer allow + deny
production custom-action consumer allow + deny
consumer parity on same exact policy facts
public production reachability
Bridge/Gateway/Guard no-bypass
DENY before effects
```

仅证明 `ProtectedExecutionBridge` seam 存在仍然不足以关闭 AC-007；三类 representative production consumers 是 blocking acceptance evidence 的必需部分。

## 4. 其他 R01 语义保持不变

- System first-class identity/ownership boundary；
- RuleView identity `(system,name)`；
- READ/WRITE/EXECUTE independent，未声明默认拒绝；
- rule/change/query/model-access 共用 canonical `ModelPath`；
- static illegal access compile-time fail，合法 dynamic access runtime fail-closed；
- atomic publication / old Context preservation / Context isolation；
- deterministic/source-aware compile Diagnostic 与 runtime denial；
- declaration compatibility 只作为到 P7 的 read-only migration boundary。

## 5. Review status

本 R03 overlay 是 `BM-R15 / FLOW-R05 / DESIGN-P2-R17 / TESTDESIGN-P2-R18` 的输入。用户选择阻断已解除，但 Requirement exact Review、risk detection、specialist Reviews 与 machine lifecycle 尚未完成；不得描述为 PASSED，也不得据此进入 Implementation Plan/TDD/Development。

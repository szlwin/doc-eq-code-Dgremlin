# P2 Requirement Decision Overlay R04

> Overlay ID：`REQAN-P2-R01+DEC-OVERLAY-20260809-R04`  
> Base Requirement：`REQAN-P2-R01@d08612768131`（历史内容保持不改写）  
> Effective decisions：`DEC-P2-DIRECT-BRIDGE-AUTHORITY-001` + `DEC-P2-AC007-STAGE-BOUNDARY-001:OPTION_B` + `DEC-P2-ACCESS-OPERATIONS-001:READ_WRITE_ONLY`  
> Status：`NEEDS_REQUIREMENT_REVIEW / MACHINE_BLOCKED`

本 overlay materialize 用户明确授权的当前 P2 Requirement delta；不回写历史 R01，也不把当前候选标成 PASSED。

## 1. AccessOperation scope — READ / WRITE only

用户明确确认当前 P2 **没有 EXECUTE**。因此 current effective acceptance 只有：

```text
READ
WRITE
```

历史 R01 中所有 READ/WRITE/EXECUTE 三操作表述，对当前 candidate 按以下方式解释：

- READ：ACTIVE；
- WRITE：ACTIVE；
- EXECUTE：`N/A / superseded for current P2 by DEC-P2-ACCESS-OPERATIONS-001`。

Current P2 不新增 EXECUTE source syntax、raw IR、enum value、policy rule、Bridge operation 或 TestDesign case。P1 `AccessMode.READ/WRITE` 一对一转换到 P2 `AccessOperation.READ/WRITE`。

### Effective AC-004 delta

同一个 System + target + canonical ModelPath：

- 只有显式 READ rule 才允许 READ；
- 只有显式 WRITE rule 才允许 WRITE；
- READ 不隐含 WRITE；WRITE 不隐含 READ；
- 未声明 operation 默认 DENY。

## 2. Direct bridge authority — ACTIVE

`DEC-P2-DIRECT-BRIDGE-AUTHORITY-001` 的 authority 决策继续 ACTIVE：caller 可提交 exact compiler-published `ModelAccessRuleKey + AccessOperation(READ|WRITE)` 与 current frame/owner/cursor；PolicyIndex miss、operation mismatch、runtime proof failure、target mismatch 均 fail closed。

旧 direct-bridge changeset 中“AC-007 remains contract-only/future-only”的 consequence 已被用户 Option B 决策**局部 supersede**；Direct Bridge authority 本身未被 supersede。

## 3. AC-007 — Option B ACTIVE

P2 必须交付 production main-source representative consumers：

1. `RuleProtectedAccessEntry`；
2. `ChangeProtectedAccessEntry`；
3. `CustomActionProtectedAccessEntry`。

三类入口必须从正常 production composition 获取，共享同一 current-context `ProtectedExecutionBridge`，并真实执行 allow/deny/no-bypass/parity acceptance。仅存在 Bridge 或测试中手工 `new Entry(testBridge)` 不足以关闭 AC-007。

Production acceptance path：

```text
starter production runtime/factory
 -> ProtectedAccessComposition(current EngineContext)
 -> ruleEntry / changeEntry / customActionEntry
 -> same ProtectedExecutionBridge
 -> internal issuance
 -> target resolution
 -> one-shot capability
 -> Gateway
 -> Guard
 -> bound operation OR deterministic DENY
```

P3/P4/P6 完整业务执行语义继续留在后续阶段；未来 executor 必须复用 P2 authority seam。

## 4. 其他 R01 语义保持

- System first-class identity/version/source/ownership；
- RuleView identity `(SystemKey,name)`；
- rule/change/query-contract/model-access 共用 canonical `ModelPath`；
- static illegal access compile-time fail；合法 dynamic access runtime fail-closed；
- atomic publication / old Context preservation / Context isolation；
- deterministic source-aware compile Diagnostic 与 runtime denial；
- declaration compatibility 只作为到 P7 的 read-only migration boundary。

## 5. Gate

R04 仍 `NEEDS_REQUIREMENT_REVIEW / MACHINE_BLOCKED`。BM/Flow/Design/TestDesign 必须绑定本 overlay 的 exact revision；risk scan、independent Reviews 和 machine lifecycle 未完成前不得进入 Implementation Plan/TDD/Development。
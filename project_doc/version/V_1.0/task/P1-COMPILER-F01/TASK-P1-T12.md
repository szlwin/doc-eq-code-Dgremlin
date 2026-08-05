# TASK-P1-T12 — 十阶段 Compiler Pipeline 与 Publication 原子终态

- Current Iteration：`I004`
- Status：`COMPLETED / PASSED`
- Base：`dev_all@3f53ea4a31b0b1366aad383f665736b0487d4d00`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- Branch：`feature/p1-t12-compiler-pipeline-20260804-2331`
- PR：`#27`
- Current Completion：`COMPLETION-P1-T12-R04@923129b1f20d`
- Open P0/P1/P2：`0 / 0 / 0`

## Completion history

### R01 / I001 — INVALIDATED, PRESERVED

`COMPLETION-P1-T12-R01@c6a515820972` 及其 R38/R34、RED、Architecture、Review、CI、Artifact 和 documented Head 均作为不可变历史保留。

### R02 / I002 — INVALIDATED, PRESERVED

`COMPLETION-P1-T12-R02@5d5a7d72119b` 及其 R39/R35、RED、Architecture、Review、CI、Artifact 和 documented Head 均作为不可变历史保留。

### R03 / I003 — INVALIDATED, PRESERVED

`COMPLETION-P1-T12-R03@4d4cd5c4c049` 及其 R40/R36、RED、Architecture、Review、CI、Artifact 和 documented Head 均作为不可变历史保留。失效原因记录于 `review-p1-t12-r06-invalidation.md`：artifact snapshot 缺少非递归遍历、完成态 memoization 和资源预算。

### R04 / I004 — CURRENT

- Design：`DESIGN-R41@P1-T12-REWORK-I004`
- Plan：`TP-P1-COMPILER-F01-R37@P1-T12-REWORK-I004`
- TDD：`TDD-P1-T12-R04@1270d6f2b829`
- Architecture：`DEVSKEL-P1-T12-R04@c82e0a3023da`
- Development：`DEV-P1-T12-R04@923129b1f20d`
- Code Review：`CODEREVIEW-P1-T12-R07@923129b1f20d`
- Testing：`TESTING-P1-T12-R04@923129b1f20d`
- Completion：`COMPLETION-P1-T12-R04@923129b1f20d`
- Reviews：`REV-000570`～`REV-000590`
- Evidence：`EVD-000885`～`EVD-000909`
- Findings：`FND-P1-T12-I004-001`～`003` CLOSED

## Current published contract

- 十 Pass 固定名称和顺序；前九 Pass 无 Publication capability；
- final Pass 只准备 candidate，Pipeline 在完整聚合所有 Diagnostic 后唯一 commit；
- ERROR、取消、超时、Clock/timing 故障、Pass 异常和 candidate 缺失时 publisher=0；
- Warning/Info 在成功结果中保留，成功路径 publisher=1；
- PUBLISHED 为不可逆外部提交终态；
- 所有 Context 关闭后公开及包内访问拒绝，Result 为独立不可变快照；
- timing long 溢出稳定 fail-closed，start timestamp 到期时不执行 Pass；
- artifact 使用显式 traversal stack、VISITING/FROZEN identity memoization 和四类资源预算；
- 共享 DAG 按唯一图线性遍历并复用 frozen identity；
- Frozen List/Set/Map 缓存结构 hash，Set/Map collision 使用 canonical structural ID；
- 循环、null、未知可变对象、collision、conflict、null result/status、publisher exception 和重复 prepare 均稳定处理；
- 不执行 P2～P7 runtime，不实现 T13/T14/T15。

## Validation

- Valid RED：`1270d6f2b829a568f7edda4a23e21ba2748d7a50` / Run `30974123330` / 6 failures / 0 errors；
- Hash Review RED：`cbeed46dbf053184f247184ad9976c706d42f500` / Run `30974844132` / 1 failure / 0 errors；
- Clean-code Head：`923129b1f20d6bebe589231b770b5c7675b52737`；
- P0 Run：`30975103715` — SUCCESS；
- Artifact：`8917961744`；
- SHA-256：`df328a44496836e018c4725714adece969f46e0f71a0228c337ff9cadb71a640`；
- I004 17/17；T12 83/83；Compiler 402/402；正常测试 522/522；
- Surefire XML 94；Errors/Skipped 0/0；
- Java 8、12 模块 Reactor、故意失败门禁：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

PR #27 未经用户明确授权不得合并；PR #27 合并前 `TASK-P1-T13` 保持 `BLOCKED_UNTIL_PR_27_MERGE`。

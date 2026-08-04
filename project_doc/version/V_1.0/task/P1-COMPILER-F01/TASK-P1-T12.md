# TASK-P1-T12 — 十阶段 Compiler Pipeline 与 Publication 原子终态

- Current Iteration：`I002`
- Status：`COMPLETED / PASSED`
- Base：`dev_all@3f53ea4a31b0b1366aad383f665736b0487d4d00`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- Branch：`feature/p1-t12-compiler-pipeline-20260804-2331`
- PR：`#27`
- Current Completion：`COMPLETION-P1-T12-R02@5d5a7d72119b`
- Open P0/P1/P2：`0 / 0 / 0`

## Completion history

### R01 / I001 — INVALIDATED, PRESERVED

- Completion：`COMPLETION-P1-T12-R01@c6a515820972`
- Design/Plan：`DESIGN-R38@P1-T12-I001` / `TP-P1-COMPILER-F01-R34@P1-T12-I001`
- Final documented Head：`49b9beee65dbc5e5db77302a7128a34a2ab77386`
- Invalidation Review：`review-p1-t12-r02-invalidation.md`
- Reason：独立 Review 确认 Publication capability 泄露、Context/Result 可变、commit 后可降级、executedPass 失真及 Oracle 不足。
- R01 的文档、RED、Architecture、Review、Completion、CI 和 Artifact 均原样保留。

### R02 / I002 — CURRENT

- Design：`DESIGN-R39@P1-T12-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R35@P1-T12-REWORK-I002`
- TDD：`TDD-P1-T12-R02@a958141d0465`
- Architecture：`DEVSKEL-P1-T12-R02@a7f8d99b1afe`
- Development：`DEV-P1-T12-R02@4499bd90849d`
- Code Review：`CODEREVIEW-P1-T12-R03@5d5a7d72119b`
- Testing：`TESTING-P1-T12-R02@5d5a7d72119b`
- Completion：`COMPLETION-P1-T12-R02@5d5a7d72119b`
- Reviews：`REV-000504`～`REV-000535`
- Evidence：`EVD-000808`～`EVD-000837`
- Findings：`FND-P1-T12-I002-001`～`008` CLOSED

## Current published contract

- 十个 Pass 名称和顺序固定；前九 Pass 无 Publication capability；
- 第十 Pass 通过一次性 `PublicationPassContext` 唯一持有 publisher；
- 发布前 ERROR/cancel/timeout/clock/token/Pass 异常路径 publisher 调用数精确为 0；
- 成功路径 publisher 调用数精确为 1；
- publish 前即时重查 token、Deadline 和 ERROR；
- publisher 返回 PUBLISHED 后立即进入不可逆 PUBLISHED，post-commit 故障不降级；
- 每 Pass Context 在 finally 中关闭，retained Context 全部访问拒绝；
- Session 终态拒绝语义写入；Result 为独立不可变值快照；
- mutable container artifact 递归复制，未知对象和循环图稳定拒绝；
- start-clock 成功后才登记 executedPass；
- conflict、null result/status、publisher exception、重复 publish 和不稳定 status 均稳定处理；
- 不执行 P2～P7 runtime，不实现 T13/T14/T15。

## Validation

- Valid RED：`a958141d0465...` / Run `30932917420` / 12 failures / 0 errors；
- Clean-code Head：`5d5a7d72119b5a36a38b19cda44186de70911912`；
- P0 Run：`30934448175` — SUCCESS；
- Artifact：`8902515127`；
- SHA-256：`2203b46ba83ad9c5a8784741efc1edef658feae77b91ea2f4cef383ca3569914`；
- I002 34/34；I001 20/20；T12 54/54；Compiler 373/373；正常测试 493/493；
- Surefire XML 90；Errors/Skipped 0/0；
- Java 8、12 模块 Reactor、故意失败门禁：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

PR #27 未经用户明确授权不得合并；PR #27 合并前 `TASK-P1-T13` 保持阻断。

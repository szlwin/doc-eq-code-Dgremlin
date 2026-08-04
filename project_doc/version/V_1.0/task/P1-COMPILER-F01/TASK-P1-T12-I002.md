# TASK-P1-T12 / I002 — Publication 原子性与 Session 冻结返工

- Status：`IN_PROGRESS / DESIGN_PASSED`
- Base：`PR27@49b9beee65dbc5e5db77302a7128a34a2ab77386`
- Dependency：`COMPLETION-P1-T11-R02@86b55b45d1cd`
- Invalidated History：`COMPLETION-P1-T12-R01@c6a515820972`
- Branch：`feature/p1-t12-compiler-pipeline-20260804-2331`
- PR：`#27`
- Design：`DESIGN-R39@P1-T12-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R35@P1-T12-REWORK-I002`
- Review：`NEEDS_CHANGES / REWORK`
- Open P0/P1/P2：`0 / 3 / 2`

## Findings

- `FND-P1-T12-I002-001` `[P1][BLOCKER]`：普通 Pass 持有 Publication capability；
- `FND-P1-T12-I002-002` `[P1][BLOCKER]`：Context 逃逸、终态 Session 和结果仍可变；
- `FND-P1-T12-I002-003` `[P1][BLOCKER]`：publish 成功后 clock/token 等可降级为 FAILED；
- `FND-P1-T12-I002-004` `[P2]`：start-clock 失败仍记录 Pass 已执行；
- `FND-P1-T12-I002-005` `[P2]`：缺少对应负向 Oracle。

## Goal

交付 Publication capability 隔离、每 Pass Context 生命周期、Session 终态封闭、不可变结果快照、Publication commit 不可降级和真实 executedPass 记录，并关闭全部五项 Findings。

## History preservation

R38、R34、I001 RED、Architecture、Development、Review、Testing、Completion、CI、Artifact 和最终 documented Head 均保持原文件及原 SHA，不回写为通过。I002 使用新的 Revision、Evidence、Review 和 Completion。

## Stop conditions

- R39/R35 未早于 I002 RED；
- 任何普通 Pass 仍可获得 publisher；
- publish 成功后仍存在 FAILED 路径；
- 结果仍从可变 Session 动态读取；
- retained Context 可在关闭或终态后读写；
- Open P0/P1 未清零；
- 未完成最终 P0、Artifact、Revision Integrity 和独立 Review；
- 用户未授权时合并 PR #27 或启动 T13。

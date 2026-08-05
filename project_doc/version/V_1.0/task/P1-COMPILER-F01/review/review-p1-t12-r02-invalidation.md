# TASK-P1-T12 Independent Review R02 — I001 Invalidation

- Reviewed Head：`49b9beee65dbc5e5db77302a7128a34a2ab77386`
- Reviewed Completion：`COMPLETION-P1-T12-R01@c6a515820972`
- Result：`NEEDS_CHANGES / REWORK`
- Open P0/P1/P2：`0 / 3 / 2`
- Next Iteration：`TASK-P1-T12 / I002`
- New Design：`DESIGN-R39@P1-T12-REWORK-I002`
- New Plan：`TP-P1-COMPILER-F01-R35@P1-T12-REWORK-I002`
- Reviews：`REV-000504`～`REV-000515`
- Evidence：`EVD-000808`～`EVD-000812`

## Findings

### `FND-P1-T12-I002-001` `[P1][BLOCKER]` — OPEN

普通 `PassContext.publicationRequest()` 暴露 `ContextPublisher`，早期 Pass 可以在返回 ERROR 前直接发布，形成外部已发布但 Pipeline=`FAILED`。

### `FND-P1-T12-I002-002` `[P1][BLOCKER]` — OPEN

Pass 可保留 Context；Session mutator 不检查终态；Result 动态读取 Session。返回后的 PUBLISHED 结果可被写入 ERROR/artifact，第二次 execute 可污染第一次结果。

### `FND-P1-T12-I002-003` `[P1][BLOCKER]` — OPEN

PublicationPass 完成副作用后仍执行 end-clock 和 cancel/deadline 后置检查，可能把已提交结果降级为 FAILED。

### `FND-P1-T12-I002-004` `[P2]` — OPEN

`recordPass` 早于 start-clock；start-clock 抛异常时真实 Pass 调用数为 0，但 executedPasses 错误记录为 1。

### `FND-P1-T12-I002-005` `[P2]` — OPEN

I001 20 项 Oracle 未覆盖 publisher 调用计数、Context 逃逸、跨 Session 污染和 post-publication 故障。

## Historical integrity

R38/R34、I001 RED、Architecture、Development、Review、Testing、Completion、CI 与 Artifact 均保留原文件和原 SHA。R01 只被标记为失效历史，不删除、不覆盖、不改写为通过。

## Gate

- PR #27：`NOT_ALLOWED_TO_MERGE`
- TASK-P1-T13：`BLOCKED_UNTIL_PR_27_REWORK_MERGED`
- I002 Completion 前 Open P0/P1/P2 必须清零。

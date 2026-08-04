# TASK-P1-T11 I002 Independent Review Input

- Evidence：`EVD-000766`
- Reviewed Head：`ae35e1cc745bf096f35c20ba73dc4909286e7a3b`
- Review Result：`NEEDS_CHANGES / REWORK`
- Open P0/P1/P2：`0 / 1 / 1`
- Invalidated Completion：`COMPLETION-P1-T11-R01@f09d9786fad8`
- Preserved History：I001 的 Design、Plan、RED、Architecture、Review、Completion、CI、Artifact 与 rejected attempt

## Findings

### FND-P1-T11-I002-001 `[P1][BLOCKER]`

`DeferredClassificationInput.Builder.resolvedReferences(null)` 将 provided 标记为 true，随后 null 被归一化为空列表，使不完整请求进入 Registry。必须让 null 保持“未提供”语义，并与显式空列表区分。

### FND-P1-T11-I002-002 `[P2]`

`DeferredDefinitionBuilder.build()` 直接遍历调用方 List，未在分类开始前形成批次快照。必须先复制整个批次，后续只遍历快照，并将复制阶段运行时异常收敛为稳定 Diagnostic。

## Required next action

建立 `TASK-P1-T11 / I002`、`DESIGN-R37` 与 `TP-R33`，执行 TDD RED、Architecture、Development、独立 Review、Testing 与 Completion Verification。PR #26 在 R02 Completion 通过前不得合并，TASK-P1-T12 继续阻断。

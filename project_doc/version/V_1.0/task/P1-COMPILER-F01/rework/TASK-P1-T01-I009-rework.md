# TASK-P1-T01 REWORK I009

- 任务：`TASK-P1-T01`
- Iteration：`I009`
- 状态：`REWORK`
- 被推翻 Completion：`COMPLETION-P1-T01-R02@a0daaf94f74b`
- 复审代码 Head：`dd590b57edd86f7e74d9c185d37306bc7669ee12`
- 当前 PR：`#16`
- 新设计：`DESIGN-R07@P1-T01-REWORK-I009`
- 新实施计划：`TP-P1-COMPILER-F01-R03@P1-T01-REWORK-I009`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 重开原因

完整 BM-R05、DESIGN-R05、DESIGN-R06 复审新增两个 P1 Blocker：

1. `CoreConfigProjection` 只能通过普通不可修改 List 异常拒绝写入，未产生稳定 `MIX-PROJECTION-WRITE` Diagnostic 和专用异常；
2. `PublishedSourceDependency` 允许 `declarationSourceRef.sourceId()` 与 `fromSourceId` 表达不同来源事实。

I008 已关闭的五个 Finding 继续保持 CLOSED，但 R02 Completion 中“开放 P0/P1 为 0”的结论已被本次 Review 推翻。旧 Completion、Review 和 Evidence 仅作为历史保留，不删除、不覆盖。

## 门禁

- PR #16 已转为 Draft，在 R03 Completion 前不得合并；
- PR #15 继续阻断；
- `TASK-P1-T03` 不得启动；
- 本 iteration 只修复 T01 发布边界合同。

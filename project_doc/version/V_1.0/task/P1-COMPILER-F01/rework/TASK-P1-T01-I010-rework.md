# TASK-P1-T01 REWORK I010

- 任务：`TASK-P1-T01`
- Iteration：`I010`
- 状态：`REWORK`
- 被推翻 Completion：`COMPLETION-P1-T01-R03@175b86e1e3ea`
- 复审代码 Head：`6d8f0af5ceba8f716604cf37c71fdee145a8a3f2`
- 当前 PR：`#16`
- 新设计：`DESIGN-R08@P1-T01-REWORK-I010`
- 新实施计划：`TP-P1-COMPILER-F01-R04@P1-T01-REWORK-I010`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 重开原因

独立 Review 发现 `ProjectionReadOnlyList` 没有覆盖 `subList(int, int)`。空 `subList(0, 0)` 继承 `AbstractList.SubList` 后，`clear/removeAll/retainAll/removeIf/replaceAll/sort` 等写方法可以无操作成功，未产生 `ProjectionWriteRejectedException` 和 `MIX_PROJECTION_WRITE` Diagnostic。

R03 中“所有 exposed List mutation paths 已关闭”以及“开放 P0/P1 为 0”的结论被本次 Review 推翻。R03 Completion、Review 和 Evidence 作为历史保留，不删除、不覆盖。

## 本轮门禁

- PR #16 已转为 Draft，I010 Completion 前不得合并；
- 本轮必须同时审计并关闭 `subList`、Iterator 和 ListIterator 派生写入口；
- PR #15 继续阻断；
- `TASK-P1-T03` 不得启动；
- 本 iteration 只修复 T01 Projection 派生视图合同。

# TASK-P1-T01 REWORK I010

- 任务：`TASK-P1-T01`
- Iteration：`I010`
- 状态：`COMPLETED`
- 被推翻 Completion：`COMPLETION-P1-T01-R03@175b86e1e3ea`
- 复审代码 Head：`6d8f0af5ceba8f716604cf37c71fdee145a8a3f2`
- 当前 Completion：`COMPLETION-P1-T01-R04@ee99223a243f`
- 当前 PR：`#16`
- 设计：`DESIGN-R08@P1-T01-REWORK-I010`
- 实施计划：`TP-P1-COMPILER-F01-R04@P1-T01-REWORK-I010`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 重开原因

独立 Review 发现 `ProjectionReadOnlyList` 没有覆盖 `subList(int, int)`。空 `subList(0, 0)` 继承 `AbstractList.SubList` 后，`clear/removeAll/retainAll/removeIf/replaceAll/sort` 等写方法可以无操作成功，未产生 `ProjectionWriteRejectedException` 和 `MIX_PROJECTION_WRITE` Diagnostic。

R03 中“所有 exposed List mutation paths 已关闭”以及“开放 P0/P1 为 0”的结论被本次 Review 推翻。R03 Completion、Review 和 Evidence 作为历史保留，没有删除或覆盖。

## 完成结果

- 有效 RED：`TDD-P1-T01-R04@f87a3f96fcbb`，P0 Run `30729765475`，25 项测试中新增 4 项失败，既有 21 项全绿；
- 架构骨架：`DEVSKEL-P1-T01-R04@1865378a29e3`，P0 Run `30729803354`，仅 Iterator 显式骨架行为保持 RED；
- Development：`DEV-P1-T01-R04@ee99223a243f`；
- Code Review：`CODEREVIEW-P1-T01-R04@ee99223a243f`；
- Testing：`TESTING-P1-T01-R04@ee99223a243f`；
- Completion：`COMPLETION-P1-T01-R04@ee99223a243f`；
- 最终代码 P0 Run：`30729866803`；
- Context 测试：26 run / 0 failures / 0 errors / 0 skipped；
- 完整 11 模块 Reactor、Java 8 和故意失败阻断门禁均 PASSED；
- Review：`REV-000103`～`REV-000111` 全部 PASSED；
- Evidence：`EVD-000355`～`EVD-000360`；
- `FND-P1-T01-I010-001` CLOSED；
- 开放 P0/P1：无。

## 已冻结合同

- 根 List、空/非空/嵌套 `subList` 的所有写方法均产生专用拒绝异常；
- `subList` 保留标准索引校验，但返回防御性 `ProjectionReadOnlyList` 快照；
- 根 List 和派生子列表的 Iterator/ListIterator 写方法均优先产生 `MIX-PROJECTION-WRITE`；
- 写入拒绝不会修改来源 `CompiledModelSet`、根列表或派生快照；
- `@Override` 独占一行，新增方法和重要逻辑使用中文注释。

## 后续门禁

- PR #16 合并前 PR #15 继续阻断；
- PR #16 合并后，TASK-P1-T02 必须基于最新 `dev_all` rebase、适配并重新验证；
- T02 重验证完成前不得启动 `TASK-P1-T03`。

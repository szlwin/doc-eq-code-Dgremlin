# TASK-P1-T01 REWORK I009

- 任务：`TASK-P1-T01`
- Iteration：`I009`
- 状态：`COMPLETED`
- 被推翻 Completion：`COMPLETION-P1-T01-R02@a0daaf94f74b`
- 复审代码 Head：`dd590b57edd86f7e74d9c185d37306bc7669ee12`
- 当前 PR：`#16`
- 设计：`DESIGN-R07@P1-T01-REWORK-I009`
- 实施计划：`TP-P1-COMPILER-F01-R03@P1-T01-REWORK-I009`
- Completion：`COMPLETION-P1-T01-R03@175b86e1e3ea`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 重开原因

完整 BM-R05、DESIGN-R05、DESIGN-R06 复审新增两个 P1 Blocker：

1. `CoreConfigProjection` 只能通过普通不可修改 List 异常拒绝写入，未产生稳定 `MIX-PROJECTION-WRITE` Diagnostic 和专用异常；
2. `PublishedSourceDependency` 允许 `declarationSourceRef.sourceId()` 与 `fromSourceId` 表达不同来源事实。

I008 已关闭的五个 Finding 继续保持 CLOSED，但 R02 Completion 中“开放 P0/P1 为 0”的结论已被本次 Review 推翻。旧 Completion、Review 和 Evidence 仅作为历史保留，不删除、不覆盖。

## R03 流程结果

- TDD：`TDD-P1-T01-R03@81b071739b19`，P0 Run `30707008948` 形成有效 RED，`REV-000094` PASSED；
- Architecture Skeleton：`DEVSKEL-P1-T01-R03@7f41cb0d06dd`，`REV-000095`、`REV-000096` PASSED；
- Development：`DEV-P1-T01-R03@6c8a2d1a7cd5`；
- Code Review：`CODEREVIEW-P1-T01-R03@175b86e1e3ea`，`REV-000097`～`REV-000100` PASSED；
- Testing：`TESTING-P1-T01-R03@175b86e1e3ea`，P0 Run `30707306280` 全绿，`REV-000101` PASSED；
- Completion：`COMPLETION-P1-T01-R03@175b86e1e3ea`，`REV-000102` PASSED；
- Evidence：`EVD-000349`～`EVD-000354` ACTIVE；
- Context 测试：21 run / 0 failures / 0 errors / 0 skipped；
- 完整 11 模块 Maven Reactor、Java release 8 和故意失败门禁全部 PASSED；
- MySQL：T01 无数据库变更，`SKIPPED_NOT_APPLICABLE`。

## 已关闭 Findings

1. `FND-P1-T01-I009-001`：增加 `ProjectionWriteRejectedException`，携带稳定 `MIX-PROJECTION-WRITE` Diagnostic；
2. `FND-P1-T01-I009-002`：依赖边构造与 Manifest 双层校验声明 SourceRef 属于 `fromSourceId`；
3. `FND-P1-T01-I009-003`：Data/View/Rule List 的 Java 8 变更入口也统一抛出专用拒绝异常。

开放 P0/P1 Finding：无。

## 后续门禁

- PR #16 在最终文档化 Head P0 通过后恢复 Ready for review；
- PR #15 继续阻断，必须等待 PR #16 合并后 rebase、适配并重新验证；
- `TASK-P1-T03` 在 T02 重验证完成前不得启动；
- 本 iteration 未实现 T02 或 T03 行为。

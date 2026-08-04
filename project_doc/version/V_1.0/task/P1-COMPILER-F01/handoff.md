# P1-COMPILER-F01 阶段交接

> T01～T07 已合并到 `dev_all`。当前完成任务为 `TASK-P1-T08 / I002`，有效 Completion 为 `COMPLETION-P1-T08-R02@bab0993ecfd8`。R01 已被独立 Review 推翻并作为不可变历史保留。PR #23 尚未合并，T09 保持阻断。

## 已合并前置任务

- T01：`COMPLETION-P1-T01-R04@ee99223a243f`
- T02：`COMPLETION-P1-T02-R05@35376308b013`
- T03：`COMPLETION-P1-T03-R05@91271c9a1c20`
- T04：`COMPLETION-P1-T04-R02@0699c6bc2ed4`
- T05：`COMPLETION-P1-T05-R03@30529276cd8f`
- T06：`COMPLETION-P1-T06-R04@242db638c61d`
- T07：`COMPLETION-P1-T07-R02@ffe544e3060d`，通过 PR #22 合并；
- T08 Base：`dev_all@c6cd8ec156563480ec30989cdd358d4979a8599b`。

## T08 Completion 历史

- R01 / I001：`COMPLETION-P1-T08-R01@ab432a3189f4`；独立 Review 发现 3 个 P1 与 1 个 P2，当前有效性失效但所有文档、Evidence、P0、Artifact 和失败 attempt 不可变保留；
- R02 / I002：`COMPLETION-P1-T08-R02@bab0993ecfd8`；当前有效。

## T08 I002（当前有效）

- Design：`DESIGN-R30@P1-T08-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R26@P1-T08-REWORK-I002`
- TDD：`TDD-P1-T08-R02@bfc8e4df822a`
- Architecture：`DEVSKEL-P1-T08-R02@3e85814d5cf5`
- Development：`DEV-P1-T08-R02@bab0993ecfd8`
- Code Review：`CODEREVIEW-P1-T08-R02@bab0993ecfd8`
- Testing：`TESTING-P1-T08-R02@bab0993ecfd8`
- Completion：`COMPLETION-P1-T08-R02@bab0993ecfd8`
- Reviews：`REV-000353`～`REV-000373`
- Evidence：`EVD-000600`～`EVD-000622`
- Findings：`FND-P1-T08-I002-001/002/003/004` CLOSED
- Clean-code Head：`bab0993ecfd8c344beead62712ba8dc02621038d`
- P0 Run：`30871077040`
- Artifact：`8877900378`
- Artifact SHA-256：`a6eed26d25e9962a28d79abc4108fc61992d5d43eae7c70261c38403a8a3d68c`
- Artifact 独立校验：实际 ZIP SHA-256 与 GitHub digest 一致
- I002 22/22；I001 T08 12/12；T08 34/34；Symbol 66/66；Compiler 195/195
- XML 30/30；YAML 59/59；Context 正常 26/26；Demo 4/4；Legacy 1/1
- 正常测试 315/315；故意失败门禁 1 项按预期失败并被识别
- 12 模块 Reactor、Java release 8、故意失败阻断：PASSED
- MySQL：`SKIPPED_NOT_APPLICABLE`
- Open P0/P1/P2：`0 / 0 / 0`

## 当前发布合同

- Resolver 输入必须与 SymbolTable 的完整 RawDefinitionSet 构建快照一致；
- 快照不一致在任何索引前返回 `reference.input.snapshot-mismatch`；
- qualified Information 必须严格为 `system.name` 两段，禁止多段、未限定和空白 segment；
- simple target、View target-main、System ref/name、RuleView、Action、Directory rel 与 property 均在 TypedKey 构造前 fail-closed；
- System data-ref/view-ref 节点缺失 ref/name 返回 `reference.owner.invalid`；
- 输入相关 IllegalArgumentException 不得越过 ReferenceResolutionResult；
- lexical 失败分类使用预聚合 CandidateSummary，不保存或扫描候选 List；
- 小预算证明 12 个同名 owner 候选与 9 个失败引用只执行 9 次摘要查询；
- 成功路径始终构造精确 TypedKey 并调用 SymbolTable.find，lexical 摘要不参与成功目标替代；
- 快照失败入口阻断；普通引用错误完整聚合、稳定排序；任一失败不发布部分集合；
- 真实 Canonical → T06 → T07 → T08 集成矩阵覆盖 ref/name、nested owner、前向 RuleView、qualified Information 与 malformed lexical；
- 无模糊匹配、大小写降级、跨类型 fallback、I/O、网络、反射执行或 static mutable state。

## Revision Integrity

- R30 first commit：`04c590caba096b999d2320e364b464143f24f3e0`
- R30 blob：`5f392e855b5f5e3a3dc93e19f02c03db57cebe11`
- R26 first commit：`dbea77b8698648acc35cbdb947687c58597d6612`
- R26 blob：`6ab25d67c788933d12e76206636590880c0c3598`
- R30/R26 在有效 RED 前创建，clean-code Head 复核 blob 未变化

## PR、恢复与下一步

- 当前 PR：`#23`
- Branch：`feature/p1-t08-reference-resolution-20260803-2254`
- Base：`dev_all@c6cd8ec156563480ec30989cdd358d4979a8599b`
- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t08-r02/completion-report.json`
- Review：`project_doc/version/V_1.0/task/P1-COMPILER-F01/review/review-p1-t08-r02.md`
- 机器恢复入口：`project_doc/version/V_1.0/tdd_p1_t08_r02_completion.json`
- `@Override` 独占一行，方法和关键逻辑使用中文注释
- 未修改 Context、T06 Raw、T07 Symbol 公共合同或 Compiler API
- 临时源码快照 workflow 已删除，不存在最终 PR 文件树
- 未启动 Information expression、ModelAccess、Deferred、Pipeline、Digest、Publication 或 T09/T10/P2～P7
- 下一 Agent：`IndependentReviewAgent`
- 未经用户明确授权不得合并 PR #23
- PR #23 合并前 `TASK-P1-T09` 保持未启动和阻断。
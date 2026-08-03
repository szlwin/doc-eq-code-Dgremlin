# P1-COMPILER-F01 阶段交接

> T01～T06 已合并到 `dev_all`。当前完成任务为 `TASK-P1-T07 / I002`，有效 Completion 为 `COMPLETION-P1-T07-R02@ffe544e3060d`。R01 已被后续独立 Review 推翻并作为不可变历史保留。PR #22 尚未合并，T08 保持阻断。

## 已合并前置任务

- T01：`COMPLETION-P1-T01-R04@ee99223a243f`
- T02：`COMPLETION-P1-T02-R05@35376308b013`
- T03：`COMPLETION-P1-T03-R05@91271c9a1c20`
- T04：`COMPLETION-P1-T04-R02@0699c6bc2ed4`
- T05：`COMPLETION-P1-T05-R03@30529276cd8f`
- T06：`COMPLETION-P1-T06-R04@242db638c61d`，merge / dev_all Head `3e0492b0319173c87abff6952d4dad0f5507c31c`

## T07 Completion 历史

- R01 / I001：`COMPLETION-P1-T07-R01@7f4ee8a0ee5a`；后续 Review 发现两个 P1 和一个 P2，已失效但不可变保留；
- R02 / I002：`COMPLETION-P1-T07-R02@ffe544e3060d`；当前有效。

## T07 I002（当前有效）

- Design：`DESIGN-R28@P1-T07-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R24@P1-T07-REWORK-I002`
- TDD：`TDD-P1-T07-R02@619714e24fd5`
- Rejected Skeleton：`DEVSKEL-P1-T07-R02-A01@15f6e0e8ef9b`
- Architecture：`DEVSKEL-P1-T07-R02@ffe544e3060d`
- Development：`DEV-P1-T07-R02@ffe544e3060d`
- Code Review：`CODEREVIEW-P1-T07-R02@ffe544e3060d`
- Testing：`TESTING-P1-T07-R02@ffe544e3060d`
- Completion：`COMPLETION-P1-T07-R02@ffe544e3060d`
- Review：`REV-000324`～`REV-000338`
- Evidence：`EVD-000567`～`EVD-000585`
- Findings：`FND-P1-T07-I002-001/002/003` CLOSED
- Clean-code Head：`ffe544e3060dd15b82a73677b30147aaa4b360af`
- P0 Run：`30819541292`
- Artifact：`8858227740`
- Artifact SHA-256：`e976842a19ff208a951e143e0e66e90a2c2fb75d4782c1c26850f133cde15356`
- Artifact 独立校验：实际 ZIP SHA-256 与 GitHub digest 一致
- Symbol 32/32；Compiler 161/161；XML 30/30；YAML 59/59；Context 正常 26/26；Demo 4/4；Legacy 1/1
- 12 模块 Reactor、Java release 8、故意失败门禁：PASSED
- MySQL：`SKIPPED_NOT_APPLICABLE`
- Open P0/P1/P2：`0 / 0 / 0`

## 当前发布合同

- 复用 Context 已发布的 11 类 TypedKey，不建立平行字符串 Key；
- 结构 owner 使用原始 Raw lexical parent name 精确比较；
- TypedKey 独立执行 Context canonical trim，RawDefinition lexical 保持原值；
- RuleView 不读取最近 System，而是按自身 ownerToken 在完整 System 集合查找；
- RuleView 支持前向文档顺序、多 System、非最近 System、多 RuleView 和同名 owner 隔离；
- missing RuleView owner 产生 `symbol.owner.system.missing`；
- Information 与 Produce 第二遍登记，Produce 使用 sourceOrdinal；
- 同 TypedKey 重复失败，首定义不覆盖，Diagnostic 保存双方 SourceRef；
- Diagnostic 使用 LinkedHashSet 单次 add 去重，最终稳定排序；
- 两遍完整扫描后统一判定 FAILED，失败不发布部分表；
- SymbolTable 包装 Context ImmutableRegistry，keys/definitions 稳定、有序、不可变；
- `ROOT_CONFIG`、`RULE`、`MODEL_ACCESS` 保持 Raw 事实；
- RawReference 不解析、不执行 I/O。

## Revision Integrity

- R28 first commit：`b717288297a5c78a79584412909f7e74550f7beb`
- R28 blob：`142ec612eb5658f41108330a4ca5b545521fd85c`
- R24 first commit：`577c68cb5b79993909660485110f11f4f8495f7a`
- R24 blob：`7a041c5c3811c1725482ee0b5ad288428c745a4e`
- R28/R24 在 RED 前创建，clean-code Head 复核 blob 未变化

## PR、恢复与下一步

- 当前 PR：`#22`
- Branch：`feature/p1-t07-symbol-table-20260803-1958`
- Base：`dev_all@3e0492b0319173c87abff6952d4dad0f5507c31c`
- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t07-r02/completion-report.json`
- Review：`project_doc/version/V_1.0/task/P1-COMPILER-F01/review/review-p1-t07-r02.md`
- 机器恢复入口：`project_doc/version/V_1.0/tdd_p1_t07_r02_completion.json`
- `@Override` 独占一行，方法和关键逻辑使用中文注释
- 未修改 Context、Raw、Source Graph、Compiler API、XML/YAML Frontend 生产代码
- 未启动 ReferenceResolver、Information、ModelAccess、Deferred、Pipeline、Digest、Publication 或 T08
- 下一 Agent：`IndependentReviewAgent`
- 未经用户明确授权不得合并 PR #22
- PR #22 合并前 `TASK-P1-T08` 保持未启动和阻断

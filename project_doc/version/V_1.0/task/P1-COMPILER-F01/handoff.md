# P1-COMPILER-F01 阶段交接

> T01～T06 已合并到 `dev_all`。当前完成任务为 `TASK-P1-T07 / I001`，有效 Completion 为 `COMPLETION-P1-T07-R01@7f4ee8a0ee5a`。PR #22 尚未合并，T08 保持阻断。

## 已合并前置任务

- T01：`COMPLETION-P1-T01-R04@ee99223a243f`
- T02：`COMPLETION-P1-T02-R05@35376308b013`
- T03：`COMPLETION-P1-T03-R05@91271c9a1c20`
- T04：`COMPLETION-P1-T04-R02@0699c6bc2ed4`
- T05：`COMPLETION-P1-T05-R03@30529276cd8f`
- T06：`COMPLETION-P1-T06-R04@242db638c61d`，merge / dev_all Head `3e0492b0319173c87abff6952d4dad0f5507c31c`

T06 R01～R03 被后续独立 Review 推翻并作为不可变历史保留；T07 只使用 R04 作为有效依赖。

## T07 I001（当前完成）

- Design：`DESIGN-R27@P1-T07-I001`
- Plan：`TP-P1-COMPILER-F01-R23@P1-T07-I001`
- TDD：`TDD-P1-T07-R01@9e7dbc1bb451`
- Architecture Skeleton：`DEVSKEL-P1-T07-R01@c4d33f9ec8e9`
- Development：`DEV-P1-T07-R01@7f4ee8a0ee5a`
- Code Review：`CODEREVIEW-P1-T07-R01@7f4ee8a0ee5a`
- Testing：`TESTING-P1-T07-R01@7f4ee8a0ee5a`
- Completion：`COMPLETION-P1-T07-R01@7f4ee8a0ee5a`
- Review：`REV-000309`～`REV-000323`
- Evidence：`EVD-000551`～`EVD-000566`
- Finding：`FND-P1-T07-I001-001` CLOSED
- Clean-code Head：`7f4ee8a0ee5a8be84e8edfe715a85189858ac425`
- P0 Run：`30814383829`
- Artifact：`8856098502`
- Artifact SHA-256：`1f71fb0f3f2615dfc599792e5760993048f832a085bdfed965b44b0f13acfdf8`
- Artifact 独立校验：实际 ZIP SHA-256 与 GitHub digest 一致
- Symbol 23/23；Compiler 152/152；XML 30/30；YAML 59/59；Context 正常 26/26；Demo 4/4；Legacy 1/1
- 12 模块 Reactor、Java release 8、故意失败门禁：PASSED
- MySQL：`SKIPPED_NOT_APPLICABLE`
- 开放 P0/P1/P2：无

## 发布合同

- 复用 Context 已发布的 11 类 TypedKey，不建立平行字符串 Key；
- 第一遍登记顶层和 owner Key；
- 第二遍登记 InformationKey 与 ProduceKey；
- Information 精确绑定 SystemKey；
- Produce 精确绑定 ActionKey，并以 Raw sourceOrdinal 区分无名定义；
- 同 TypedKey 重复失败，首定义不被覆盖；
- duplicate Diagnostic 保存首定义与重复定义 SourceRef；
- 两遍完整扫描后统一判定 FAILED；
- owner 上下文不完整或 token 不匹配时 fail closed；
- SymbolTable 包装 Context ImmutableRegistry，keys/definitions 稳定有序且不可变；
- 任一失败都不发布部分表；
- `ROOT_CONFIG`、`RULE`、`MODEL_ACCESS` 保持 Raw 事实；
- RawReference 不解析、不执行 I/O。

## Revision Integrity

- R27 first commit：`5cb05ddd77d2bdb9c21f25dec6aea36003f78f28`
- R27 blob：`613edfdc133fa68aa12ae3adc31eb8ae23058d9c`
- R23 first commit：`0f1e65e8553b50526133e182780d845e6e9565bf`
- R23 blob：`840989a6119e7e5f99981957614806c2152ea56d`
- R27/R23 在 RED 前创建，clean-code Head 复核 blob 未变化

## PR、恢复与下一步

- 当前 PR：`#22`
- Branch：`feature/p1-t07-symbol-table-20260803-1958`
- Base：`dev_all@3e0492b0319173c87abff6952d4dad0f5507c31c`
- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t07-r01/completion-report.json`
- 机器恢复入口：`project_doc/version/V_1.0/tdd_p1_t07_r01_completion.json`
- `@Override` 独占一行，方法和关键逻辑使用中文注释
- 未修改 Context、Raw、Source Graph、Compiler API、XML/YAML Frontend 生产代码
- 未启动 ReferenceResolver、Information、ModelAccess、Deferred、Pipeline、Digest、Publication 或 T08
- 未经用户明确授权不得合并 PR #22
- PR #22 合并前 `TASK-P1-T08` 保持未启动和阻断

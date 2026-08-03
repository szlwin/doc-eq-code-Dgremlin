# P1-COMPILER-F01 阶段交接

> T01～T05 已合并到 `dev_all`。T05 当前有效 Completion 为 `COMPLETION-P1-T05-R03@30529276cd8f`，其 merge / T06 base 为 `17ce0834b947a75ff3ccbd24c7b1332fb93e8941`。当前有效任务为 `TASK-P1-T06 / I001`。

## 已合并前置任务

- T01：`COMPLETION-P1-T01-R04@ee99223a243f`；
- T02：`COMPLETION-P1-T02-R05@35376308b013`；
- T03：`COMPLETION-P1-T03-R05@91271c9a1c20`；
- T04：`COMPLETION-P1-T04-R02@0699c6bc2ed4`；
- T05：`COMPLETION-P1-T05-R03@30529276cd8f`，merge / dev_all Head `17ce0834b947a75ff3ccbd24c7b1332fb93e8941`。

T05 I001/I002 Completion 及其 Design、Plan、Review、Evidence 均作为不可变历史保留，不能作为 T06 当前输入。

## T06 I001（当前有效）

- Design：`DESIGN-R23@P1-T06-I001`；
- Plan：`TP-P1-COMPILER-F01-R19@P1-T06-I001`；
- TDD：`TDD-P1-T06-R01@8c5efd3dcbee`；
- Architecture Skeleton：`DEVSKEL-P1-T06-R01@6033e59728e7`；
- Development：`DEV-P1-T06-R01@90d483290cf3`；
- Code Review：`CODEREVIEW-P1-T06-R01@90d483290cf3`；
- Testing：`TESTING-P1-T06-R01@90d483290cf3`；
- Completion：`COMPLETION-P1-T06-R01@90d483290cf3`；
- Review：`REV-000257`～`REV-000269`；
- Evidence：`EVD-000499`～`EVD-000511`；
- Clean-code Head：`90d483290cf3943003624f21f19981535ca1408c`；
- P0 Run：`30789608249`；
- Artifact：`8846541706`；
- Artifact SHA-256：`b0cf248154f392bf85a95c8903949efc16bf1a3bb264a2cbef72210df808b51f`；
- T06 16/16；Compiler total 99/99；XML 30/30；YAML 59/59；Context 正常 26/26；Demo 4/4；Legacy 1/1；
- 12 模块 Reactor、Java release 8、故意失败门禁：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`；
- 开放 P0/P1/P2：无。

## RawDefinitionSet 合同

- 新增 `dec.core.compiler.raw`，不含 parser 类型；
- 六类根使用完整父子 Grammar 白名单；
- 14 类定义按输入文档顺序与定义先序生成；
- sourceOrdinal 必须从 0 连续；
- SourceRef、format、schemaVersion、owner/name、attributes、references、body 均保留；
- attributes 稳定排序，references/children 保持文档顺序；
- reference 不解析、不访问外部资源；
- 进入嵌套定义后，父定义停止收集该子树引用；
- Builder 先全量验证，再提取和发布；
- unknown/null/缺 name/owner 全部 fail closed；
- 失败不暴露部分集合；
- 等价 XML/YAML Canonical 除 format 外 Raw 语义一致；
- 集合和 diagnostics defensive copy、不可变。

## Revision Integrity

- R23 first commit：`8a6cadfbb35f82820dd077a44033c6ba179ad77c`；
- R23 blob：`f7eb09ff6291c3f84bb93ace88a6d301d9ef73ad`；
- R19 first commit：`5bab7c508e27762d306d672fd925f2c743fbd245`；
- R19 blob：`e5e65b495ffe01c8265061d975f2264d31b761e7`；
- R23/R19 在 RED 前冻结，clean-code Head 复核不变。

## PR、恢复与下一步

- 当前 PR：`#21`；
- Branch：`feature/p1-t06-raw-definition-20260803-1334`；
- Base：`dev_all@17ce0834b947a75ff3ccbd24c7b1332fb93e8941`；
- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t06-r01/completion-report.json`；
- 机器恢复入口：`project_doc/version/V_1.0/tdd_p1_t06_r01_completion.json`；
- `@Override` 独占一行，公共方法、构造器和关键逻辑使用中文注释；
- 未修改 Context、Source Graph、Canonical API、XML/YAML Frontend 生产代码；
- 未启动 TypedKey、SymbolTable、引用解析、Pipeline 或 T07；
- 未经明确授权不得合并 PR #21；
- PR #21 合并前 `TASK-P1-T07` 保持阻断。

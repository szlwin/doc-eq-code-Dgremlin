# P1-COMPILER-F01 阶段交接

> T01～T05 已合并到 `dev_all`。T06 I001/I002 的 Completion R01/R02 已被后续独立 Review 推翻并作为不可变历史保留。当前有效任务为 `TASK-P1-T06 / I003`，当前有效 Completion 为 `COMPLETION-P1-T06-R03@432ccdc1103f`。

## 已合并前置任务

- T01：`COMPLETION-P1-T01-R04@ee99223a243f`；
- T02：`COMPLETION-P1-T02-R05@35376308b013`；
- T03：`COMPLETION-P1-T03-R05@91271c9a1c20`；
- T04：`COMPLETION-P1-T04-R02@0699c6bc2ed4`；
- T05：`COMPLETION-P1-T05-R03@30529276cd8f`，merge / dev_all Head `17ce0834b947a75ff3ccbd24c7b1332fb93e8941`。

## T06 历史

- I001 Completion：`COMPLETION-P1-T06-R01@90d483290cf3`；
- I002 Completion：`COMPLETION-P1-T06-R02@aec3cd105b15`；
- R01/R02 以及对应 Design、Plan、TDD、Skeleton、Review、Evidence、Testing 和机器 checkpoint 全部不可变保留；
- R01/R02 不能作为 T07 当前前置输入。

## T06 I003（当前有效）

- Design：`DESIGN-R25@P1-T06-REWORK-I003`；
- Plan：`TP-P1-COMPILER-F01-R21@P1-T06-REWORK-I003`；
- TDD：`TDD-P1-T06-R03@ea1701deb923`；
- Architecture Skeleton：`DEVSKEL-P1-T06-R03@35357c213fdc`；
- Development：`DEV-P1-T06-R03@432ccdc1103f`；
- Code Review：`CODEREVIEW-P1-T06-R03@432ccdc1103f`；
- Testing：`TESTING-P1-T06-R03@432ccdc1103f`；
- Completion：`COMPLETION-P1-T06-R03@432ccdc1103f`；
- Review：`REV-000283`～`REV-000295`；
- Evidence：`EVD-000525`～`EVD-000537`；
- Clean-code Head：`432ccdc1103f0119230858e7ae2343529af6c294`；
- P0 Run：`30801214669`；
- Artifact：`8850875201`；
- Artifact SHA-256：`eadc28a2db03ff23405869712aefa84398cf1b9b37f9408b20d348af67d783b7`；
- I003 7/7；T06 Raw 38/38；Compiler 121/121；XML 30/30；YAML 59/59；Context 正常 26/26；Demo 4/4；Legacy 1/1；
- 12 模块 Reactor、Java release 8、故意失败门禁：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`；
- 开放 P0/P1/P2：无。

## I003 关闭的 P1

- public build 只迭代调用方 List 一次；
- snapshot 复制期间拒绝 null，复制完成后判断 empty；
- snapshot 使用不可变容器；
- validate 与 extract 只消费同一 snapshot；
- snapshot 顺序决定 sourceOrdinal；
- snapshot 完成后不再访问原始 List；
- snapshot 读取 RuntimeException 稳定返回 `raw.build.failed`；
- unsupported root/unknown child 不能通过第二次迭代绕过验证；
- 失败不发布成功空集合、非法 body 或部分 RawDefinitionSet。

## 保持的 I001/I002 合同

- 六类根完整父子 Grammar；
- 14 Kind 与 public owner/name matrix；
- owner/name/reference lexical 保留；
- reference 第一阶段验证与精确 SourceRef；
- depth 256 / node count 65,536；
- RawDefinition equals/hashCode/toString 全语义字段；
- 不可变集合与 diagnostics；
- XML/YAML Canonical parity；
- reference 不解析、不执行 I/O。

## Revision Integrity

- R25 first commit：`e77d147314ced40501ccccf659dc58319a30aa58`；
- R25 blob：`6715d7b217e01801f5ff1c26ba67c34c8e65f39d`；
- R21 first commit：`0906c84847cf95951aaeff2591d5a1f04c89c956`；
- R21 blob：`9293a04977f80b3e1ffa7499cdbc1e4081ca9a1c`；
- R25/R21 在 RED 前创建，clean-code Head 复核 blob 未变化。

## PR、恢复与下一步

- 当前 PR：`#21`；
- Branch：`feature/p1-t06-raw-definition-20260803-1334`；
- Base：`dev_all@17ce0834b947a75ff3ccbd24c7b1332fb93e8941`；
- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t06-r03/completion-report.json`；
- 机器恢复入口：`project_doc/version/V_1.0/tdd_p1_t06_r03_completion.json`；
- `@Override` 独占一行，方法和关键逻辑使用中文注释；
- 未修改 Context、Source Graph、Canonical API、XML/YAML Frontend 生产代码；
- 未启动 TypedKey、SymbolTable、引用解析、Pipeline、Publication 或 T07；
- 未经用户明确授权不得合并 PR #21；
- PR #21 合并前 `TASK-P1-T07` 保持阻断。

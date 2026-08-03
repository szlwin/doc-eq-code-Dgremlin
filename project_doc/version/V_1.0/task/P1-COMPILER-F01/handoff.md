# P1-COMPILER-F01 阶段交接

> T01～T05 已合并到 `dev_all`。T06 I001～I003 的 Completion R01～R03 已被后续独立 Review 推翻并作为不可变历史保留。当前有效任务为 `TASK-P1-T06 / I004`，当前有效 Completion 为 `COMPLETION-P1-T06-R04@242db638c61d`。

## 已合并前置任务

- T01：`COMPLETION-P1-T01-R04@ee99223a243f`；
- T02：`COMPLETION-P1-T02-R05@35376308b013`；
- T03：`COMPLETION-P1-T03-R05@91271c9a1c20`；
- T04：`COMPLETION-P1-T04-R02@0699c6bc2ed4`；
- T05：`COMPLETION-P1-T05-R03@30529276cd8f`，merge / dev_all Head `17ce0834b947a75ff3ccbd24c7b1332fb93e8941`。

## T06 历史

- I001 Completion：`COMPLETION-P1-T06-R01@90d483290cf3`；
- I002 Completion：`COMPLETION-P1-T06-R02@aec3cd105b15`；
- I003 Completion：`COMPLETION-P1-T06-R03@432ccdc1103f`；
- R01～R03 及对应 Design、Plan、TDD、Skeleton、Review、Evidence、Testing 和机器 checkpoint 全部不可变保留；
- R01～R03 不能作为 T07 当前前置输入。

## T06 I004（当前有效）

- Design：`DESIGN-R26@P1-T06-REWORK-I004`；
- Plan：`TP-P1-COMPILER-F01-R22@P1-T06-REWORK-I004`；
- TDD：`TDD-P1-T06-R04@e2e41dac48fe`；
- Architecture Skeleton：`DEVSKEL-P1-T06-R04@2d78c2290498`；
- Development：`DEV-P1-T06-R04@242db638c61d`；
- Code Review：`CODEREVIEW-P1-T06-R04@242db638c61d`；
- Testing：`TESTING-P1-T06-R04@242db638c61d`；
- Completion：`COMPLETION-P1-T06-R04@242db638c61d`；
- Review：`REV-000296`～`REV-000308`；
- Evidence：`EVD-000538`～`EVD-000550`；
- Clean-code Head：`242db638c61d58eb70e452c1ac08668b6d738b0a`；
- P0 Run：`30810370900`；
- Artifact：`8854512655`；
- Artifact SHA-256：`4472e3cd084eadc18e6b47af19738f9f227834d0a5217caac29b0529ee1aeb33`；
- I004 8/8；I003 7/7；T06 Raw 46/46；Compiler 129/129；XML 30/30；YAML 59/59；Context 正常 26/26；Demo 4/4；Legacy 1/1；
- 12 模块 Reactor、Java release 8、故意失败门禁：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`；
- 开放 P0/P1/P2：无。

## I004 关闭的 P1

- snapshot 在复制文档引用时使用现有 `maxCanonicalNodeCount` 作为硬上限；
- 第 N+1 个文档在 `snapshot.add` 前失败；
- Diagnostic SourceRef 精确指向当前超限文档；
- 达到限制后不再请求 iterator 的下一项；
- snapshot 资源失败不发布部分集合；
- 原始 List 只创建一次 iterator，且不调用随机访问、批量转换、Stream 或 Spliterator；
- 后续 `ValidationBudget` 保持不变，继续检查全部根和后代节点；
- 不捕获 `OutOfMemoryError`，不使用真实 OOM 或无限循环测试。

## 保持的 I001～I003 合同

- 六类根完整父子 Grammar；
- 14 Kind 与 public owner/name matrix；
- owner/name/reference lexical 保留；
- reference 第一阶段验证与精确 SourceRef；
- depth 256 / node count 65,536；
- 调用方 List 单次迭代与不可变 snapshot；
- validate/extract/ordinal 使用同一 snapshot；
- RawDefinition equals/hashCode/toString 全语义字段；
- 不可变集合、diagnostics 和 XML/YAML Canonical parity；
- reference 不解析、不执行 I/O。

## Revision Integrity

- R26 first commit：`06ca3ed8241f2f2cd337c852504e5c9912a86b1e`；
- R26 blob：`d970a04534306fd6e02e0c26ec43947a8375bb61`；
- R22 first commit：`4dfe138fe613837bf4fc7797ef47d9242f733762`；
- R22 blob：`5028129d2b43dd0ae9ed2679240a656ec5ab4a92`；
- R26/R22 在 RED 前创建，clean-code Head 复核 blob 未变化。

## PR、恢复与下一步

- 当前 PR：`#21`；
- Branch：`feature/p1-t06-raw-definition-20260803-1334`；
- Base：`dev_all@17ce0834b947a75ff3ccbd24c7b1332fb93e8941`；
- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t06-r04/completion-report.json`；
- 机器恢复入口：`project_doc/version/V_1.0/tdd_p1_t06_r04_completion.json`；
- `@Override` 独占一行，方法和关键逻辑使用中文注释；
- 临时 Workflow 已删除且未作为证据；
- 未修改 RawBuilderLimits 生产值、Context、Source Graph、Canonical API、XML/YAML Frontend 生产代码；
- 未启动 TypedKey、SymbolTable、引用解析、Pipeline、Publication 或 T07；
- 未经用户明确授权不得合并 PR #21；
- PR #21 合并前 `TASK-P1-T07` 保持阻断。

# TASK-P1-T06 / I004 — Snapshot Budget Rework

- 状态：`COMPLETED`
- PR：`#21`
- Branch：`feature/p1-t06-raw-definition-20260803-1334`
- Rework Base：`36b223e0f50fe090031b499366eb6ff5844b05d3`
- Dependency：`COMPLETION-P1-T05-R03@30529276cd8f`
- Historical Completion：`COMPLETION-P1-T06-R01@90d483290cf3`、`COMPLETION-P1-T06-R02@aec3cd105b15`、`COMPLETION-P1-T06-R03@432ccdc1103f`
- Superseding Review：`REV-000296`
- Finding：`FND-P1-T06-I004-001` — `CLOSED`
- Design：`DESIGN-R26@P1-T06-REWORK-I004`
- Plan：`TP-P1-COMPILER-F01-R22@P1-T06-REWORK-I004`
- TDD：`TDD-P1-T06-R04@e2e41dac48fe`
- Architecture Skeleton：`DEVSKEL-P1-T06-R04@2d78c2290498`
- Development：`DEV-P1-T06-R04@242db638c61d`
- Code Review：`CODEREVIEW-P1-T06-R04@242db638c61d`
- Testing：`TESTING-P1-T06-R04@242db638c61d`
- Completion：`COMPLETION-P1-T06-R04@242db638c61d`
- Review：`REV-000296`～`REV-000308`
- Evidence：`EVD-000538`～`EVD-000550`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 完成结果

Snapshot 在添加每个文档引用前执行 `maxCanonicalNodeCount` 硬上限。小预算 N 下，第 N+1 个非 null 文档以 `raw.limit.node-count` 和当前文档 SourceRef 受控失败，并立即停止 iterator；不会继续读取下一项，也不会发布部分 RawDefinitionSet。

前置文档数限制只保护 snapshot 容器分配。后续 `ValidationBudget` 保持不变，继续遍历所有根与后代节点并执行完整树深度/节点预算。

## TDD 与测试

- RED Head：`e2e41dac48fe3ccef948efb333443de81d3466ca`；
- RED P0：Run `30809689151`，I004 8 run / 5 expected failures / 0 errors / 3 pass；
- GREEN Head：`242db638c61d58eb70e452c1ac08668b6d738b0a`；
- GREEN P0：Run `30810370900` — SUCCESS；
- Artifact：`8854512655`；
- SHA-256：`4472e3cd084eadc18e6b47af19738f9f227834d0a5217caac29b0529ee1aeb33`；
- I004 8/8；I003 7/7；T06 Raw 46/46；Compiler 129/129；XML 30/30；YAML 59/59；Context 正常 26/26；Demo 4/4；Legacy 1/1；
- 12 模块 Reactor、Java release 8、故意失败门禁：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## 保持的合同

- `FND-P1-T06-I003-001` 保持 CLOSED；
- I002 的 2 个 P1 与 3 个 P2 保持 CLOSED；
- 六类根 Grammar、14 Kind、lexical、reference、完整树 depth/node budget、toString、不可变集合、XML/YAML parity 无回退；
- 原始 List 只创建一次 iterator，不调用随机访问、批量转换、Stream 或 Spliterator 入口；
- 临时 Workflow 已删除且未作为证据；
- R01～R03 与全部历史 Revision、Review、Evidence、Completion、机器 checkpoint 未覆盖或删除。

PR #21 保持 Open、未合并；TASK-P1-T07 未启动且继续阻断。

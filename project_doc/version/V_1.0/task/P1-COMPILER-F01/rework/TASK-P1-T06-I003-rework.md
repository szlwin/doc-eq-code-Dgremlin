# TASK-P1-T06 / I003 — Input Snapshot Rework

- 状态：`IN_PROGRESS`
- PR：`#21`
- Branch：`feature/p1-t06-raw-definition-20260803-1334`
- Rework Base：`3884f331dd066da1ff556f9b0544716d7ca3502c`
- Dependency：`COMPLETION-P1-T05-R03@30529276cd8f`
- Historical Completion：`COMPLETION-P1-T06-R01@90d483290cf3`
- Superseded Completion：`COMPLETION-P1-T06-R02@aec3cd105b15`
- Superseding Review：`REV-000283`
- Finding：`FND-P1-T06-I003-001`
- Design：`DESIGN-R25@P1-T06-REWORK-I003`
- Plan：`TP-P1-COMPILER-F01-R21@P1-T06-REWORK-I003`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## Rework 范围

本 iteration 只关闭 `RawDefinitionBuilder.build(List)` 未冻结输入批次，导致 validate 与 extract 可能消费不同文档的 P1。

必须保留：

- I001、I002 的全部 Design、Plan、TDD、Skeleton、Development、Review、Evidence、Testing、Completion 和机器 checkpoint；
- I002 已关闭的 2 个 P1 与 3 个 P2；
- 六类根 Grammar、14 Kind、lexical、reference、depth/node budget、toString 和不可变集合合同。

不得启动：TypedKey、SymbolTable、引用解析、Deferred、Pipeline、Digest、Publication、TASK-P1-T07。

## 当前 Gate

- Design R25：PASSED；
- Plan R21：PASSED；
- 开放 P0：0；
- 开放 P1：1；
- 下一阶段：TDD RED；
- PR #21：不得合并。

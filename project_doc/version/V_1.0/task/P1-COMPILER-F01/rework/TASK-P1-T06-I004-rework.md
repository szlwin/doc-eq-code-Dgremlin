# TASK-P1-T06 / I004 — Snapshot Budget Rework

- 状态：`IN_PROGRESS`
- PR：`#21`
- Branch：`feature/p1-t06-raw-definition-20260803-1334`
- Rework Base：`36b223e0f50fe090031b499366eb6ff5844b05d3`
- Dependency：`COMPLETION-P1-T05-R03@30529276cd8f`
- Historical Completion：`COMPLETION-P1-T06-R01@90d483290cf3`、`COMPLETION-P1-T06-R02@aec3cd105b15`
- Superseded Completion：`COMPLETION-P1-T06-R03@432ccdc1103f`
- Superseding Review：`REV-000296`
- Finding：`FND-P1-T06-I004-001`
- Design：`DESIGN-R26@P1-T06-REWORK-I004`
- Plan：`TP-P1-COMPILER-F01-R22@P1-T06-REWORK-I004`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## Rework 范围

本 iteration 只关闭 snapshot 在完整树节点预算前无上限复制文档引用的 P1。前置预算利用“每个文档至少有一个根节点”建立硬上限，后续 `ValidationBudget` 继续检查所有后代节点。

必须保留：

- I001～I003 的全部 Design、Plan、TDD、Skeleton、Development、Review、Evidence、Testing、Completion 和机器 checkpoint；
- `FND-P1-T06-I003-001` CLOSED；
- I002 的 2 个 P1 与 3 个 P2 CLOSED；
- 六类根 Grammar、14 Kind、lexical、reference、depth/node budget、toString、不可变集合和 XML/YAML parity。

不得启动：TypedKey、SymbolTable、引用解析、Deferred、Pipeline、Digest、Publication、TASK-P1-T07。

## 当前 Gate

- Design R26：PASSED；
- Plan R22：PASSED；
- 开放 P0：0；
- 开放 P1：1；
- 开放 P2：0；
- 下一阶段：TDD RED；
- PR #21：不得合并。

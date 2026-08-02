# TASK-P1-T03 REWORK I002

- Task：`TASK-P1-T03`
- Iteration：`I002`
- 状态：`IN_PROGRESS`
- 分支：`feature/p1-t03-source-graph-20260802-1430`
- Rework base：`335cc7ae2843145ae891a22892a169e74ac5d6fc`
- 原 Completion：`COMPLETION-P1-T03-R01@713848bfa65e`（被独立 Review 推翻，历史保留）
- Design：`DESIGN-R14@P1-T03-REWORK-I002`
- Plan：`TP-P1-COMPILER-F01-R10@P1-T03-REWORK-I002`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## Findings

- `FND-P1-T03-I002-001` / P1：缺少统一 canonical reference key。
- `FND-P1-T03-I002-002` / P1：cycle stack 混用 sourceId 与 reference。
- `FND-P1-T03-I002-003` / P2：声明列号不是 start tag `<` 的起始列。

## 门禁

1. 新 Oracle 必须先形成有效 RED；
2. 既有 Context 26 项与 Compiler 62 项不得回归；
3. canonicalization 不得消除 `..` 后再验证；
4. Provider、edge、duplicate key、ancestor stack 和 graph equality 使用同一 canonical reference；
5. sourceId 与 reference 身份域明确分离；
6. cycle 必须在递归 Provider 调用前返回 `MIX-SOURCE-POLICY`；
7. 7 条声明边必须精确指向 `<` 的行、列和 nodePath；
8. `@Override` 独占一行，方法和重要逻辑使用中文注释；
9. Java 8、12 模块 Reactor 和失败阻断门禁必须通过；
10. 开放 P0/P1 阻断 Completion；
11. PR #18 未经授权不得合并；
12. T04 保持未启动。

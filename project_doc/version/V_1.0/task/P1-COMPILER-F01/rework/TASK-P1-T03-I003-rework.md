# TASK-P1-T03 REWORK I003

- Task：`TASK-P1-T03`
- Iteration：`I003`
- 状态：`IN_PROGRESS`
- 分支：`feature/p1-t03-source-graph-20260802-1430`
- Rework base：`4f218f5dbf329949b8f7b3d7396668919482d198`
- 原 Completion：`COMPLETION-P1-T03-R02@6af43b47f044`（被独立 Review 推翻，历史保留）
- Design：`DESIGN-R15@P1-T03-REWORK-I003`
- Plan：`TP-P1-COMPILER-F01-R11@P1-T03-REWORK-I003`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## Findings

- `FND-P1-T03-I003-001` / P1：仅点段引用 canonicalize 为空，破坏非空不变量并泄漏未分类异常。
- `FND-P1-T03-I003-002` / P1：编码单点段 `%2e` 未进入统一 canonical key。
- `FND-P1-T03-I003-003` / P2：CRLF/CR 声明位置缺少直接 Oracle。

## 门禁

1. 新 Oracle 必须先形成有效 RED；
2. 既有 Context 26 项与 Compiler 68 项不得回归；
3. `.`、`./`、`./.` canonical key 必须非空且统一为 `.`；
4. 非绝对根必须稳定返回 `MIX-SOURCE-PATH-ESCAPE`、空 graph、Provider access 0；
5. `%2e` 单点段必须与字面量 `.`、无点段统一；
6. `%2e%2e`、`.%2e`、`%2e.` 等父目录证据必须保留；
7. `%2F` 等编码分隔符不得改变 segment 边界；
8. opaque 与 hierarchical URI 均覆盖；
9. CRLF、CR 下的 7 条边 line、column、nodePath 必须直接验证；
10. `@Override` 独占一行，方法和重要逻辑使用中文注释；
11. Java 8、12 模块 Reactor 和失败阻断门禁必须通过；
12. 开放 P0/P1 阻断 Completion；
13. PR #18 未经授权不得合并；
14. T04 保持未启动。

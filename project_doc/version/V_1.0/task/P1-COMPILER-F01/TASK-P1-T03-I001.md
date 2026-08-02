# TASK-P1-T03 I001

- 任务：`TASK-P1-T03`
- Iteration：`I001`
- 状态：`IN_PROGRESS`
- 分支：`feature/p1-t03-source-graph-20260802-1430`
- 基线：`dev_all@370b72f4bf4ec9b3620586f26d13d95f611f3cc9`
- 前置 Completion：`COMPLETION-P1-T02-R05@35376308b013`
- Design：`DESIGN-R13@P1-T03-I001`
- Plan：`TP-P1-COMPILER-F01-R09@P1-T03-I001`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 目标

实现安全源发现与精确 SourceGraph，使固定 mix 入口得到精确 10 个 Source 和 7 条声明边，并在任何路径逃逸、未知 scheme、缺失源、重复 sourceId 或资源上限失败时返回稳定 Diagnostic，不暴露部分图。

## 门禁

1. 新 TDD Oracle 必须以 Java 8 成功编译；
2. RED 不得来自 ClassNotFound、NoSuchMethod、语法、依赖或 fixture 缺失；
3. Provider 调用前拒绝路径逃逸和未知 scheme；
4. 目录展开 Source 不得伪造声明边；
5. 相同 sourceId 不得覆盖或静默去重；
6. Source 与 edge 输出必须确定性排序；
7. 不修改 `dec-core-context` 生产代码；
8. 不实现 T04 Canonical Frontend；
9. 所有 `@Override` 独占一行；
10. 方法、构造器和重要逻辑使用中文注释；
11. 开放 P0/P1 阻断 Completion；
12. 新 PR 未经明确授权不得合并。

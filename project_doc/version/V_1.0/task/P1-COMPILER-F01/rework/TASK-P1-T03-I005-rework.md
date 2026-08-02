# TASK-P1-T03 REWORK I005

- Task：`TASK-P1-T03`
- Iteration：`I005`
- 状态：`IN_PROGRESS`
- 分支：`feature/p1-t03-source-graph-20260802-1430`
- Rework base：`d41b4553189b4b9e80a7ca92c4acd34e4fc97e42`
- 原 Completion：`COMPLETION-P1-T03-R04@04bfb86c9bf1`（被独立 Review 推翻，历史保留）
- Design：`DESIGN-R17@P1-T03-REWORK-I005`
- Plan：`TP-P1-COMPILER-F01-R13@P1-T03-REWORK-I005`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## Finding

- `FND-P1-T03-I005-001` / P1：声明解析器仅按路径末两级识别，错误 root、错误嵌套和 ignored subtree 中的同名元素可生成真实 Source 边。

## 门禁

1. 新 Oracle 必须先形成有效 RED；
2. root 仅接受四条完整声明路径；
3. systems 仅接受 `/systems/system/rule-file-info/rule-file`；
4. 文档根必须分别为 `orm-config` 和 `systems`；
5. 其它路径中的同名元素必须忽略；
6. 错误 root/wrong nesting 只允许 root Provider 访问一次；
7. 错误 systems 结构不得访问 rule 或 business Provider；
8. 任何失败不发布部分 graph；
9. 合法 fixture 10 Source / 7 Edge / 8 Provider 调用保持；
10. Context 与既有 Compiler 测试不得回归；
11. `@Override` 独占一行，重要逻辑使用中文注释；
12. Java 8、12 模块 Reactor、失败阻断通过；
13. 开放 P0/P1 阻断 Completion；
14. PR #18 未经明确授权不得合并；
15. TASK-P1-T04 保持未启动。

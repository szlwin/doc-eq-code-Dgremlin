# TASK-P1-T03 REWORK I005

- Task：`TASK-P1-T03`
- Iteration：`I005`
- 状态：`COMPLETED`
- 分支：`feature/p1-t03-source-graph-20260802-1430`
- Rework base：`d41b4553189b4b9e80a7ca92c4acd34e4fc97e42`
- 原 Completion：`COMPLETION-P1-T03-R04@04bfb86c9bf1`（被独立 Review 推翻，历史保留）
- 当前 Completion：`COMPLETION-P1-T03-R05@91271c9a1c20`
- Design：`DESIGN-R17@P1-T03-REWORK-I005`
- Plan：`TP-P1-COMPILER-F01-R13@P1-T03-REWORK-I005`
- TDD：`TDD-P1-T03-R05@06bc2a0c0ebd`
- Architecture Skeleton：`DEVSKEL-P1-T03-R05@1d49bb2f1fa3`
- Development：`DEV-P1-T03-R05@91271c9a1c20`
- Code Review：`CODEREVIEW-P1-T03-R05@91271c9a1c20`
- Testing：`TESTING-P1-T03-R05@91271c9a1c20`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## Finding

- `FND-P1-T03-I005-001` / P1 / `CLOSED`：声明解析器已从路径后缀匹配改为完整元素栈精确匹配，并验证 `orm-config`、`systems` 根元素。

## 完成事实

1. root 只接受四条冻结完整声明路径；
2. systems 只接受 `/systems/system/rule-file-info/rule-file`；
3. 其它路径中的同名元素全部忽略；
4. wrong-root 与 wrong-nesting 均返回 `MIX_SOURCE_POLICY`，仅访问 root Provider 一次；
5. wrong-systems-root 与 wrong-system-path 均返回 `MIX_SOURCE_POLICY`，不访问 rule 或 business Provider；
6. 合法声明旁的 ignored subtree 不改变 10 Source / 7 Edge / 8 Provider 调用；
7. 发布的七条 Edge `nodePath` 只能来自五条冻结路径；
8. 任何失败不发布部分 graph；
9. Context 26/26、Compiler 83/83、I005 5/5 通过；
10. Java 8、12 模块 Reactor、失败阻断通过；
11. MySQL 为 `SKIPPED_NOT_APPLICABLE`；
12. 开放 P0/P1 为 0；
13. `@Override` 独占一行，方法和重要逻辑使用中文注释；
14. 未修改 Context 生产代码，未实现 T04；
15. PR #18 未经明确授权不得合并，T04 继续阻断。

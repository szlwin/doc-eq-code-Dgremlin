# TASK-P1-T01 REWORK I008

- 任务：`TASK-P1-T01`
- Iteration：`I008`
- 状态：`REWORK`
- 原完成 Revision：`COMPLETION-P1-T01-R01@7be02cd9af4c`
- 追溯代码 Head：`9aa41beba5c89bfd5f47f56d2ebc1c669de5e357`
- 追溯 PR：`#14`，已合并到 `dev_all`
- 新设计：`DESIGN-R06@P1-T01-REWORK-I008`
- 新实施计划：`TP-P1-COMPILER-F01-R02@P1-T01-REWORK-I008`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 重开原因

追溯 Review 发现 5 个开放 P1 Finding：

1. DirectoryKey 缺少 BusinessScope owner；
2. CompiledModelSet 缺少 SourceManifest 发布视图和 Typed Registry；
3. ERROR model 与不同源 Projection 可构造；
4. Registry Key 与 Definition 身份可错配；
5. 测试 Oracle 未冻结上述设计语义。

原 Completion、Review 和 Evidence 作为历史保留，不删除、不覆盖；本 iteration 使用全新的 Revision、Review、Evidence 和 Completion。

## 依赖门禁

- PR #15（TASK-P1-T02）在本 REWORK 合并前不得合并；
- TASK-P1-T03 不得启动；
- 本分支只修复 T01 公共合同，不实现后续 Compiler 行为。

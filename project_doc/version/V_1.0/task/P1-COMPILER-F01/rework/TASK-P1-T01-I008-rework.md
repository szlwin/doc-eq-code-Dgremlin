# TASK-P1-T01 REWORK I008

- 任务：`TASK-P1-T01`
- Iteration：`I008`
- 状态：`COMPLETED`
- 原完成 Revision：`COMPLETION-P1-T01-R01@7be02cd9af4c`
- 追溯代码 Head：`9aa41beba5c89bfd5f47f56d2ebc1c669de5e357`
- 追溯 PR：`#14`，已合并到 `dev_all`
- 新设计：`DESIGN-R06@P1-T01-REWORK-I008`
- 新实施计划：`TP-P1-COMPILER-F01-R02@P1-T01-REWORK-I008`
- Completion Revision：`COMPLETION-P1-T01-R02@a0daaf94f74b`
- 干净代码 Head：`a0daaf94f74b38186bc1e80ecc00903744bac0b4`
- 干净代码 P0 Run：`30705625463`，结果 `PASSED`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`

## 重开原因

追溯 Review 发现 5 个 P1 Finding：

1. DirectoryKey 缺少 BusinessScope owner；
2. CompiledModelSet 缺少 SourceManifest 发布视图和 Typed Registry；
3. ERROR model 与不同源 Projection 可构造；
4. Registry Key 与 Definition 身份可错配；
5. 测试 Oracle 未冻结上述设计语义。

原 Completion、Review 和 Evidence 作为历史保留，没有删除或覆盖。本 iteration 使用新的 Design、Plan、TDD、Review、Evidence 和 Completion。

## 完成结果

- 5 个追溯 P1 Finding 全部 CLOSED；
- `DirectoryKey` 使用 `BusinessScopeKey + name` 完整身份；
- `CompiledModelSet` 冻结中立 SourceManifest、Typed Registry、Deferred、Diagnostic、Digest 和版本闭包；
- ERROR Diagnostic、Definition 身份错配、Deferred 完整身份错配均在构造边界拒绝；
- `CoreConfigProjection` 只能由同一个 `CompiledModelSet` 派生；
- Context 合同测试 17 项全绿，其中 REWORK 语义测试 8 项全绿；
- 完整 11 模块 Maven Reactor 与故意失败门禁均通过；
- Java 8 编译通过；
- 新增和修改方法、重要逻辑均使用中文注释；
- 所有新增和修改的 `@Override` 注解均独占一行；
- 开放 P0/P1 Finding：0。

## 后续门禁

- PR #16 合并 `dev_all` 后，PR #15（TASK-P1-T02）必须 rebase 并适配新的 T01 公共合同，再重新执行 Review、Testing 和 Completion Verification；
- TASK-P1-T03 在 T02 重验证完成前不得启动；
- 本分支只完成 T01 公共合同返工，没有实现后续 Compiler 行为。

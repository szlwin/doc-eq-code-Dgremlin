# TASK-P1-T02 REWORK I002

- 任务：`TASK-P1-T02`
- Iteration：`I002`
- 状态：`COMPLETED`
- 结果：`PASSED`
- 最新基线：`dev_all@f88f45731e16868bfacb489b63e3086aae49d018`
- 被替代 Completion：`COMPLETION-P1-T02-R01@643b44a8b72a`
- 当前 Completion：`COMPLETION-P1-T02-R02@8847b3c7dfac`
- 设计：`DESIGN-R09@P1-T02-REWORK-I002`
- 实施计划：`TP-P1-COMPILER-F01-R05@P1-T02-REWORK-I002`
- 执行模式：`SEQUENTIAL / auto / architecture_review / git_checkpoint`
- 分支：`feature/p1-t02-rework-i002-20260802-1116`
- 当前 PR：`#17`
- 被替代 PR：`#15`

## 重开原因

PR #16 已将 T01 最终发布聚合合同合并到 `dev_all`。旧 PR #15 基于 T01 初始版本，无法直接合并；旧测试调用已经失效的 `CompiledModelSet` 构造签名，且旧 `PublishedCompilationResult` 通过 `equals(...)` 接受值相等但并非同一实例的模型与 Context，允许调用方重新拼接发布事实。

旧 R01 Completion、Review、Evidence 和 PR #15 均作为历史保留，没有删除或覆盖。本轮使用新的 Revision、Review 与 Evidence 完成重新验证。

## 完成结果

- TDD：`TDD-P1-T02-R02@33a00d364088`，P0 Run `30730604783` 形成有效 RED；
- Architecture Skeleton：`DEVSKEL-P1-T02-R02@881facd9fad2`，P0 Run `30730643136` 保持一项受控 RED；
- Development：`DEV-P1-T02-R02@8847b3c7dfac`；
- Code Review：`CODEREVIEW-P1-T02-R02@8847b3c7dfac`；
- Testing：`TESTING-P1-T02-R02@8847b3c7dfac`；
- Completion：`COMPLETION-P1-T02-R02@8847b3c7dfac`；
- 最终代码 P0 Run：`30730762775`；
- Context 测试：26 run / 0 failures / 0 errors / 0 skipped；
- Compiler 测试：12 run / 0 failures / 0 errors / 0 skipped；
- 完整 12 模块 Reactor：PASSED；
- Java release 8：PASSED；
- 故意失败测试阻断门禁：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## 已关闭 Finding

- `FND-P1-T02-I002-001`：值相等但不同实例的模型可与 Context 组合，P1，CLOSED；
- `FND-P1-T02-I002-002`：成功结果可携带模型之外的 diagnostics，P1，CLOSED；
- `FND-P1-T02-I002-003`：成功结果复制而非复用模型 diagnostics 实例，P2，CLOSED。

开放 P0/P1 Finding：无。

## 最终合同

1. `ModelCompiler.compileAndPublish(...)` 是唯一公共 Compiler 入口；
2. `PublishedCompilationResult` 要求 `compiledModelSet == context.compiledModelSet()`；
3. 成功结果 diagnostics 必须与模型 diagnostics 一致，并复用同一不可变实例；
4. 失败结果对输入 diagnostics 执行防御性复制，且不暴露候选模型、Context 或 Digest；
5. `dec-core-compiler` 只依赖 `dec-core-context`；
6. 未修改 T01 生产代码，未启动 T03 SourceGraph、Frontend 或 Pipeline；
7. 所有新增和修改的 `@Override` 注解独占一行，公共方法和重要逻辑使用中文注释。

## Review 与 Evidence

- Review：`REV-000112`～`REV-000120`，全部 PASSED；
- Evidence：`EVD-000361`～`EVD-000366`，全部 ACTIVE；
- 机器恢复入口：`project_doc/version/V_1.0/tdd_p1_t02_r02_completion.json`。

## 后续门禁

PR #17 合并到 `dev_all` 前不得启动 `TASK-P1-T03`。本 iteration 不声明或实现任何 T03 行为。

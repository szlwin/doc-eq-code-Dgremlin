# P1-COMPILER-F01 恢复上下文

- 当前逻辑任务：`TASK-P1-T15 / I001` 已完成
- 当前有效 Completion：`COMPLETION-P1-T15-R01@f36b03e6243`
- 状态：`COMPLETED / PASSED`
- Base：`dev_all@665dd364975505bb01263885a25b3bb1be767d2b`
- Dependency：`COMPLETION-P1-T14-R03@37fb814b39c5`
- Branch：`feature/p1-t15-retire-declaration-20260806-1354`
- PR：`#30 / OPEN / READY_FOR_REVIEW / NOT_MERGED`
- Design：`DESIGN-R51@P1-T15-I001`
- Plan：`TP-P1-COMPILER-F01-R47@P1-T15-I001`
- TDD：`TDD-P1-T15-R01@bff67b86fb55`
- Architecture：`DEVSKEL-P1-T15-R01@bff67b86fb55`
- Development：`DEV-P1-T15-R01@f36b03e6243`
- Code Review：`CODEREVIEW-P1-T15-R01@f36b03e6243`
- Testing：`TESTING-P1-T15-R01@f36b03e6243`
- Open P0/P1/P2：`0 / 0 / 0`

## Current contract

- `EngineContext → CoreConfigProjection` 是旧核心唯一只读事实源；
- `CompilerStarter` 只持有实例级 `ModelCompiler`；
- 编译发布精确委托一次，输入与结果不复制、不改写；
- Projection 只从 `PublishedCompilationResult.engineContext()` 获取；
- Starter 不保存全局 current Context，不拥有额外 Publisher/CAS；
- `dec-expand-declaration` 已从 Git、POM、Reactor、依赖图、源码和 Artifact 整体退役；
- Demo 不再包含旧 declaration/config 示例；
- 无 Adapter、反射生产逻辑、ServiceLoader 回流或双轨 runtime。

## Review repair

- Finding：`FND-P1-T15-I001-001 / P2 / SPEC-CORRECTNESS-ORACLE / CLOSED`；
- 原 3 项测试只覆盖结构与类路径；
- 新增 4 项独立行为测试，覆盖精确一次委托、参数/结果 identity、null 前置拒绝、Projection identity 和非发布拒绝；
- 修复只修改测试，不修改生产实现。

## Validation

- Code/Test Revision：`f36b03e6243f6e3c9d2f5b2ffce7cf4b1fd63eb3`
- Run / Artifact：`31083267905 / 8960370768`
- SHA-256：`ea2c919cbacfead831a5d137894991b09b7a2163f0616c9bc47f99505db517b3`
- Surefire XML：110；All：633；Normal passed：632；intentional failure：1；Errors/Skipped：0/0
- T15：10/10；Starter：10/10；Compiler：504/504；XML：30/30；YAML：59/59；Demo：3/3
- retirement baseline / mutation blocked / restored baseline：`PASSED / YES / PASSED`
- Java 8、T14 mutation、T15 retirement、intentional failure：PASSED
- MySQL：`SKIPPED_NOT_APPLICABLE`

## Revision integrity

- R51 first commit/blob：`4e47d3a4b57f40ee2da6c9fcd4ba30e572bbd9b2` / `3a11a6f8f8110ab0c187d07a3a88bf4c442c0516`
- R47 first commit/blob：`c5d0537f95f0d0b7c95be2d6e9bbff0151a643b4` / `051e41e77d3f5c40a8248e3de1bb94c65e71ed8d`
- Code/Test Revision 后只允许 `project_doc` 与 PR 元数据更新；
- 新增 `@Override` 独占一行；方法与重要逻辑使用中文注释。

## Recovery

- Completion：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/commands/completion-p1-t15-r01/completion-report.json`
- Review：`project_doc/version/V_1.0/task/P1-COMPILER-F01/review/review-p1-t15-r01.md`
- TDD：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/tdd-p1-t15-r01.md`
- Testing：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/testing-p1-t15-r01.md`
- Revision Lock：`project_doc/version/V_1.0/task/P1-COMPILER-F01/evidence/revision-lock-p1-t15-r01.md`

仅在用户明确授权后合并 PR #30。

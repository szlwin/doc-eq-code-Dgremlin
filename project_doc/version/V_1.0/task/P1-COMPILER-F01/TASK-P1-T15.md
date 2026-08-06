# TASK-P1-T15 — 旧核心只读投影与 Declaration Runtime 整体退役

- Current Iteration：`I001`
- Status：`COMPLETED / PASSED`
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
- Completion：`COMPLETION-P1-T15-R01@f36b03e6243`
- Open P0/P1/P2：`0 / 0 / 0`

## Delivered contract

- 保持 `EngineContext → CoreConfigProjection` 单一事实源，不复制、不缓存兼容投影；
- 新增实例级 `CompilerStarter`，只依赖 `ModelCompiler`；
- `compileAndPublish` 精确委托一次并原样返回结果；
- `projection` 只接受已发布结果，返回同一个 EngineContext 的同一个 Projection；
- 删除 Starter 全局 Config 写入口和旧 XML/YAML Parser 运行时依赖；
- 从根 POM、默认 Reactor、依赖管理和 Git 树整体删除 `dec-expand-declaration`；
- 删除 Demo 中依赖旧 declaration/config 的源码、资源和测试；
- 未保留 Adapter、复制实现、ServiceLoader 回流或运行时双轨；
- 建立 POM、源码、反射字符串、服务、依赖树与发布 Artifact 残留门禁；
- mutation proof 可检测旧 module 与旧 package/source 回流，清理后恢复 GREEN。

## TDD and Review

- Valid RED：Run `31077241009`，3 tests / 3 expected assertion failures / 0 errors；
- Review Finding `FND-P1-T15-I001-001`：P2 SPEC/CORRECTNESS/ORACLE，已关闭；
- 新增 4 项独立行为测试，验证委托次数、参数/结果 identity、Projection identity、空参数前置拒绝和非发布结果拒绝；
- T15 最终共 10/10，通过结构、行为和独立 Review 三组测试。

## Validation

- Code/Test Revision：`f36b03e6243f6e3c9d2f5b2ffce7cf4b1fd63eb3`；
- P0 Run：`31083267905` — SUCCESS；
- Artifact/SHA：`8960370768` / `ea2c919cbacfead831a5d137894991b09b7a2163f0616c9bc47f99505db517b3`；
- Surefire XML：110；All：633；Normal passed：632；intentional failure：1；Errors/Skipped：0/0；
- T15：10/10；Starter：10/10；Compiler：504/504；XML：30/30；YAML：59/59；Demo：3/3；
- retirement baseline：PASSED；mutation：按预期阻断；restore：PASSED；
- Java release 8、T14 mutation gate、T15 retirement gate、intentional failure gate：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## Revision integrity and style

- R51 first commit/blob：`4e47d3a4b57f40ee2da6c9fcd4ba30e572bbd9b2` / `3a11a6f8f8110ab0c187d07a3a88bf4c442c0516`；
- R47 first commit/blob：`c5d0537f95f0d0b7c95be2d6e9bbff0151a643b4` / `051e41e77d3f5c40a8248e3de1bb94c65e71ed8d`；
- Code/Test Revision 后只允许 `project_doc` 与 PR 元数据更新；
- 所有新增 `@Override` 独占一行；
- 类、方法、委托、Projection、扫描、mutation 和重要测试逻辑使用中文注释。

PR #30 未执行合并；未经用户明确授权不得合并。

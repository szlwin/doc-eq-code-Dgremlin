# TDD-P1-T15-R01 — Valid Behavior RED

- TDD ID：`TDD-P1-T15-R01@bff67b86fb55`
- Iteration：`TASK-P1-T15 / I001`
- Design：`DESIGN-R51@P1-T15-I001`
- Plan：`TP-P1-COMPILER-F01-R47@P1-T15-I001`
- Head：`bff67b86fb5549a2397f61c42905440f8c4ff052`
- P0 Run：`31077241009`
- Result：`FAILURE / EXPECTED_BEHAVIOR_RED`
- Artifact：`8958005105`
- Artifact SHA-256：`3e3e9572ff3fd6777fc2fd91ed148f1b7f85bf28625dd4d7bee63ffef6ce7ec8`

## RED integrity

- Starter production compile：PASSED；
- Starter testCompile：PASSED；
- JUnit 实际执行：3；
- Failures：3；
- Errors：0；
- Skipped：0；
- Failure type：`org.opentest4j.AssertionFailedError`；
- 无 `COMPILATION ERROR`、ClassNotFound 环境错误或测试未执行。

精确失败：

1. `compilerStarterUsesOnlyInstanceCompilerBoundary`：新 `CompilerStarter` 尚不存在；
2. `legacyGlobalStarterEntryPointsAreRetired`：`ConfigUtil` 与 `DataSourceManager` 仍在 Starter Artifact 中；
3. `legacyParserTypesAreNotVisibleFromStarterRuntime`：Starter 仍把旧 XML/YAML Parser 放在运行时类路径。

## Controls

在进入 Starter RED 前：

- Context：26/26；
- Compiler：504/504；
- XML frontend：30/30；
- YAML frontend：59/59；
- 前置模块均成功；
- Reactor 明确列出 `dec-expand-declaration`，证明旧 module 仍在默认构建闭包。

Artifact 独立下载和解析结果：

- Surefire XML：105；
- 全部测试记录：622；
- 正常通过：619；
- T15 expected failures：3；
- Errors/Skipped：0/0。

## Gate

该运行是可编译、测试实际执行且原因精确的有效行为 RED。生产实现前不得削弱 T15 Oracle。

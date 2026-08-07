# TESTING-P1-T15-R02 — Declaration Runtime 退役门禁完整性验证

- Evidence ID：`EVD-001107`
- Testing：`TESTING-P1-T15-R02@7c901332b8e5`
- Iteration：`TASK-P1-T15 / I002`
- Code/Test Revision：`7c901332b8e5c559a73c127e1a1bd86411f8adc1`
- Status：`PASSED`

## Validation run

- P0 Run：`31092216605` — SUCCESS；
- Artifact：`8963981122`；
- GitHub digest：`sha256:b012e85a83b93fba76341fdeee5c719d147e57673e97d036f44abde259f7a016`；
- 独立 ZIP SHA-256：`b012e85a83b93fba76341fdeee5c719d147e57673e97d036f44abde259f7a016`。

Run 中以下步骤全部通过：Core build and tests、T14 provenance mutation、intentional failure gate、T15 declaration runtime retirement、Artifact upload。MySQL 为 `SKIPPED_NOT_APPLICABLE`。

## Surefire independent parse

- Surefire XML：110；
- All records：633；
- Normal passed：632；
- Intentional failure：1；
- Errors：0；
- Skipped：0；
- T15：10/10；Starter：10/10；Compiler：504/504；XML：30/30；YAML：59/59；Demo：3/3。

唯一 Failure 仍为 `dec.core.context.gate.P0IntentionalFailureTest.mustFail`，错误为 `P0 failure-gate proof`，只用于证明普通构建会阻断失败测试。

## Baseline retirement evidence

独立解析 `baseline-summary.json`：

- result：`PASSED`；violationCount：0；
- POM：11；Reactor dependency tree：11/11；
- source/resource files：693；
- class output directories：16；class files：947；compiled resources：205；
- Artifact：10；Artifact entries：958；unreadable Artifact：0；
- `ALL_PROJECT_POMS`、`ALL_REACTOR_DEPENDENCY_TREES`、`SOURCE_AND_REFLECTION_STRINGS`、`SERVICE_LOADER_CONTENT`、`CLASS_CONSTANT_POOLS`、`PUBLISHED_ARTIFACT_ENTRIES_AND_CONTENT` 全部为 true。

`dependency-tree-status.tsv` 包含根项目和十个子模块，11 行命令状态全部为 0；`baseline-dependency-tree-modules.json` 为 expected/covered `11/11`。

## Mutation and restore evidence

Mutation summary：

- result：`FAILED`，符合预期；
- Reactor/POM：12/12；
- violationCount：13；
- detected categories：`MODULE`、`POM_COORDINATE`、`DEPENDENCY_TREE`、`SOURCE_REFERENCE`、`SERVICE_LOADER`、`CLASS_CONSTANT_POOL`、`ARTIFACT_RESOURCE_CONTENT`；
- 直接检测非 Demo XML 模块依赖、root module/profile 坐标、旧模块依赖树、反射资源、ServiceLoader、编译 class 常量池和中性 ZIP entry 内容。

恢复后：

- result：`PASSED`；violationCount：0；
- Reactor dependency tree：11/11；POM：11；
- class/Artifact 扫描计数与基线一致；
- `restoredBaselinePassed=true`，且基线失败路径保持只读。

## Failed repair attempt retained as evidence

Run `31091739607` 的 Core、T14 和 failure gate 通过，但 T15 基线因共享 Maven outputFile 只覆盖 dec-demo 而失败。该结果推动改为逐模块独立报告，未被误登记为通过，也未用于 Completion。

Testing 结论：`PASSED`。

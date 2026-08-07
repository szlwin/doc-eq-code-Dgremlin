# DEV-P1-T15-R02 — Declaration Runtime 退役门禁完整性修复

- Evidence ID：`EVD-001106`
- Development：`DEV-P1-T15-R02@7c901332b8e5`
- Iteration：`TASK-P1-T15 / I002`
- Code/Test Revision：`7c901332b8e5c559a73c127e1a1bd86411f8adc1`
- Status：`PASSED`

## Rework trigger

重新 Review 打开 `FND-P1-T15-I001-002 / P1 / GATE / RETIREMENT_INTEGRITY / EVIDENCE_INTEGRITY`。确认 I001 门禁只直接扫描根 POM 与 Starter POM，Reactor 依赖树会被最后一个模块覆盖，且没有独立扫描 class 常量池和 Artifact 内容，因此 I001 的 Code Review、Testing 与 Completion 保留历史但失效。

`TDD-P1-T15-R01@bff67b86fb55`、`DESIGN-R51@P1-T15-I001`、`TP-P1-COMPILER-F01-R47@P1-T15-I001` 与 `DEVSKEL-P1-T15-R01@bff67b86fb55` 仍然有效；I002 只修复既有退役合同的门禁实现和证据完整性。

## Gate implementation

- 新增 `p1_t15_retirement_scan.py`，统一生成机器可解析的违规、计数和 scope 证据；
- 扫描所有非 `target`、非 `project_doc` 的 `pom.xml`，覆盖普通 dependency、dependencyManagement、profile 与 plugin dependency；
- 递归解析根 Reactor，冻结模块路径、groupId、artifactId 与 packaging 清单；
- 每个 Reactor 目标使用独立 Maven 命令和独立 dependency-tree 文件，禁止共享 outputFile；
- 每个独立依赖报告必须命令成功、文件存在且包含目标模块坐标，否则 fail-closed；
- 汇总报告保留明确模块标记，最终 Artifact 同时包含逐模块文件、命令日志、状态 TSV 和完整聚合树；
- 扫描 `target/classes` 与 `target/test-classes` 中所有 `.class` 字节，覆盖点号和斜线形式旧包名；
- 扫描编译输出中的资源及 `META-INF/services` 内容；
- 扫描 target 下 jar、war、zip 的路径、entry 名称、解压后的 class/资源内容，并递归检查嵌套归档；
- 任一归档或 entry 无法读取时登记 `ARTIFACT_UNREADABLE` 并阻断；
- summary 只根据真实扫描计数登记已覆盖 scope，不再声明未执行的覆盖面。

## Mutation proof

I002 mutation 同时注入并要求检测：

1. 真实 `dec-expand-declaration` Reactor 模块；
2. 非 Demo 模块 `dec-context-config-parse-xml` 的旧依赖；
3. 根 profile/dependencyManagement 中的旧坐标；
4. 中性资源文件名中的反射字符串；
5. ServiceLoader 文件内容；
6. 只存在于编译后 `.class` 常量池的旧 FQCN；
7. 中性 ZIP entry 中的旧内容。

Mutation 必须出现 `MODULE`、`POM_COORDINATE`、`DEPENDENCY_TREE`、`SOURCE_REFERENCE`、`SERVICE_LOADER`、`CLASS_CONSTANT_POOL`、`ARTIFACT_RESOURCE_CONTENT` 七类违规；清理后重新生成全部依赖树并恢复 GREEN。

## Repair attempts

- `d0bbec6b3dd5`：完成扫描器和七类 mutation；Run `31091739607` 真实揭示 Maven `appendOutput` 仍只保留最后模块，因此该尝试未作为完成证据；
- `7c901332b8e5`：改为逐模块独立 dependency tree，并验证每个文件的目标坐标；Run `31092216605` 全部通过。

## Style and scope

- 本迭代未修改 Java 生产代码和 JUnit 测试代码；
- 未新增或改动 `@Override`，既有注解继续独占一行；
- Python 函数、Shell 阶段和所有关键扫描、恢复、fail-closed 逻辑均使用中文注释；
- 不引入新运行时依赖，不恢复旧 Declaration Runtime，不实现 P2～P7 runtime。

Development 完成，进入独立 Code Review、Testing 与 Completion。

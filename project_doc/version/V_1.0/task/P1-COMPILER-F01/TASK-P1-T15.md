# TASK-P1-T15 — 旧核心只读投影与 Declaration Runtime 整体退役

- Iteration：`I001`
- Status：`IN_PROGRESS / TDD_RED_PENDING`
- Base：`dev_all@665dd364975505bb01263885a25b3bb1be767d2b`
- Dependency：`COMPLETION-P1-T14-R03@37fb814b39c5`
- Branch：`feature/p1-t15-retire-declaration-20260806-1354`
- Design：`DESIGN-R51@P1-T15-I001`
- Plan：`TP-P1-COMPILER-F01-R47@P1-T15-I001`
- Open P0/P1/P2：`0 / 0 / 0`

## Goal

- 保持 `EngineContext → CoreConfigProjection` 单一事实源；
- Starter 改为实例级 `ModelCompiler` 委托，不写全局 Config；
- 删除旧 Starter 全局配置入口和 Parser 依赖；
- 整体删除 `dec-expand-declaration`；
- 建立全仓与 Artifact 残留扫描；
- 不保留 Adapter、复制实现或运行时双轨。

## Allowed scope

- `pom.xml`；
- `dec-core-starter` POM、源码与测试；
- `dec-expand-declaration/` 整体删除；
- `.github/workflows/p0-build.yml`；
- `scripts/remediation/prove_p1_t15_retirement_gate.sh`；
- `dec-core-context` 仅允许补测试，除非 Review 证明已有 Projection 合同缺陷；
- `project_doc` 流程证据。

## Excluded

- P2～P7 runtime；
- SQL/MySQL 业务语义；
- demo 路径硬编码；
- global current EngineContext；
- LegacyDeclarationAdapter；
- 将旧模块代码复制到其他模块。

## Required evidence

- 可编译行为 RED；
- Architecture Gate；
- First GREEN；
- 独立 Review；
- Starter/Context/Compiler/全 Reactor；
- Java 8、intentional failure、T14 mutation gate；
- retirement dependency/source/service/reflection/artifact report；
- Artifact 独立 SHA/XML 解析；
- Revision Integrity；
- Completion、handoff、resume 与 PR Review。

未经用户明确授权不得合并本任务 PR。

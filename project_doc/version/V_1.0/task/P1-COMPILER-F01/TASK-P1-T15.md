# TASK-P1-T15 — 旧核心只读投影与 Declaration Runtime 整体退役

- Current Iteration：`I002`
- Status：`COMPLETED / PASSED`
- Base：`dev_all@665dd364975505bb01263885a25b3bb1be767d2b`
- Dependency：`COMPLETION-P1-T14-R03@37fb814b39c5`
- Branch：`feature/p1-t15-retire-declaration-20260806-1354`
- PR：`#30 / OPEN / READY_FOR_REVIEW / NOT_MERGED`
- Design：`DESIGN-R51@P1-T15-I001`
- Plan：`TP-P1-COMPILER-F01-R47@P1-T15-I001`
- TDD：`TDD-P1-T15-R01@bff67b86fb55` — VALID
- Architecture：`DEVSKEL-P1-T15-R01@bff67b86fb55` — VALID
- Development：`DEV-P1-T15-R02@7c901332b8e5`
- Code Review：`CODEREVIEW-P1-T15-R02@7c901332b8e5`
- Testing：`TESTING-P1-T15-R02@7c901332b8e5`
- Completion：`COMPLETION-P1-T15-R02@7c901332b8e5`
- Open P0/P1/P2：`0 / 0 / 0`

## Iteration history

### I001

完成实例级 `CompilerStarter`、Projection 单一事实源和旧 Declaration Runtime 删除。Starter 行为 Oracle 在 `FND-P1-T15-I001-001` 中补齐并关闭。

重新 Review 打开 `FND-P1-T15-I001-002 / P1`：I001 retirement gate 未完整覆盖全部 POM、完整 Reactor dependency tree、class 常量池和 Artifact 内容。因此 I001 的 Review、Testing、Completion 保留历史但由 I002 取代；TDD、Design、Plan、Architecture 继续有效。

### I002

- 新增统一机器扫描器，真实枚举全部 POM、源码/资源、class 输出和发布 Artifact；
- 逐 Reactor 模块独立生成 dependency tree，禁止共享输出覆盖；
- 每个报告必须命令成功、文件存在并包含目标模块坐标；
- 扫描 class 常量池、编译资源、ServiceLoader、Archive entry 和解压内容；
- 无法读取 Artifact 或 entry 时 fail-closed；
- mutation 覆盖非 Demo POM、root profile/dependencyManagement、完整依赖树、反射资源、ServiceLoader、class 常量池和中性 ZIP 内容；
- 基线、预期 mutation 阻断和清理恢复全部通过。

## Production contract

- `EngineContext → CoreConfigProjection` 仍是旧核心唯一只读事实源；
- `CompilerStarter` 只持有实例级 `ModelCompiler`；
- 编译发布精确委托一次，输入与结果不复制、不改写；
- Projection 只来自 `PublishedCompilationResult.engineContext()`；
- Starter 不保存全局 current Context，不拥有额外 Publisher/CAS；
- `dec-expand-declaration` 已从 Git、POM、Reactor、依赖图、源码、class 和 Artifact 整体退役；
- 无 Adapter、反射生产逻辑、ServiceLoader 回流或双轨 runtime。

## Validation

- Code/Test Revision：`7c901332b8e5c559a73c127e1a1bd86411f8adc1`；
- P0 Run / Artifact：`31092216605 / 8963981122`；
- SHA-256：`b012e85a83b93fba76341fdeee5c719d147e57673e97d036f44abde259f7a016`；
- Surefire XML：110；All：633；Normal passed：632；intentional failure：1；Errors/Skipped：0/0；
- T15：10/10；Starter：10/10；Compiler：504/504；XML：30/30；YAML：59/59；Demo：3/3；
- baseline POM / Reactor：`11 / 11-of-11`；class：947；compiled resources：205；
- Artifact / entries / unreadable：`10 / 958 / 0`；
- mutation Reactor：12/12，七类违规全部检测；
- baseline / mutation blocked / restored：`PASSED / YES / PASSED`；
- Java release 8、T14 mutation、T15 retirement、intentional failure gate：PASSED；
- MySQL：`SKIPPED_NOT_APPLICABLE`。

## Revision and style

- `d0bbec6b3dd5` 的共享 appendOutput 尝试在 Run `31091739607` 被真实 CI 拒绝，未作为完成证据；
- 最终修复 revision 为 `7c901332b8e5`；
- I002 未修改 Java 生产和测试源码，既有 `@Override` 继续独占一行；
- Python/Shell 方法、扫描、mutation、恢复和重要门禁逻辑均使用中文注释；
- 本 Revision 后只允许 `project_doc` 与 PR 元数据更新。

PR #30 未执行合并；未经用户明确授权不得合并。

# TASK-P1-T15 — 旧核心只读投影与 Declaration Runtime 整体退役

- Current Iteration：`I003`
- Status：`COMPLETED / PASSED`
- Base：`dev_all@665dd364975505bb01263885a25b3bb1be767d2b`
- Dependency：`COMPLETION-P1-T14-R03@37fb814b39c5`
- Branch：`feature/p1-t15-retire-declaration-20260806-1354`
- PR：`#30 / OPEN / READY_FOR_REVIEW / NOT_MERGED`
- Design：`DESIGN-R51@P1-T15-I001`
- Plan：`TP-P1-COMPILER-F01-R47@P1-T15-I001`
- TDD：`TDD-P1-T15-R01@bff67b86fb55` — VALID
- Architecture：`DEVSKEL-P1-T15-R01@bff67b86fb55` — VALID
- Development：`DEV-P1-T15-R03@66fa5db14f7b`
- Code Review：`CODEREVIEW-P1-T15-R03@66fa5db14f7b`
- Testing：`TESTING-P1-T15-R03@66fa5db14f7b`
- Completion：`COMPLETION-P1-T15-R03@66fa5db14f7b`
- Open P0/P1/P2：`0 / 0 / 0`

## Iteration history

### I001

完成实例级 `CompilerStarter`、Projection 单一事实源和旧 Declaration Runtime 删除。Starter 行为 Oracle 在 `FND-P1-T15-I001-001` 中补齐并关闭。

重新 Review 打开 `FND-P1-T15-I001-002 / P1`：I001 retirement gate 未完整覆盖全部 POM、完整 Reactor dependency tree、class 常量池和 Artifact 内容。因此 I001 的 Review、Testing、Completion 保留历史但由 I002 取代；TDD、Design、Plan、Architecture 继续有效。

### I002

完成全仓 POM、逐 Reactor dependency tree、class 常量池、编译资源、ServiceLoader 和发布 Artifact 内容扫描，并以七类 mutation 证明退役门禁 fail-closed。`FND-P1-T15-I001-002` 已关闭。

后续验收复核打开 `FND-P1-T15-I002-003 / P1 / MYSQL_BUSINESS_EXECUTION / EVIDENCE_INTEGRITY`：原 `mysql-it` 只证明 MySQL 容器、Schema 和带 Profile 的 Reactor 构建成功；PR 中不存在 `RuleTests` 等真实业务执行测试，I002 的 Review、Testing、Completion 因数据库业务验收缺失而由 I003 取代。I002 的生产退役实现和 retirement gate 结论继续有效。

### I003

- 以 `dev_all` 的 `RuleTests`、`DirectoryTest`、`OrderTest` 为业务 Oracle，恢复三个代表性 MySQL 场景；
- 不恢复依赖已退役 `dec-expand-declaration` 的 `TestOrderBusiness`；
- 新增测试专用 `DemoMySqlTestSupport`，直接组装仍保留的 XML Parser、Config、DataConnectionFactory 和 MySQL Adapter，不重新引入已删除的 Starter 全局工具；
- `RuleTests` 验证 Rule、ORM、双数据源写入和 DSL 改值；
- `DirectoryTest` 验证目录状态范围转换为 SQL 后只返回目标用户；
- `OrderTest` 验证订单、明细、支付和支付明细四表聚合写入及主外键回填；
- 每个场景在全部业务断言通过后写入独立数据库执行标记；
- CI 同时校验 Surefire XML、必需测试类、失败/错误/跳过计数、三条执行标记和业务表结果，任何测试缺失或被过滤均 fail-closed；
- `FND-P1-T15-I002-003` 已关闭。

## Production contract

- `EngineContext → CoreConfigProjection` 仍是旧核心唯一只读事实源；
- `CompilerStarter` 只持有实例级 `ModelCompiler`；
- 编译发布精确委托一次，输入与结果不复制、不改写；
- Projection 只来自 `PublishedCompilationResult.engineContext()`；
- Starter 不保存全局 current Context，不拥有额外 Publisher/CAS；
- `dec-expand-declaration` 已从 Git、POM、Reactor、依赖图、源码、class 和 Artifact 整体退役；
- I003 只新增测试夹具和验收门禁，不恢复 Declaration Runtime、Adapter、反射生产逻辑或双轨运行时。

## Validation

- Code/Test Revision：`66fa5db14f7b0ead00e5d706acd164e1a9f4ff62`；
- P0 Run：`31119253989`，Attempt 3：`SUCCESS`；
- Core Artifact：`8974119611`；SHA-256：`929a748eea9b70e5b5e2ac15e5501b204f8149c5a62d099c67d85870d85d0bb7`；
- MySQL Artifact：`8974141953`；SHA-256：`6a79f5c2140225fcaa4ac7930cd305f959c96b756ec3565f39058a8ea511b7ce`；
- Core Surefire XML：112；All：639；Normal passed：638；intentional failure：1；Errors/Skipped：0/0；
- MySQL 业务测试：3/3；Failures/Errors/Skipped：0/0/0；
- `RuleTests`、`DirectoryTest`、`OrderTest`：全部存在且通过；
- 数据库执行标记：3/3；
- 最终数据库证据：order/order-detail/pay/pay-detail/product/user=`1/1/1/1/1/3`；
- Java release 8、T14 mutation、T15 retirement、intentional failure gate：PASSED；
- MySQL 业务执行与数据库结果门禁：PASSED。

## Revision and style

- Attempt 1 与 Attempt 2 在 checkout 前因 GitHub `503 Service Unavailable / Failed to resolve action download info` 失败，保留为基础设施历史，未作为代码失败或完成证据；
- Attempt 3 真实启动 MySQL 8.0、导入两个 Schema、编译并执行三类业务测试；
- 新增 Java 类和重要方法均有中文说明；新增 `@Override` 独占一行；
- 测试夹具只存在于 `src/test`，未修改 Java 生产源码；
- 本 Revision 后只允许 `project_doc` 与 PR 元数据更新。

PR #30 未执行合并；未经用户明确授权不得合并。

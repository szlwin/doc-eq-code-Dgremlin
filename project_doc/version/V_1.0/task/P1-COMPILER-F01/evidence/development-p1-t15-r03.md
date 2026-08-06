# DEV-P1-T15-R03 — dec-demo MySQL 业务执行验收补强

- Evidence ID：`EVD-001108`
- Development：`DEV-P1-T15-R03@66fa5db14f7b`
- Iteration：`TASK-P1-T15 / I003`
- Code/Test Revision：`66fa5db14f7b0ead00e5d706acd164e1a9f4ff62`
- Status：`PASSED`

## Rework trigger

复核确认 `FND-P1-T15-I002-003 / P1 / MYSQL_BUSINESS_EXECUTION / EVIDENCE_INTEGRITY`：I002 的 `mysql-it` Job 只启动 MySQL、导入 Schema 并执行带 Profile 的 Reactor；PR 中已不存在 `dev_all` 的 `RuleTests`、`DirectoryTest`、`OrderTest` 等业务执行测试，且 `failIfNoTests=false` 允许空跑成功。因此 I002 的 Code Review、Testing 与 Completion 保留历史但由 I003 取代。

I001/I002 的生产退役实现、retirement gate、TDD、Design、Plan 与 Architecture 继续有效；I003 只补齐 `dec-demo` 业务/MySQL 验收和证据门禁。

## Test runtime implementation

- 新增测试专用 `DemoMySqlTestSupport`；
- 直接使用仍保留的 `ConfigFileParser`、`ConfigManager`、`DataConnectionFactory` 和 MySQL Connection/Convert/Execute Factory；
- 将 `data1`、`data2` 分别绑定到 `demo-test2`、`demo-test1`；
- 使用 Hikari 数据源并在每个测试类结束后关闭连接池；
- 不恢复 `ConfigUtil`、`DataSourceManager` 或 `ContainerManager.getCurrentModelContainer()` 等已删除的 Starter 全局写入口；
- 不恢复依赖 `dec-expand-declaration` 的 `TestOrderBusiness`。

## Restored business scenarios

### RuleTests

- 复用 `dev_all` 的 `save-Order + back-Order` 双连接业务路径；
- 构造 `OrderInfo`、用户和两个商品；
- 执行真实 `ModelContainer.execute()`；
- 断言主库用户/订单/商品、次库商品、删除规则和 DSL 价格改值均生效。

### DirectoryTest

- 导入 register/auth/范围外三类用户；
- 执行目录 `start(register) → end(auth)` 查询；
- 断言只返回两个目标用户，证明目录条件被转换为真实 SQL。

### OrderTest

- 构造订单、订单明细、支付和支付明细聚合；
- 执行 `save-Order`；
- 断言四张业务表各写入一行，且订单 ID、支付 ID 已回填到关联记录。

## Fail-closed gates

- `dec-demo` 的 `mysql-it` Profile 只运行 `@Tag("mysql-it")`，并设置 `failIfNoTests=true`；
- `verify_dec_demo_mysql_it.py` 必须发现三个指定 Surefire suite，测试数至少 3，Failures/Errors/Skipped 必须全部为 0；
- 每个业务测试只有在全部 SQL 断言通过后才写入 `dec_test_execution_audit`；
- Workflow 必须发现三条精确执行标记，并输出业务表计数；
- MySQL 业务 Artifact 使用 `if-no-files-found=error`，报告缺失直接阻断。

## Style and scope

- 新增 Java 类、公开方法和重要组装逻辑均有中文注释；
- `@Override` 独占一行；
- 未修改 Java 生产源码；
- 未重新引入 Declaration Runtime 或生产双轨；
- 本地环境因 Maven 镜像 DNS 不可达只完成静态解析，真实编译和数据库执行以 GitHub Actions Revision 绑定证据为准。

Development 完成，进入独立 Code Review、Testing 与 Completion。

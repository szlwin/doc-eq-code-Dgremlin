# CODEREVIEW-P1-T15-R03 — dec-demo MySQL 业务验收独立 Review

- Review ID：`REV-000762`
- Code Review：`CODEREVIEW-P1-T15-R03@66fa5db14f7b`
- Input：`DEV-P1-T15-R03@66fa5db14f7b`
- Iteration：`TASK-P1-T15 / I003`
- Status：`PASSED`
- Open P0/P1/P2：`0 / 0 / 0`

## Historical invalidation

`FND-P1-T15-I002-003` 证明 I002 将 MySQL 环境/Schema 成功误当成业务数据库执行成功，且 PR 中没有 `RuleTests` 等真实业务测试。因此以下记录保留历史但由 I003 取代：

- `CODEREVIEW-P1-T15-R02@7c901332b8e5`；
- `TESTING-P1-T15-R02@7c901332b8e5`；
- `COMPLETION-P1-T15-R02@7c901332b8e5`。

I002 的生产退役实现和 retirement gate 结论仍有效。

## Review scope

- `dev_all` 业务 Oracle 与 I003 恢复范围；
- 测试夹具与已退役 Starter 全局入口之间的边界；
- Rule、Directory、Order 三类业务执行和 SQL 结果 Oracle；
- 双数据源、资源关闭和测试隔离；
- Surefire、数据库执行标记和 Artifact fail-closed 门禁；
- Declaration Runtime 不回流；
- Java 8、中文注释与 `@Override` 格式。

## Review findings

### FND-P1-T15-I002-003 — P1 / MYSQL_BUSINESS_EXECUTION / EVIDENCE_INTEGRITY

Disposition：`CLOSED`。

关闭证据：

- 三个必需测试类都包含 `@Tag("mysql-it")` 并由 Profile 显式运行；
- `failIfNoTests=true`，测试全部缺失时 Maven 直接失败；
- 独立 Python 门禁要求三个 suite 均存在且 Failures/Errors/Skipped 为 0；
- 每个测试均调用真实业务执行入口，并以 JDBC 查询断言数据库状态；
- 三条执行标记只在业务断言全部通过后写入；
- Workflow 独立查询三条标记和业务表计数；
- Artifact 含三个 XML、三份文本报告、机器摘要和数据库证据；
- 独立下载后的 SHA-256 与 GitHub digest 一致。

## Code review conclusions

- `DemoMySqlTestSupport` 位于测试源码，不改变生产运行时；
- 测试夹具直接使用仍保留的底层 Parser/Config/Factory，不恢复已删除的 `ConfigUtil`、`DataSourceManager` 等全局写入口；
- 每个测试类由既有 Profile 在独立 JVM 中运行，且各场景主动清理自身业务表，避免单例注册与数据库状态互相污染；
- Hikari 主/次连接池均通过 `AutoCloseable` 关闭；
- `RuleTests` 同时验证主库、次库、删除规则和 DSL 改值，不是只检查无异常；
- `DirectoryTest` 直接断言返回集合；
- `OrderTest` 验证四表写入和关联 ID 回填；
- 未恢复 `TestOrderBusiness`，避免重新引入 `dec-expand-declaration`；
- CI 的报告和数据库门禁均 fail-closed；
- 新增方法和重要逻辑有中文说明，`@Override` 独占一行。

Review 结论：`PASSED / FND-P1-T15-I002-003 CLOSED / NO_OPEN_P0_P1_P2`，允许进入最终 Testing 与 Completion。

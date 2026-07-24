# P0 决策

1. 构建 JDK 固定为 17，产出仍使用 Java 8 `release`，P0 不升级业务语言级别。
2. Maven Wrapper 使用 3.3.4 的 only-script 模式，Maven 固定为 3.9.16，不提交二进制 wrapper JAR。
3. 默认 `clean verify` 不连接 MySQL；旧数据库场景通过 `mysql-it` 标签和 profile 隔离。
4. P0 只冻结旧资源和公开 API，不修正 XML/YAML 语义漂移。
5. `DirectoryTest`、`RuleTests`、`OrderTest`、`TestOrderBusiness` 与数据库测试辅助类已迁入 `src/test/java`；数据库场景使用 `mysql-it` 标签隔离。

6. 仓库内已有的 `smarter`/`javolution` JAR 通过受控 bootstrap 脚本安装到 Maven 本地仓库，避免依赖开发机预装的私有 artifact。

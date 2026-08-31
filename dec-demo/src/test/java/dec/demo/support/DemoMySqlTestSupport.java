package dec.demo.support;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dec.core.starter.common.ConfigUtil;
import dec.core.starter.common.DataSourceManager;
import dec.external.datasource.sql.datasource.DBDataSource;
import dec.external.datasource.sql.mysql.connection.factory.MySQLDBConnectionFactory;
import dec.external.datasource.sql.mysql.convert.container.factory.MySQLConvertContainerFactory;
import dec.external.datasource.sql.mysql.datatype.convert.factory.MySQLDataConvertContainerFactory;
import dec.external.datasource.sql.mysql.execute.container.factory.MySQLExecuteContainerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 为 dec-demo 的遗留业务执行测试组装隔离的 MySQL 运行环境。
 *
 * <p>初始化过程只使用 Starter 的简洁门面，业务测试不创建、传递或安装 ConfigInfo。</p>
 */
public final class DemoMySqlTestSupport implements AutoCloseable {
    private static final String MYSQL_TYPE = "MySQL";
    private static final String PRIMARY_DEFAULT =
            "jdbc:mysql://127.0.0.1:3306/demo-test2"
                    + "?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String SECONDARY_DEFAULT =
            "jdbc:mysql://127.0.0.1:3306/demo-test1"
                    + "?useSSL=false&allowPublicKeyRetrieval=true";

    private final HikariDataSource primaryDataSource;
    private final HikariDataSource secondaryDataSource;

    private DemoMySqlTestSupport(
            HikariDataSource primaryDataSource,
            HikariDataSource secondaryDataSource) {
        this.primaryDataSource = primaryDataSource;
        this.secondaryDataSource = secondaryDataSource;
    }

    /**
     * 加载指定 dec-demo 配置，并把两个逻辑数据源绑定到独立测试库。
     *
     * @param configResource 不带 classpath: 前缀的配置资源
     * @return 已完成 Parser、工厂和数据源装配的测试夹具
     */
    public static DemoMySqlTestSupport load(String configResource) throws Exception {
        // 1. 添加 MySQL 数据源类型；候选 ConfigInfo 由框架内部维护。
        ConfigUtil.addDataSourceConfig(MYSQL_TYPE, DBDataSource.class.getName());
        // 2. 加载配置文件；业务代码只提供路径。
        ConfigUtil.parseConfigInfo("classpath:" + configResource);
        // 3. 注册数据库连接、转换和执行实现。
        registerFactories();
        // 4. 绑定两个实际数据库连接池。
        return addDataSources();
    }

    private static DemoMySqlTestSupport addDataSources() throws Exception {
        HikariDataSource primary = null;
        HikariDataSource secondary = null;
        try {
            primary = createDataSource(
                    "dec-demo-primary",
                    env("DEC_MYSQL_URL", PRIMARY_DEFAULT));
            secondary = createDataSource(
                    "dec-demo-secondary",
                    env("DEC_MYSQL_URL_SECONDARY", SECONDARY_DEFAULT));
            DataSourceManager.addDataSource("data1", primary);
            DataSourceManager.addDataSource("data2", secondary);
            return new DemoMySqlTestSupport(primary, secondary);
        } catch (Exception failure) {
            closeQuietly(secondary);
            closeQuietly(primary);
            throw failure;
        }
    }

    /**
     * 获取主测试库连接。调用方负责关闭连接。
     */
    public Connection primaryConnection() throws SQLException {
        return primaryDataSource.getConnection();
    }

    /**
     * 获取第二测试库连接。调用方负责关闭连接。
     */
    public Connection secondaryConnection() throws SQLException {
        return secondaryDataSource.getConnection();
    }

    /**
     * 在业务断言全部通过后写入执行标记，供 CI 独立确认测试没有被空跑或过滤。
     */
    public void recordExecution(String testCase) throws SQLException {
        try (Connection connection = primaryConnection();
                PreparedStatement delete = connection.prepareStatement(
                        "DELETE FROM dec_test_execution_audit WHERE test_case = ?");
                PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO dec_test_execution_audit(test_case) VALUES (?)")) {
            delete.setString(1, testCase);
            delete.executeUpdate();
            insert.setString(1, testCase);
            insert.executeUpdate();
        }
    }

    private static void registerFactories() {
        DataSourceManager.addConnectionFactory(MYSQL_TYPE, new MySQLDBConnectionFactory());
        DataSourceManager.addConvertContainerFactory(
                MYSQL_TYPE,
                new MySQLConvertContainerFactory());
        DataSourceManager.addDataConvertContainerFacory(
                MYSQL_TYPE,
                new MySQLDataConvertContainerFactory());
        DataSourceManager.addExecuteContainerFacory(
                MYSQL_TYPE,
                new MySQLExecuteContainerFactory());
    }

    private static void closeQuietly(HikariDataSource dataSource) {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private static HikariDataSource createDataSource(String poolName, String jdbcUrl) {
        HikariConfig config = new HikariConfig();
        config.setPoolName(poolName);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(env("DEC_MYSQL_USER", "root"));
        config.setPassword(env("DEC_MYSQL_PASSWORD", "mysqldb"));
        config.setMinimumIdle(0);
        config.setMaximumPoolSize(3);
        return new HikariDataSource(config);
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    @Override
    public void close() {
        secondaryDataSource.close();
        primaryDataSource.close();
    }
}

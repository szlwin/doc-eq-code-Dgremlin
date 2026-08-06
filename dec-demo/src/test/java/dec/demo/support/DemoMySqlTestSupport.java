package dec.demo.support;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dec.context.parse.xml.parse.config.ConfigFileParser;
import dec.core.context.config.manager.ConfigManager;
import dec.core.context.config.model.config.Config;
import dec.core.context.config.model.config.ConfigInfo;
import dec.core.context.config.model.connection.ConnectionInfo;
import dec.core.context.config.model.datasource.DataSourceConfigInfo;
import dec.core.context.config.utils.ConfigContextUtil;
import dec.core.model.connection.DataConnectionFactory;
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
 * <p>该夹具只存在于测试源码中，直接使用仍保留的底层 Parser、Config 和数据源工厂，
 * 不重新引入 T15 已删除的 Starter 全局写入口。</p>
 */
public final class DemoMySqlTestSupport implements AutoCloseable {
    private static final String MYSQL_TYPE = "MySQL";
    private static final String PRIMARY_DEFAULT =
            "jdbc:mysql://127.0.0.1:3306/demo-test2"
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String SECONDARY_DEFAULT =
            "jdbc:mysql://127.0.0.1:3306/demo-test1"
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

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
        ConfigInfo configInfo = new ConfigInfo();
        ConfigManager.getInstance().setConfigInfo(configInfo);
        registerMySqlType(configInfo);
        new ConfigFileParser().parse("classpath:" + configResource);
        registerFactories();

        HikariDataSource primary = createDataSource(
                "dec-demo-primary",
                env("DEC_MYSQL_URL", PRIMARY_DEFAULT));
        HikariDataSource secondary = createDataSource(
                "dec-demo-secondary",
                env("DEC_MYSQL_URL_SECONDARY", SECONDARY_DEFAULT));
        try {
            bindDataSource("data1", primary);
            bindDataSource("data2", secondary);
            return new DemoMySqlTestSupport(primary, secondary);
        } catch (Exception failure) {
            primary.close();
            secondary.close();
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

    private static void registerMySqlType(ConfigInfo configInfo) {
        DataSourceConfigInfo dataSourceConfig = new DataSourceConfigInfo();
        dataSourceConfig.setName(MYSQL_TYPE);
        dataSourceConfig.setDataSource(DBDataSource.class.getName());
        configInfo.add(Config.DATASOURCE_CONFIG, dataSourceConfig);

        ConnectionInfo<?, ?, ?> connectionInfo = new ConnectionInfo<Object, Object, Object>();
        connectionInfo.setName(MYSQL_TYPE);
        configInfo.add(Config.CONNECTION_CONFIG, connectionInfo);
    }

    private static void registerFactories() {
        DataConnectionFactory factory = DataConnectionFactory.getInstance();
        factory.addConnectionFactory(MYSQL_TYPE, new MySQLDBConnectionFactory());
        factory.addConvertContainerFactory(MYSQL_TYPE, new MySQLConvertContainerFactory());
        factory.addDataConvertContainerFacory(
                MYSQL_TYPE,
                new MySQLDataConvertContainerFactory());
        factory.addExecuteContainerFacory(MYSQL_TYPE, new MySQLExecuteContainerFactory());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void bindDataSource(
            String logicalName,
            javax.sql.DataSource jdbcDataSource) {
        dec.core.context.config.model.datasource.DataSource model =
                ConfigContextUtil.getConfigInfo().getDataSource(logicalName);
        if (model == null) {
            throw new IllegalStateException("Missing data source model: " + logicalName);
        }
        DBDataSource container = new DBDataSource();
        container.setDataSource(jdbcDataSource);
        model.setDataSource(container);
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

package dec.core.starter.common;

import dec.core.context.config.manager.ConfigManager;
import dec.core.context.config.model.config.Config;
import dec.core.context.config.model.config.ConfigInfo;
import dec.core.context.config.model.datasource.DataSourceConfigInfo;
import dec.core.datasource.connection.factory.DBConectionFacory;
import dec.core.datasource.convert.container.factory.ConvertContainerFacory;
import dec.core.datasource.datatype.convert.factory.DataConvertContainerFacory;
import dec.core.datasource.execute.container.factory.ExecuteContainerFacory;
import dec.core.model.connection.DataConnectionFactory;
import java.util.Objects;

/**
 * 提供不暴露 ConfigInfo 的数据源和工厂注册入口。
 *
 * <p>连接与数据源的配置关系由 XML/YAML 解析器建立；这里仅绑定运行时数据源对象，
 * 不要求业务代码额外创建 ConnectionInfo。</p>
 */
public final class DataSourceManager {
    private DataSourceManager() {
    }

    /**
     * 将 JDBC 连接池等真实数据源绑定到配置中的逻辑数据源。
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void addDataSource(String name, Object dataSource) throws Exception {
        String checkedName = requireText(name, "逻辑数据源名称");
        Object checkedDataSource = Objects.requireNonNull(dataSource, "数据源对象不能为空");
        ConfigInfo configInfo = ConfigManager.getInstance().getInstalledConfigInfo();
        dec.core.context.config.model.datasource.DataSource model =
                configInfo.getDataSource(checkedName);
        if (model == null) {
            throw new IllegalStateException("配置中不存在数据源: " + checkedName);
        }

        String type = requireText(model.getType(), "数据源 " + checkedName + " 的类型");
        DataSourceConfigInfo typeConfig = (DataSourceConfigInfo) configInfo.get(
                Config.DATASOURCE_CONFIG,
                type);
        if (typeConfig == null) {
            throw new IllegalStateException("数据源类型尚未注册: " + type);
        }

        String containerClassName = requireText(
                typeConfig.getDataSource(),
                "数据源类型 " + type + " 的实现类");
        Object instance = Class.forName(containerClassName)
                .getDeclaredConstructor()
                .newInstance();
        if (!(instance instanceof dec.core.datasource.connection.datasource.DataSource)) {
            throw new IllegalStateException(
                    "数据源实现未实现框架 DataSource 接口: " + containerClassName);
        }

        dec.core.datasource.connection.datasource.DataSource container =
                (dec.core.datasource.connection.datasource.DataSource) instance;
        container.setDataSource(checkedDataSource);
        model.setDataSource(container);
    }

    public static void addConnectionFactory(
            String name,
            DBConectionFacory<?, ?> factory) {
        DataConnectionFactory.getInstance().addConnectionFactory(
                requireText(name, "数据源类型名称"),
                Objects.requireNonNull(factory, "连接工厂不能为空"));
    }

    public static void addConvertContainerFactory(
            String name,
            ConvertContainerFacory<?, ?> factory) {
        DataConnectionFactory.getInstance().addConvertContainerFactory(
                requireText(name, "数据源类型名称"),
                Objects.requireNonNull(factory, "参数转换工厂不能为空"));
    }

    /** 保留原有拼写，避免已有初始化代码被迫修改。 */
    public static void addDataConvertContainerFacory(
            String name,
            DataConvertContainerFacory factory) {
        DataConnectionFactory.getInstance().addDataConvertContainerFacory(
                requireText(name, "数据源类型名称"),
                Objects.requireNonNull(factory, "数据转换工厂不能为空"));
    }

    /** 保留原有拼写，避免已有初始化代码被迫修改。 */
    public static void addExecuteContainerFacory(
            String name,
            ExecuteContainerFacory<?, ?> factory) {
        DataConnectionFactory.getInstance().addExecuteContainerFacory(
                requireText(name, "数据源类型名称"),
                Objects.requireNonNull(factory, "执行工厂不能为空"));
    }

    private static String requireText(String value, String label) {
        String checked = Objects.requireNonNull(value, label + "不能为空").trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(label + "不能为空");
        }
        return checked;
    }
}

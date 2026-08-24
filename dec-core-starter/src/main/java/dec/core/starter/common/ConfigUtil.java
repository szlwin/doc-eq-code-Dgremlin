package dec.core.starter.common;

import dec.context.parse.xml.exception.XMLParseException;
import dec.context.parse.xml.parse.config.ConfigFileParser;
import dec.context.parse.xml.parse.config.ConnectionInfoParser;
import dec.core.context.config.manager.ConfigManager;
import dec.core.context.config.model.config.Config;
import dec.core.context.config.model.config.ConfigInfo;
import dec.core.context.config.model.datasource.DataSourceConfigInfo;
import java.util.Objects;

/**
 * 提供不暴露 ConfigInfo 的简洁配置入口。
 *
 * <p>配置会先写入当前线程的候选对象，整个文件解析成功后才替换已安装配置。
 * 业务初始化代码只需要声明数据源类型并加载配置文件。</p>
 *
 * <p>需求来源：FEATURE-DESC-3361AD2E54FC requirement.md 中
 * BR-P2-SYSTEM-RULEVIEW-001、BR-P2-SYSTEM-RULEVIEW-008。</p>
 */
public final class ConfigUtil {
    private static final ThreadLocal<ConfigInfo> CANDIDATE =
            new ThreadLocal<ConfigInfo>();

    private ConfigUtil() {
    }

    /**
     * 为下一次配置加载登记一种数据源实现。
     *
     * @param name 配置文件中使用的数据源类型，例如 MySQL
     * @param className 对应的数据源容器实现类名
     */
    public static void addDataSourceConfig(String name, String className) {
        String checkedName = requireText(name, "数据源类型名称");
        String checkedClassName = requireText(className, "数据源实现类名");

        DataSourceConfigInfo dataSource = new DataSourceConfigInfo();
        dataSource.setName(checkedName);
        dataSource.setDataSource(checkedClassName);
        candidate().add(Config.DATASOURCE_CONFIG, dataSource);
    }

    /**
     * 加载完整配置文件。解析失败时，当前正在使用的配置保持不变。
     */
    public static void parseConfigInfo(String filePath) throws XMLParseException {
        ConfigInfo candidate = candidate();
        try {
            ConfigInfo parsed = new ConfigFileParser().parseInto(
                    candidate,
                    requireText(filePath, "配置文件路径"));
            ConfigManager.getInstance().setConfigInfo(parsed);
        } finally {
            CANDIDATE.remove();
        }
    }

    /**
     * 加载旧格式的连接类型配置。保留该入口用于兼容已有应用。
     */
    public static void parseConnectionInfo(String filePath) throws XMLParseException {
        ConfigInfo candidate = candidate();
        try {
            ConfigInfo parsed = new ConnectionInfoParser().parseInto(
                    candidate,
                    requireText(filePath, "连接配置文件路径"));
            ConfigManager.getInstance().setConfigInfo(parsed);
        } finally {
            CANDIDATE.remove();
        }
    }

    private static ConfigInfo candidate() {
        ConfigInfo value = CANDIDATE.get();
        if (value == null) {
            value = new ConfigInfo();
            CANDIDATE.set(value);
        }
        return value;
    }

    private static String requireText(String value, String label) {
        String checked = Objects.requireNonNull(value, label + "不能为空").trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(label + "不能为空");
        }
        return checked;
    }
}

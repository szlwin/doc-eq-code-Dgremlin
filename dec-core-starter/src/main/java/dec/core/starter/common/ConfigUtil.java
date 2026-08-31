package dec.core.starter.common;

import dec.context.parse.xml.exception.XMLParseException;
import dec.context.parse.xml.parse.config.ConfigFileParser;
import dec.context.parse.xml.parse.config.ConnectionInfoParser;
import dec.context.parse.yaml.exception.YAMLParseException;
import dec.context.parse.yaml.parse.config.YamlConfigFileParser;
import dec.core.compiler.api.CompilationOptions;
import dec.core.compiler.api.CompilationResult;
import dec.core.compiler.api.CompilationStatus;
import dec.core.compiler.api.ContextPublisher;
import dec.core.compiler.api.PublicationResult;
import dec.core.compiler.api.PublicationStatus;
import dec.core.compiler.source.SourceReference;
import dec.core.context.EngineContext;
import dec.core.context.config.manager.ConfigManager;
import dec.core.context.config.model.config.Config;
import dec.core.context.config.model.config.ConfigInfo;
import dec.core.context.config.model.datasource.DataSourceConfigInfo;
import dec.core.starter.CompilerBootstrap;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

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
    private static final String CONFIG_SCHEMA_VERSION = "1.0";
    private static final String CONFIG_OPTIONS_VERSION = "config-util-p2";
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
     * 加载完整 XML/YAML 配置。现代 XML 会继续编译 System、Business，
     * 旧格式保持兼容；P2 尚不支持的现代 YAML 会在安装前明确失败。
     */
    public static synchronized void parseConfigInfo(String filePath)
            throws XMLParseException {
        ConfigInfo candidate = candidate();
        try {
            String checkedPath = requireText(filePath, "配置文件路径");
            if (isYaml(checkedPath)) {
                parseYaml(candidate, checkedPath);
                return;
            }
            parseXml(candidate, checkedPath);
        } finally {
            ConfigManager.getInstance().clearLoadingConfigInfo();
        }
    }

    /** XML 保持旧配置兼容，并为现代配置继续执行 Compiler。 */
    private static void parseXml(ConfigInfo candidate, String filePath)
            throws XMLParseException {
        ConfigFileParser parser = new ConfigFileParser();
        boolean requiresCompiler = parser.requiresCompiler(filePath);
        ConfigInfo parsed = parser.parseInto(candidate, filePath);
        if (requiresCompiler) {
            compileAndInstall(parsed, filePath);
        } else {
            // P2 保留旧配置入口到 P7；旧格式没有可绑定的 EngineContext。
            ConfigManager.getInstance().install(parsed);
        }
    }

    /**
     * YAML 在 P2 支持旧 Data/View/Rule/Connection 配置。
     * System/Business Source Graph 的格式对等属于 P8，必须在安装前明确失败。
     */
    private static void parseYaml(ConfigInfo candidate, String filePath)
            throws XMLParseException {
        try {
            YamlConfigFileParser parser = new YamlConfigFileParser();
            if (parser.requiresCompiler(filePath)) {
                throw new YAMLParseException(
                        "P2不支持YAML中的System/Business编译；该格式对等能力属于P8: "
                                + filePath);
            }
            ConfigInfo parsed = parser.parseInto(candidate, filePath);
            ConfigManager.getInstance().install(parsed);
        } catch (YAMLParseException e) {
            throw new XMLParseException(e);
        }
    }

    /** 统一门面通过扩展名选择 YAML，其余路径保持 XML 兼容。 */
    private static boolean isYaml(String filePath) {
        String lower = filePath.toLowerCase(Locale.ROOT);
        return lower.endsWith(".yaml") || lower.endsWith(".yml");
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
            ConfigManager.getInstance().install(parsed);
        } finally {
            ConfigManager.getInstance().clearLoadingConfigInfo();
        }
    }

    private static ConfigInfo candidate() {
        return ConfigManager.getInstance().getOrCreateLoadingConfigInfo();
    }

    /**
     * 编译现代配置并整体安装候选对象。编译失败结果不包含可发布模型，
     * 因而不会替换 ConfigManager 中仍在使用的旧配置。
     */
    private static void compileAndInstall(ConfigInfo candidate, String filePath)
            throws XMLParseException {
        try {
            CompilationResult result = CompilerBootstrap.builder()
                    .allowedRoot(classpathRoot(filePath))
                    .publisher(acceptPublication())
                    .build()
                    .compileAndInstall(
                            candidate,
                            new SourceReference(filePath),
                            new CompilationOptions(
                                    CONFIG_SCHEMA_VERSION,
                                    CONFIG_OPTIONS_VERSION),
                            Optional.<EngineContext>empty());
            if (result.status() != CompilationStatus.PUBLISHED) {
                throw new XMLParseException(
                        "配置编译失败: " + result.diagnostics());
            }
        } catch (XMLParseException e) {
            throw e;
        } catch (Exception e) {
            throw new XMLParseException(e);
        }
    }

    /** 从 classpath 根配置路径推导 Compiler 允许访问的最小目录。 */
    private static String classpathRoot(String filePath) throws XMLParseException {
        if (!filePath.startsWith("classpath:")) {
            throw new XMLParseException(
                    "包含 System 或 Business 的配置必须使用 classpath 路径: " + filePath);
        }
        int separator = filePath.lastIndexOf('/');
        if (separator < "classpath:".length()) {
            return "classpath:/";
        }
        return filePath.substring(0, separator + 1);
    }

    /**
     * CompilerStarter 在返回 PUBLISHED 后负责把候选 ConfigInfo 与 EngineContext 整体安装。
     * 这里仅确认当前单次配置加载允许进入该安装步骤。
     */
    private static ContextPublisher acceptPublication() {
        return new ContextPublisher() {
            @Override
            public PublicationResult publish(
                    Optional<EngineContext> expectedCurrent,
                    EngineContext candidate) {
                return new PublicationResult() {
                    @Override
                    public PublicationStatus status() {
                        return PublicationStatus.PUBLISHED;
                    }
                };
            }
        };
    }

    private static String requireText(String value, String label) {
        String checked = Objects.requireNonNull(value, label + "不能为空").trim();
        if (checked.isEmpty()) {
            throw new IllegalArgumentException(label + "不能为空");
        }
        return checked;
    }
}

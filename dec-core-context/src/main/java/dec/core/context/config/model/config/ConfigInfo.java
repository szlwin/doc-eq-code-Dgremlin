package dec.core.context.config.model.config;

import dec.core.context.CoreConfigProjection;
import dec.core.context.EngineContext;
import dec.core.context.config.model.config.data.ConfigBaseData;
import dec.core.context.config.model.connection.Connection;
import dec.core.context.config.model.connection.ConnectionInfo;
import dec.core.context.config.model.data.Data;
import dec.core.context.config.model.datasource.DataSource;
import dec.core.context.config.model.datasource.DataSourceConfigInfo;
import dec.core.context.config.model.directory.DirectoryInfo;
import dec.core.context.config.model.relation.Relation;
import dec.core.context.config.model.rule.RuleViewInfo;
import dec.core.context.config.model.service.ServiceInfo;
import dec.core.context.config.model.view.ViewData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConfigInfo {

    private volatile EngineContext engineContext;

    private final List<Map<String, ConfigBaseData>> configStores;

    public static final String CONNECTION_INFO = "orm-connection-info";

    public static final String DEFAULT_CONNECTION = "default";

    public static final String DATASOURCE_INFO = "orm-datasource-info";

    public static final String DEFAULT_DATASOURCE = "default";

    public static final String DATA_FILE_INFO = "orm-data-file-info";

    public static final String RELATION_FILE_INFO = "orm-relation-file-info";

    public static final String VIEW_FILE_INFO = "orm-view-file-info";

    public static final String RULE_FILE_INFO = "orm-rule-file-info";

    public static final String DATASOURCE_CONFIG = "orm-dataSource-config";

    public static final String CONNECTION_CONFIG = "orm-connection-config";

    public static final String SERVICE_FILE_INFO = "orm-service-info";

    public static final String DIRECTORY_FILE_INFO = "orm-directory-file-info";

    private String defaultConnection;

    private String defaultDataSource;

    public ConfigInfo() {
        this.configStores = createStores();
    }

    /**
     * 复制已有配置，供重载流程在独立候选对象上继续解析。
     * 配置模型本身在加载完成后按只读数据使用，因此这里只复制索引，不重复构造模型对象。
     */
    public ConfigInfo(ConfigInfo source) {
        Objects.requireNonNull(source, "source");
        this.configStores = createStores();
        for (int type = 0; type < Config.SIZE; type++) {
            this.configStores.get(type).putAll(source.configStores.get(type));
        }
        this.defaultConnection = source.defaultConnection;
        this.defaultDataSource = source.defaultDataSource;
    }

    /** 将编译结果绑定到当前配置，业务代码仍可通过 ConfigInfo 使用统一入口。 */
    public ConfigInfo useEngineContext(EngineContext engineContext) {
        this.engineContext = Objects.requireNonNull(engineContext, "engineContext");
        return this;
    }

    /** 当前配置是否已经完成编译并绑定 EngineContext。 */
    public boolean hasEngineContext() {
        return engineContext != null;
    }

    /** 返回当前配置对应的编译上下文。 */
    public EngineContext getEngineContext() {
        if (engineContext == null) {
            throw new IllegalStateException("当前配置尚未完成编译，EngineContext 不可用");
        }
        return engineContext;
    }

    /** 返回同一 EngineContext 生成的只读兼容投影。 */
    public CoreConfigProjection getCoreConfigProjection() {
        return getEngineContext().projection();
    }

    public Data getData(String name) {
        return (Data) get(Config.DATA, name);
    }

    public void addData(Data data) {
        add(Config.DATA, data);
    }

    public void addDataList(List<Data> list) {
        for (int i = 0; i < list.size(); i++) {
            addData(list.get(i));
        }
    }

    public Relation getRelation(String name) {
        return (Relation) get(Config.RELATION, name);
    }

    public void addRelation(Relation relation) {
        add(Config.RELATION, relation);
    }

    public void addRelationList(List<Relation> list) {
        for (int i = 0; i < list.size(); i++) {
            addRelation(list.get(i));
        }
    }

    public void addViewData(ViewData viewdata) {
        add(Config.VIEWDATA, viewdata);
    }

    public void addViewDataList(List<ViewData> list) {
        for (int i = 0; i < list.size(); i++) {
            addViewData(list.get(i));
        }
    }

    public DirectoryInfo getDirectory(String name) {
        return (DirectoryInfo) get(Config.DIRECTORY_CONFIG, name);
    }

    public ViewData getViewData(String name) {
        return (ViewData) get(Config.VIEWDATA, name);
    }

    public String getDefaultConnection() {
        return defaultConnection;
    }

    public void setDefaultConnection(String defaultConnection) {
        this.defaultConnection = defaultConnection;
    }

    public String getDefaultDataSource() {
        return defaultDataSource;
    }

    public void setDefaultDataSource(String defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }

    public DataSource<?> getDataSource(String name) {
        return (DataSource<?>) get(Config.DATASOURCE, name);
    }

    public void addDataSource(DataSource dataSource) {
        add(Config.DATASOURCE, dataSource);
    }

    public Connection getConnection(String name) {
        String actualName = name == null ? defaultConnection : name;
        return (Connection) get(Config.CONNECTION, actualName);
    }

    public void addConnection(Connection connection) {
        add(Config.CONNECTION, connection);
    }

    public RuleViewInfo getRuleViewInfo(String name) {
        return (RuleViewInfo) get(Config.RULE, name);
    }

    public void addRuleViewInfo(RuleViewInfo ruleViewInfo) {
        add(Config.RULE, ruleViewInfo);
    }

    public void addRuleViewInfoList(List<RuleViewInfo> list) {
        for (int i = 0; i < list.size(); i++) {
            addRuleViewInfo(list.get(i));
        }
    }

    public ConfigBaseData get(int type, String name) {
        return store(type).get(name);
    }

    public <V extends ConfigBaseData> void add(int type, V v) {
        ConfigBaseData value = Objects.requireNonNull(v, "config value");
        store(type).put(configKey(type, value), value);
    }

    public void addList(int type, List<ConfigBaseData> list) {
        for (int i = 0; i < list.size(); i++) {
            add(type, list.get(i));
        }
    }

    private static List<Map<String, ConfigBaseData>> createStores() {
        List<Map<String, ConfigBaseData>> stores =
                new ArrayList<Map<String, ConfigBaseData>>(Config.SIZE);
        for (int type = 0; type < Config.SIZE; type++) {
            stores.add(new HashMap<String, ConfigBaseData>());
        }
        return stores;
    }

    private Map<String, ConfigBaseData> store(int type) {
        if (type < 0 || type >= Config.SIZE) {
            throw new IllegalArgumentException("未知配置类型: " + type);
        }
        return configStores.get(type);
    }

    private String configKey(int type, ConfigBaseData value) {
        String name;
        switch (type) {
            case Config.DATASOURCE:
                name = ((DataSource<?>) value).getName();
                break;
            case Config.CONNECTION:
                name = ((Connection) value).getName();
                break;
            case Config.DATA:
                name = ((Data) value).getName();
                break;
            case Config.RELATION:
                name = ((Relation) value).getName();
                break;
            case Config.VIEWDATA:
                name = ((ViewData) value).getName();
                break;
            case Config.RULE:
                name = ((RuleViewInfo) value).getName();
                break;
            case Config.SERVICE:
                ServiceInfo service = (ServiceInfo) value;
                name = service.getName() + "_" + service.getVersion();
                break;
            case Config.DIRECTORY_CONFIG:
                name = ((DirectoryInfo) value).getName();
                break;
            case Config.DATASOURCE_CONFIG:
                name = ((DataSourceConfigInfo) value).getName();
                break;
            case Config.CONNECTION_CONFIG:
                name = ((ConnectionInfo<?, ?, ?>) value).getName();
                break;
            default:
                throw new IllegalArgumentException("未知配置类型: " + type);
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("配置名称不能为空，类型: " + type);
        }
        return name;
    }
}

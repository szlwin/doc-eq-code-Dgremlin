package dec.context.parse.yaml.parse.config;

import dec.context.parse.yaml.exception.YAMLParseException;
import dec.context.parse.yaml.parse.FileParser;
import dec.context.parse.yaml.parse.YamlSupport;
import dec.context.parse.yaml.parse.data.YamlDataFileParser;
import dec.context.parse.yaml.parse.rule.YamlRuleFileParser;
import dec.context.parse.yaml.parse.view.YamlViewFileParser;
import dec.core.context.config.manager.ConfigManager;
import dec.core.context.config.model.config.Config;
import dec.core.context.config.model.config.ConfigInfo;
import dec.core.context.config.model.connection.Connection;
import dec.core.context.config.model.connection.ConnectionInfo;
import dec.core.context.config.model.data.Data;
import dec.core.context.config.model.datasource.DataSource;
import dec.core.context.config.model.rule.RuleViewInfo;
import dec.core.context.config.model.view.ViewData;
import dec.core.context.config.utils.ConfigContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class YamlConfigFileParser implements FileParser<ConfigInfo> {

    private final static Logger log = LoggerFactory.getLogger(YamlConfigFileParser.class);

    @Override
    public ConfigInfo parse(String filePath) throws YAMLParseException {
        log.info("------Dec yaml init Start------");
        ConfigInfo configInfo = ConfigManager.getInstance().getConfigInfo();
        if (configInfo == null) {
            configInfo = new ConfigInfo();
            ConfigManager.getInstance().setConfigInfo(configInfo);
        }
        Map<String, Object> root = YamlSupport.loadMap(YamlSupport.findOne(filePath));
        parseDataSources(configInfo, root);
        parseConnections(configInfo, root);
        parseDataFiles(configInfo, root);
        parseViewFiles(configInfo, root);
        parseRuleFiles(configInfo, root);
        log.info("------Dec yaml init End------");
        return configInfo;
    }

    private void parseDataSources(ConfigInfo configInfo, Map<String, Object> root) throws YAMLParseException {
        Map<String, Object> sourceInfo = section(root, "orm-datasource-info", "ormDatasourceInfo", "datasourceInfo", "dataSourceInfo");
        Object value = sourceInfo == null ? YamlSupport.first(root, "datasources", "dataSources") : YamlSupport.first(sourceInfo, "orm-datasource", "datasources", "dataSources");
        if (sourceInfo != null) {
            configInfo.setDefaultDataSource(YamlSupport.str(sourceInfo, "default"));
        }
        for (Object item : YamlSupport.list(value, "datasources")) {
            Map<String, Object> map = YamlSupport.map(item, "datasource");
            DataSource<?> dataSource = new DataSource<>();
            String name = YamlSupport.requireStr(map, "datasource.name", "name");
            if (ConfigContextUtil.getConfigInfo().get(Config.DATASOURCE, name) != null) {
                throw new YAMLParseException("The data source is existed: " + name);
            }
            dataSource.setName(name);
            String type = YamlSupport.requireStr(map, "datasource.type", "type", "nameRef", "ref");
            if (ConfigContextUtil.getConfigInfo().get(Config.DATASOURCE_CONFIG, type) == null) {
                throw new YAMLParseException("The data source type is not existed: " + type);
            }
            dataSource.setType(type);
            dataSource.setDriverClass(YamlSupport.str(map, "driverClass", "driver-class", "driver_class"));
            dataSource.setUrl(YamlSupport.str(map, "url"));
            dataSource.setUserName(YamlSupport.str(map, "username", "userName", "user_name"));
            dataSource.setPassWord(YamlSupport.str(map, "password"));
            configInfo.addDataSource(dataSource);
            log.info("Load yaml data source:{} success!", name);
        }
    }

    private void parseConnections(ConfigInfo configInfo, Map<String, Object> root) throws YAMLParseException {
        Map<String, Object> connectionInfo = section(root, "orm-connection-info", "ormConnectionInfo", "connectionInfo");
        Object value = connectionInfo == null ? YamlSupport.first(root, "connections") : YamlSupport.first(connectionInfo, "orm-connection", "connections");
        if (connectionInfo != null) {
            configInfo.setDefaultConnection(YamlSupport.str(connectionInfo, "default"));
        }
        for (Object item : YamlSupport.list(value, "connections")) {
            Map<String, Object> map = YamlSupport.map(item, "connection");
            Connection connection = new Connection();
            String name = YamlSupport.requireStr(map, "connection.name", "name");
            if (ConfigContextUtil.getConfigInfo().get(Config.CONNECTION, name) != null) {
                throw new YAMLParseException("The connection is existed: " + name);
            }
            connection.setName(name);
            for (Object ref : dataSourceRefs(map)) {
                String dataSourceName;
                if (ref instanceof Map) {
                    dataSourceName = YamlSupport.requireStr(YamlSupport.map(ref, "dataSource"), "dataSource.ref", "ref", "name");
                } else {
                    dataSourceName = String.valueOf(ref);
                }
                DataSource<?> dataSource = configInfo.getDataSource(dataSourceName);
                if (dataSource == null) {
                    throw new YAMLParseException("The data source is not existed: " + dataSourceName);
                }
                dataSource.setConName(connection.getName());
                connection.getDataSourceInfo().addDataSource(dataSource);
                ConnectionInfo info = (ConnectionInfo) ConfigContextUtil.getConfigInfo().get(Config.CONNECTION_CONFIG, dataSource.getType());
                connection.setConnectionInfo(info);
            }
            Map<String, Object> properties = YamlSupport.map(YamlSupport.first(map, "properties", "property-info", "propertyInfo"), "properties");
            if (properties != null) {
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    connection.getPropertyInfo().put(entry.getKey(), entry.getValue() == null ? null : String.valueOf(entry.getValue()));
                }
            }
            configInfo.addConnection(connection);
            log.info("Load yaml connection:{} success!", name);
        }
    }

    private void parseDataFiles(ConfigInfo configInfo, Map<String, Object> root) throws YAMLParseException {
        for (String path : filePaths(root, ConfigInfo.DATA_FILE_INFO, "dataFiles")) {
            List<Data> dataList = new YamlDataFileParser().parse(path);
            for (Data data : dataList) {
                configInfo.addData(data);
            }
        }
    }

    private void parseViewFiles(ConfigInfo configInfo, Map<String, Object> root) throws YAMLParseException {
        for (String path : filePaths(root, ConfigInfo.VIEW_FILE_INFO, "viewFiles")) {
            List<ViewData> viewDataList = new YamlViewFileParser().parse(path);
            for (ViewData viewData : viewDataList) {
                configInfo.addViewData(viewData);
            }
        }
    }

    private void parseRuleFiles(ConfigInfo configInfo, Map<String, Object> root) throws YAMLParseException {
        for (String path : filePaths(root, ConfigInfo.RULE_FILE_INFO, "ruleFiles")) {
            List<RuleViewInfo> ruleViewInfoList = new YamlRuleFileParser().parse(path);
            for (RuleViewInfo ruleViewInfo : ruleViewInfoList) {
                configInfo.addRuleViewInfo(ruleViewInfo);
            }
        }
    }

    private Map<String, Object> section(Map<String, Object> root, String... names) throws YAMLParseException {
        return YamlSupport.map(YamlSupport.first(root, names), names[0]);
    }

    private List<Object> dataSourceRefs(Map<String, Object> map) throws YAMLParseException {
        Object refs = YamlSupport.first(map, "dataSources", "data-source-info", "dataSourceInfo");
        if (refs instanceof Map) {
            Object dataSource = YamlSupport.first(YamlSupport.map(refs, "dataSourceInfo"), "data-source", "dataSources", "dataSource");
            return YamlSupport.list(dataSource, "dataSources");
        }
        return YamlSupport.list(refs, "dataSources");
    }

    private List<String> filePaths(Map<String, Object> root, String xmlName, String alias) throws YAMLParseException {
        Object section = YamlSupport.first(root, xmlName, alias);
        Object files = section;
        if (section instanceof Map) {
            files = YamlSupport.first(YamlSupport.map(section, alias), "orm-file", "files", "paths", alias);
        }
        java.util.ArrayList<String> paths = new java.util.ArrayList<>();
        for (Object item : YamlSupport.list(files, alias)) {
            if (item instanceof Map) {
                paths.add(YamlSupport.requireStr(YamlSupport.map(item, alias), alias + ".path", "path"));
            } else {
                paths.add(String.valueOf(item));
            }
        }
        return paths;
    }
}

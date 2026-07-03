package dec.context.parse.yaml.parse.data;

import dec.context.parse.yaml.exception.YAMLParseException;
import dec.context.parse.yaml.parse.FileParser;
import dec.context.parse.yaml.parse.YamlResource;
import dec.context.parse.yaml.parse.YamlSupport;
import dec.core.context.config.model.config.Config;
import dec.core.context.config.model.data.Column;
import dec.core.context.config.model.data.Data;
import dec.core.context.config.model.data.DataProperty;
import dec.core.context.config.model.data.DataTable;
import dec.core.context.config.model.data.PropertyInfo;
import dec.core.context.config.model.data.TableInfo;
import dec.core.context.config.model.datasource.config.DataSourceConfig;
import dec.core.context.config.utils.ConfigContextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class YamlDataFileParser implements FileParser<List<Data>> {

    @Override
    public List<Data> parse(String filePath) throws YAMLParseException {
        List<Data> dataList = new ArrayList<>();
        for (YamlResource resource : YamlSupport.findAll(filePath)) {
            Map<String, Object> root = YamlSupport.loadMap(resource);
            for (Object item : roots(root)) {
                dataList.add(parseData(YamlSupport.map(item, "data")));
            }
        }
        return dataList;
    }

    public Data parseData(Map<String, Object> node) throws YAMLParseException {
        Data data = new Data();
        String name = YamlSupport.requireStr(node, "data.name", "name");
        if (ConfigContextUtil.getConfigInfo().get(Config.DATA, name) != null) {
            throw new YAMLParseException("The data is existed: " + name);
        }
        data.setName(name);
        data.setClassName(YamlSupport.str(node, "class", "className"));
        data.setPropertyInfo(parseProperties(YamlSupport.first(node, "properties", "property-info", "propertyInfo")));
        data.setTableInfo(parseTables(data.getPropertyInfo(), YamlSupport.first(node, "tables", "table-info", "tableInfo")));
        return data;
    }

    private PropertyInfo parseProperties(Object value) throws YAMLParseException {
        PropertyInfo propertyInfo = new PropertyInfo();
        if (value instanceof Map) {
            Map<String, Object> map = YamlSupport.map(value, "properties");
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                DataProperty property = new DataProperty();
                property.setName(entry.getKey());
                if (entry.getValue() instanceof Map) {
                    property.setType(YamlSupport.requireStr(YamlSupport.map(entry.getValue(), entry.getKey()), entry.getKey() + ".type", "type"));
                } else {
                    property.setType(String.valueOf(entry.getValue()));
                }
                propertyInfo.addProperty(property);
            }
            return propertyInfo;
        }
        for (Object item : YamlSupport.list(value, "properties")) {
            Map<String, Object> map = YamlSupport.map(item, "property");
            DataProperty property = new DataProperty();
            property.setName(YamlSupport.requireStr(map, "property.name", "name"));
            property.setType(YamlSupport.requireStr(map, "property.type", "type"));
            propertyInfo.addProperty(property);
        }
        return propertyInfo;
    }

    private TableInfo parseTables(PropertyInfo propertyInfo, Object value) throws YAMLParseException {
        TableInfo tableInfo = new TableInfo();
        for (Object item : YamlSupport.list(value, "tables")) {
            Map<String, Object> map = YamlSupport.map(item, "table");
            DataTable table = new DataTable();
            table.setName(YamlSupport.requireStr(map, "table.name", "name"));
            String dataSourceName = YamlSupport.requireStr(map, "table.dataSource", "dataSource", "data-source", "data_source");
            if (DataSourceConfig.getInstance().get(dataSourceName) == null) {
                throw new YAMLParseException("The data source is not existed: " + dataSourceName);
            }
            table.setDataSourceName(dataSourceName);
            table.setKey(YamlSupport.requireStr(map, "table.key", "key"));
            table.setKeyType(YamlSupport.str(map, "keyType", "key-type", "key_type"));
            table.setSeq(YamlSupport.str(map, "seq"));
            table.setCon(YamlSupport.str(map, "con"));
            parseColumns(propertyInfo, table, YamlSupport.first(map, "columns", "column"));
            tableInfo.addTable(table);
        }
        return tableInfo;
    }

    private void parseColumns(PropertyInfo propertyInfo, DataTable table, Object value) throws YAMLParseException {
        if (value instanceof Map) {
            Map<String, Object> map = YamlSupport.map(value, "columns");
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Column column = parseColumn(propertyInfo, entry.getKey(), entry.getValue());
                if (entry.getKey().equals(table.getKey())) {
                    table.setPropertyKey(column.getRefproperty());
                }
                table.addColumn(column);
            }
            return;
        }
        for (Object item : YamlSupport.list(value, "columns")) {
            Map<String, Object> map = YamlSupport.map(item, "column");
            Column column = parseColumn(propertyInfo, YamlSupport.requireStr(map, "column.name", "name"), map);
            if (column.getName().equals(table.getKey())) {
                table.setPropertyKey(column.getRefproperty());
            }
            table.addColumn(column);
        }
    }

    private Column parseColumn(PropertyInfo propertyInfo, String name, Object value) throws YAMLParseException {
        Column column = new Column();
        column.setName(name);
        if (value instanceof Map) {
            Map<String, Object> map = YamlSupport.map(value, "column");
            column.setRefproperty(YamlSupport.requireStr(map, "column.ref", "ref", "refProperty", "ref-property", "ref_property"));
            column.setType(YamlSupport.str(map, "type"));
        } else {
            column.setRefproperty(String.valueOf(value));
        }
        if (propertyInfo.getProperty(column.getRefproperty()) == null) {
            throw new YAMLParseException("The ref property is not existed: " + column.getRefproperty());
        }
        if (column.getType() != null && !"".equals(column.getType())) {
            String originType = propertyInfo.getProperty(column.getRefproperty()).getType();
            column.setOriginType(originType);
            column.setConvertFun(originType + "_" + column.getType());
        }
        return column;
    }

    private List<Object> roots(Map<String, Object> root) throws YAMLParseException {
        Object datas = YamlSupport.first(root, "datas", "dataList");
        if (datas != null) {
            return YamlSupport.list(datas, "datas");
        }
        Object data = YamlSupport.first(root, "data");
        if (data != null) {
            return YamlSupport.list(data, "data");
        }
        Object mapping = YamlSupport.first(root, "orm-data-mapping", "ormDataMapping");
        if (mapping != null) {
            return YamlSupport.list(YamlSupport.first(YamlSupport.map(mapping, "orm-data-mapping"), "data"), "data");
        }
        return YamlSupport.list(root, "data");
    }
}

package dec.context.parse.yaml.parse.view;

import dec.context.parse.yaml.exception.YAMLParseException;
import dec.context.parse.yaml.parse.FileParser;
import dec.context.parse.yaml.parse.YamlResource;
import dec.context.parse.yaml.parse.YamlSupport;
import dec.core.context.config.model.config.Config;
import dec.core.context.config.model.config.ConfigConstanst;
import dec.core.context.config.model.config.ConfigInfo;
import dec.core.context.config.model.data.Data;
import dec.core.context.config.model.data.DataProperty;
import dec.core.context.config.model.relation.ManyRelation;
import dec.core.context.config.model.relation.OneRelation;
import dec.core.context.config.model.relation.Relation;
import dec.core.context.config.model.view.RelationInfo;
import dec.core.context.config.model.view.ViewData;
import dec.core.context.config.model.view.ViewProperty;
import dec.core.context.config.model.view.ViewPropertyInfo;
import dec.core.context.config.utils.ConfigContextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class YamlViewFileParser implements FileParser<List<ViewData>> {

    @Override
    public List<ViewData> parse(String filePath) throws YAMLParseException {
        List<ViewData> viewList = new ArrayList<>();
        for (YamlResource resource : YamlSupport.findAll(filePath)) {
            Map<String, Object> root = YamlSupport.loadMap(resource);
            for (Object item : roots(root)) {
                viewList.add(parseView(YamlSupport.map(item, "view")));
            }
        }
        return viewList;
    }

    public ViewData parseView(Map<String, Object> node) throws YAMLParseException {
        ViewData viewData = new ViewData();
        String name = YamlSupport.requireStr(node, "view.name", "name");
        if (ConfigContextUtil.getConfigInfo().get(Config.VIEWDATA, name) != null) {
            throw new YAMLParseException("The view is existed: " + name);
        }
        viewData.setName(name);
        viewData.setClassName(YamlSupport.str(node, "class", "className"));
        String targetMain = YamlSupport.requireStr(node, "view.targetMain", "targetMain", "target-main", "target_main");
        Data data = ConfigContextUtil.getConfigInfo().getData(targetMain);
        if (data == null) {
            throw new YAMLParseException("The data is not existed: " + targetMain);
        }
        viewData.setTargetMain(data);
        viewData.setViewPropertyInfo(parseProperties(viewData, YamlSupport.first(node, "properties", "property-info", "propertyInfo")));
        return viewData;
    }

    private ViewPropertyInfo parseProperties(ViewData viewData, Object value) throws YAMLParseException {
        ViewPropertyInfo propertyInfo = new ViewPropertyInfo();
        if (value instanceof Map) {
            Map<String, Object> map = YamlSupport.map(value, "properties");
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                propertyInfo.addProperty(parseProperty(viewData, entry.getKey(), entry.getValue()));
            }
            return propertyInfo;
        }
        for (Object item : YamlSupport.list(value, "properties")) {
            Map<String, Object> map = YamlSupport.map(item, "property");
            propertyInfo.addProperty(parseProperty(viewData, YamlSupport.requireStr(map, "property.name", "name"), map));
        }
        return propertyInfo;
    }

    private ViewProperty parseProperty(ViewData viewData, String name, Object value) throws YAMLParseException {
        ViewProperty property = new ViewProperty();
        property.setName(name);
        if (!(value instanceof Map)) {
            setRefProperty(viewData, property, String.valueOf(value));
            return property;
        }

        Map<String, Object> map = YamlSupport.map(value, "property");
        String relation = YamlSupport.str(map, "relation");
        if (relation == null || "".equals(relation)) {
            setRefProperty(viewData, property, YamlSupport.requireStr(map, "property.ref", "ref", "refProperty", "ref-property", "ref_property"));
            return property;
        }

        ConfigInfo configInfo = ConfigContextUtil.getConfigInfo();
        String dataName = YamlSupport.requireStr(map, "property.data", "data");
        Data data = configInfo.getData(dataName);
        if (data == null) {
            throw new YAMLParseException("The relation data is not existed: " + dataName);
        }

        RelationInfo relationInfo = viewData.getRelationInfo();
        if (relationInfo == null) {
            relationInfo = new RelationInfo();
            viewData.setRelationInfo(relationInfo);
        }

        ViewData subViewData = new ViewData();
        subViewData.setName(name);
        subViewData.setTargetMain(data);
        subViewData.setParentView(viewData);

        Relation relationObj = createRelation(viewData, map, relation);
        relationObj.setViewProperty(property);

        property.setViewData(subViewData);
        property.setRelation(relationObj);
        relationInfo.addRelation1(data.getName(), relationObj);

        subViewData.setViewPropertyInfo(parseProperties(subViewData, YamlSupport.first(map, "properties", "property-info", "propertyInfo")));
        return property;
    }

    private void setRefProperty(ViewData viewData, ViewProperty property, String refProperty) throws YAMLParseException {
        Map<String, DataProperty> refProMap = viewData.getTargetMain().getPropertyInfo().getProperty();
        if (!refProMap.containsKey(refProperty)) {
            throw new YAMLParseException("The ref property is not existed in " + viewData.getTargetMain().getName() + ": " + refProperty);
        }
        property.setRefProperty(refProperty);
    }

    private Relation createRelation(ViewData view, Map<String, Object> map, String relation) throws YAMLParseException {
        if (ConfigConstanst.RELATION_TYPE_ONE_TO_ONE.equals(relation)) {
            OneRelation relationObj = new OneRelation();
            relationObj.setType(ConfigConstanst.RELATION_TYPE_ONE_TO_ONE);
            relationObj.setOneKey(YamlSupport.requireStr(map, "relation.key", "key"));
            relationObj.setOneRef(YamlSupport.requireStr(map, "relation.data", "data"));
            relationObj.setOneMainkey(YamlSupport.requireStr(map, "relation.relKey", "relKey", "rel-key", "rel_key"));
            relationObj.setOneMainRef(view.getTargetMain().getName());
            return relationObj;
        }
        if (ConfigConstanst.RELATION_TYPE_ONE_TO_MANY.equals(relation)) {
            ManyRelation relationObj = new ManyRelation();
            relationObj.setType(ConfigConstanst.RELATION_TYPE_ONE_TO_MANY);
            relationObj.setOneKey(YamlSupport.requireStr(map, "relation.relKey", "relKey", "rel-key", "rel_key"));
            relationObj.setOneRef(view.getTargetMain().getName());
            relationObj.setManyKey(YamlSupport.requireStr(map, "relation.key", "key"));
            relationObj.setManyRef(YamlSupport.requireStr(map, "relation.data", "data"));
            return relationObj;
        }
        throw new YAMLParseException("The relation type is not supported: " + relation);
    }

    private List<Object> roots(Map<String, Object> root) throws YAMLParseException {
        Object views = YamlSupport.first(root, "views", "viewList");
        if (views != null) {
            return YamlSupport.list(views, "views");
        }
        Object view = YamlSupport.first(root, "view");
        if (view != null) {
            return YamlSupport.list(view, "view");
        }
        Object mapping = YamlSupport.first(root, "orm-view-mapping", "ormViewMapping");
        if (mapping != null) {
            return YamlSupport.list(YamlSupport.first(YamlSupport.map(mapping, "orm-view-mapping"), "view"), "view");
        }
        return YamlSupport.list(root, "view");
    }
}

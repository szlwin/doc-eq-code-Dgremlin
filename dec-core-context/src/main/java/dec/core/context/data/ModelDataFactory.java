package dec.core.context.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import dec.core.context.collections.list.SimpleList;
import dec.core.context.config.exception.DataNotDefineException;
import dec.core.context.config.model.config.ConfigConstanst;
import dec.core.context.config.model.config.ConfigInfo;
import dec.core.context.config.model.view.ViewData;
import dec.core.context.config.model.view.ViewProperty;
import dec.core.context.config.utils.ConfigContextUtil;
import dec.core.context.model.CompiledMaterializationNode;
import dec.core.context.model.CompiledViewMaterializationPlan;
import javolution.util.FastMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class ModelDataFactory {

    private static final ModelDataFactory viewDataFactory = new ModelDataFactory();

    private ModelDataFactory() {

    }

    public static ModelDataFactory getInstance() {
        return viewDataFactory;
    }

    public ModelData createData(String name) throws DataNotDefineException {
        return createData(name, null);
    }

    /**
     * 按指定物化计划创建局部 ModelData，保留给编译器内部和兼容调用。
     * 普通业务创建必须使用 ViewData 全量字段，不能把访问计划误当成完整模型定义。
     */
    @SuppressWarnings("unchecked")
    public ModelData createData(
            CompiledViewMaterializationPlan plan,
            Object originObject) throws DataNotDefineException {
        Objects.requireNonNull(plan, "plan");
        if (originObject instanceof ModelData) {
            throw new DataNotDefineException("originObject must not be ModelData");
        }

        ModelData modelData = new ModelData();
        modelData.setName(plan.viewKey().name());
        // ModelContainer 会把执行结果写回原对象，因此根 Map 需要保留 FastJSON 的对象语义。
        Map<String, Object> values = new JSONObject();
        for (CompiledMaterializationNode node : plan.fields()) {
            materializePath(values, node.path().segments());
        }

        if (originObject != null) {
            try {
                Object json = JSONObject.toJSON(originObject);
                if (json instanceof Map) {
                    mergeKnownValues(values, (Map<String, Object>) json);
                } else {
                    throw new IllegalArgumentException("originObject is not object-like");
                }
                if (!(originObject instanceof Map)) {
                    modelData.setOriginData(originObject);
                }
            } catch (RuntimeException ex) {
                throw new DataNotDefineException(
                        "originObject cannot be materialized for view " + plan.viewKey().name());
            }
        }
        modelData.setValues(values);
        return modelData;
    }

    /** 按物化计划建立嵌套字段骨架。 */
    @SuppressWarnings("unchecked")
    private static void materializePath(Map<String, Object> root, List<String> segments) {
        Map<String, Object> cursor = root;
        for (int index = 0; index < segments.size(); index++) {
            String segment = segments.get(index);
            boolean leaf = index == segments.size() - 1;
            if (leaf) {
                if (!cursor.containsKey(segment)) {
                    cursor.put(segment, null);
                }
                return;
            }
            Object current = cursor.get(segment);
            if (current == null) {
                // 嵌套对象也使用 JSONObject，保证结果可以递归写回业务对象。
                Map<String, Object> child = new JSONObject();
                cursor.put(segment, child);
                cursor = child;
            } else if (current instanceof Map) {
                cursor = (Map<String, Object>) current;
            } else {
                throw new IllegalArgumentException(
                        "compiled materialization path crosses non-object segment: " + segment);
            }
        }
    }

    /** 只把来源对象中已声明的字段合并到物化结果。 */
    @SuppressWarnings("unchecked")
    private static void mergeKnownValues(Map<String, Object> target, Map<String, Object> source) {
        for (Map.Entry<String, Object> entry : target.entrySet()) {
            if (!source.containsKey(entry.getKey())) {
                continue;
            }
            Object expected = entry.getValue();
            Object actual = source.get(entry.getKey());
            if (expected instanceof Map && actual instanceof Map) {
                mergeKnownValues((Map<String, Object>) expected, (Map<String, Object>) actual);
            } else {
                entry.setValue(actual);
            }
        }
    }

    public ModelData createData(String name, Object object) throws DataNotDefineException {
        ConfigInfo configInfo = ConfigContextUtil.getConfigInfo();
        ModelData baseData = new ModelData();
        ViewData viewDataConfig = configInfo.getViewData(name);

        if (viewDataConfig == null)
            throw new DataNotDefineException("The view data:" + name + " is not defined!");

        baseData.setName(name);

        // ViewData 是业务模型的完整定义，访问计划只负责编译期校验，不能在这里裁剪字段。
        Map<String, ViewProperty> map
                = viewDataConfig.getViewPropertyInfo().getProperty();

        baseData.setViewInfo(viewDataConfig);

        convert(baseData.getAllValues(), map);

        if (object != null) {
            if (object instanceof Map) {
                baseData.setValues((Map)object);
            }else{
                baseData.setOriginData(object);
                baseData.setValues((Map)JSONObject.toJSON(object));
            }
        }
        //RelationInfo relationInfo = viewDataConfig.getRelationInfo();

        //addRelationInfo(baseData,relationInfo);

        return baseData;
    }
	/*
	private void addRelationInfo(ModelData data,RelationInfo relationInfo){
		Collection<RelationView> rViewCollection = relationInfo.getRelation();
		Iterator<RelationView> it = rViewCollection.iterator();
		while(it.hasNext()){
			RelationView rView = it.next();
			addRelationView(data,rView);
		}
	}
	
	private void addRelationView(ModelData data,RelationView rView){
		
		if(rView.getRef().getType().equals(Constanst.RELATION_TYPE_ONE_TO_MANY)){
			//data.addKey(rView.getRelationProperty());
			data.addData(rView.getRelationProperty(), new ArrayList<Object>(20));
			return;
		}
		
		Collection<RelationProperty> rProCollection = rView.getAll();
		
		
		Map<String,Object> map = new HashMap<String,Object>();
		
		Iterator<RelationProperty> it = rProCollection.iterator();
		
		while(it.hasNext()){
			RelationProperty rProperty = it.next();
			map.put(rProperty.getName(), null);
		}
		
		data.addData(rView.getRelationProperty(), map);
		
	}*/

    private void convert(Map<String, Object> viewMap, Map<String, ViewProperty> map) {
        Set<String> keySet = map.keySet();
        Iterator<String> it = keySet.iterator();
        while (it.hasNext()) {
            String key = it.next();
            ViewProperty viewProperty = map.get(key);
            if (viewProperty.getRelation() != null) {
                if (viewProperty.getRelation().getType().equals(ConfigConstanst.RELATION_TYPE_ONE_TO_MANY)) {
                    viewMap.put(key, new SimpleList<Object>());
                } else {
                    Map<String, Object> subViewMap = new FastMap<String, Object>();
                    viewMap.put(key, subViewMap);
                    convert(subViewMap, viewProperty.getViewData().getViewPropertyInfo().getProperty());
                }
            } else {
                viewMap.put(key, null);
            }
        }
    }
}

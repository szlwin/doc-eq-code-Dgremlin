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
     * 根据编译期冻结的物化计划创建 ModelData，不再读取全局 ConfigContext。
     *
     * <p>DEV-04 / AC-P2-SYSTEM-RULEVIEW-008：运行时只能消费当前 EngineContext 已发布的
     * {@link CompiledViewMaterializationPlan}。这里按 plan 中的精确 ModelPath 建立字段骨架，
     * 然后把真实 originObject 映射到同一个 ModelData；后续 DEV-05 必须把这个实例原样冻结到 Handle。
     *
     * @param plan 当前捕获 Context 中的精确物化计划
     * @param originObject 真实业务对象或 Map；不得传入已经构造好的 ModelData 作为 trusted 输入
     * @return 与 plan 精确对应的新 ModelData
     * @throws DataNotDefineException originObject 无法转换为对象字段时抛出
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
        // legacy ModelContainer 的成功写回路径要求 values 同时实现 FastJSON JSON；因此根对象必须用 JSONObject。
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

    /** 按编译期精确路径建立嵌套对象骨架；禁止运行时重新解释 selector。 */
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
                // 嵌套对象同样保持 JSON Map 语义，保证真实 originData 写回时可以安全递归转换。
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

    /** 只覆盖 plan 已声明的字段，避免 originObject 注入未编译字段。 */
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
        ModelData baseData = new ModelData();

        ConfigInfo configInfo = ConfigContextUtil.getConfigInfo();
        ViewData viewDataConfig = configInfo.getViewData(name);

        if (viewDataConfig == null)
            throw new DataNotDefineException("The view data:" + name + " is not defined!");

        baseData.setName(name);

        //��ȡ����������Ϣ
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

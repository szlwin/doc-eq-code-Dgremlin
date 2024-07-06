package dec.core.model.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import dec.core.context.config.exception.DataNotDefineException;
import dec.core.context.config.manager.ConfigManager;
import dec.core.context.config.model.config.ConfigInfo;
import dec.core.context.config.model.data.Data;
import dec.core.context.config.model.relation.Relation;
import dec.core.context.config.model.rule.RuleViewInfo;
import dec.core.context.config.model.view.ViewData;
import dec.core.context.config.model.view.ViewProperty;
import dec.core.context.data.BaseData;
import dec.core.context.data.BaseDataFactory;
import dec.core.context.data.ModelData;
import dec.core.context.data.ModelDataFactory;
import dec.core.context.data.NullData;
import dec.core.model.data.DataConvert;

/*import com.orm.common.xml.exception.DataNotDefineException;
import com.orm.common.xml.model.config.ConfigInfo;
import com.orm.common.xml.model.data.Data;
import com.orm.common.xml.model.relation.OneRelation;
import com.orm.common.xml.model.relation.Relation;
import com.orm.common.xml.model.rule.RuleViewInfo;
import com.orm.common.xml.model.view.RelationInfo;
import com.orm.common.xml.model.view.RelationProperty;
import com.orm.common.xml.model.view.RelationView;
import com.orm.common.xml.model.view.ViewData;
import com.orm.common.xml.model.view.ViewProperty;
import com.orm.common.xml.util.ConfigManager;*/

public class DataUtil {

	public static ModelData createViewData(String name) throws DataNotDefineException{
		return ModelDataFactory.getInstance().createData(name);
	}

	public static ModelData createViewData(String name, Object object) throws DataNotDefineException{
		return ModelDataFactory.getInstance().createData(name, object);
	}
	
	public static BaseData createBaseData(String name) throws DataNotDefineException{
		return BaseDataFactory.getInstance().createData(name);
	}
	
	public static BaseData createBaseData(Data dataInfo) throws DataNotDefineException{
		return BaseDataFactory.getInstance().createData(dataInfo);
	}
	
	public static void addDataToView(ModelData viewBaseData,BaseData baseData){
		
		//DataConvert dataConvert = new DataConvert();
		
		DataConvert.convert(viewBaseData, baseData);
	}
	
	public static void addDataToView(String name,ModelData viewBaseData,BaseData baseData){
		
		//DataConvert dataConvert = new DataConvert();
		
		DataConvert.convert(name,viewBaseData, baseData);
	}
	
	public static void convertDataToBaseData(Map<String,Object> dataMap,Map<String,Object> dataObjValue,Iterator<String> keyIt){
		while(keyIt.hasNext()){
			String key = keyIt.next();
			String lowKey = key.toLowerCase();
			if(dataMap.containsKey(lowKey)){
				dataObjValue.put(key,dataMap.get(lowKey));
			}
		}
	}
	
	public static void convertDataToBaseData(Map<String,Object> dataMap,Map<String,Object> dataObjValue){
		Iterator<String> keyIt = dataObjValue.keySet().iterator();
		
		convertDataToBaseData(dataMap,dataObjValue,keyIt);
		
	}
	
	public static void convertDataToBaseData(Map<String,Object> paramMap,BaseData data){
		
		Map<String,Object> dataMap = paramMap;
		Map<String,Object> dataObjValue = data.getValues();
		
		convertDataToBaseData(dataMap,dataObjValue);

	}
	
	public static void convertDataToBaseData(Map<String,Object> paramMap,BaseData data,ViewData view){
		
		Collection<ViewProperty> proColection = view.getViewPropertyInfo().getProperty().values();
		
		Iterator<ViewProperty> it = proColection.iterator();
		
		while(it.hasNext()){
			ViewProperty pro = it.next();
			if(pro.getRelation() == null)
				data.setValue(pro.getRefProperty(), paramMap.get(pro.getName()));
		}
	}
	/*
	public static void convertDataToBaseData(Map<String,Object> paramMap,BaseData data,RelationView relationView){
		Collection<RelationProperty> proColection = relationView.getAll();
		
		Iterator<RelationProperty> it = proColection.iterator();

		while(it.hasNext()){
			RelationProperty pro = it.next();
			data.setValue(pro.getRefProperty(), paramMap.get(pro.getName()));
		}
	}*/
	
	public static void convertDataToBaseData(Map<String,Object> paramMap,BaseData data,Relation relation){
		Collection<ViewProperty> proColection = relation.getViewProperty()
				.getViewData()
				.getViewPropertyInfo()
				.getProperty().values();
		
		Iterator<ViewProperty> it = proColection.iterator();

		while(it.hasNext()){
			ViewProperty pro = it.next();
			data.setValue(pro.getRefProperty(), paramMap.get(pro.getName()));
		}
	}

	public static Object getValueByKey(String key,Object obj){
		if(obj instanceof Map)
			return getValueByKey(key,(Map<String,Object>)obj);
		
		if(obj instanceof ModelData)
			return getValueByKey(key,((ModelData)obj).getValues());
		
		return null;
	}

	public static Object getValueByKey(String key,Map<String,Object> map){
		String keyArray[] = key.split("\\.");
		
		if(keyArray.length == 1){
			if(map.containsKey(key)){
				Object value = map.get(key);
				if(value == null)
					return new NullData();
				else
					return value;
				
			}else{
				return null;
			}
		}
			
		Map<String,Object> valueMap = map;
		for(int i = 0; i < keyArray.length-1; i++){
			if(valueMap.containsKey(keyArray[i]))
				valueMap = (Map<String,Object>)valueMap.get(keyArray[i]);
		}
		
		if(valueMap.containsKey(keyArray[keyArray.length-1])){
			Object value = valueMap.get(keyArray[keyArray.length-1]);
			if(value == null)
				return new NullData();
			else
				return value;
		}
		else
			return null;
	}
	
	public static Map<String,Object> getModelValues(ModelData modelData){
		return modelData.getValues();
	}
	
	public static RuleViewInfo getRuleViewInfo(String name){
		ConfigInfo configInfo = DataUtil.getConfigInfo();
		
		RuleViewInfo ruleViewInfo = configInfo.getRuleViewInfo(name);
		return ruleViewInfo;
	}
	
	public static ConfigInfo getConfigInfo(){
		return ConfigManager.getInstance().getConfigInfo();
	}
	
	public static ViewData getViewInfo(ModelData modelData){
		return modelData.getViewInfo();
	}
	
	public static Map<String,Object> getValues(BaseData data){
		if(data == null)
			return null;
		return data.getValues();
	}
}

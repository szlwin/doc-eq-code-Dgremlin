package dec.core.model.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import dec.core.context.config.model.data.Column;
import dec.core.context.config.model.data.Data;
import dec.core.context.config.model.data.DataTable;
import dec.core.context.config.model.relation.OneRelation;
import dec.core.context.config.model.view.RelationInfo;
import dec.core.context.config.model.view.ViewData;
import dec.core.context.config.model.view.ViewProperty;
import dec.core.context.data.BaseData;
import dec.core.context.data.ModelData;
import dec.core.context.data.NullData;
import dec.external.datasource.sql.datatype.convert.DataTypeConvert;
import javolution.util.FastMap;

public class DataConvert {
	
	public static void convert(String name,ModelData viewBaseData,BaseData baseData){
		Map<String,Object> viewMap = viewBaseData.getAllValues();
		String proArray[] = name.split("\\.");
		
		if(proArray.length == 1){
			Object obj = viewMap.get(proArray[0]);
			Object superObj = viewMap;
			ViewProperty viewProperty = viewBaseData.getViewInfo().getViewPropertyInfo().getProperty().get(proArray[0]);
			convert(superObj,obj,viewProperty,baseData);
			
			return;
		}
		
		Map<String,Object> dataMap = (Map<String, Object>) viewMap.get(proArray[0]);
		
		ViewProperty viewProperty = viewBaseData.getViewInfo().getViewPropertyInfo().getProperty().get(proArray[0]);
		
		for(int i =1 ; i < proArray.length;i++){
			if(i != proArray.length-2){
				dataMap = (Map<String, Object>) dataMap.get(proArray[i]);
			}
			viewProperty = viewProperty.getViewData().getViewPropertyInfo().getProperty().get(proArray[i]);
		}
		
		convert(dataMap.get(proArray[proArray.length-2]),dataMap.get(proArray[proArray.length-1]),viewProperty,baseData);
	}
	
	public static void convert(Object superObj,Object obj,ViewProperty viewProperty,Map<String,Object> baseData,String dataSource){
		Map<String,ViewProperty> dataMap = viewProperty.getViewData().getViewPropertyInfo().getProperty();
		Map<String,Object> valueMap = new FastMap<String,Object>();
		
		Iterator<ViewProperty> it = dataMap.values().iterator();
		
		while(it.hasNext()){
			ViewProperty rProperty =  it.next();
			String key = rProperty.getRefProperty().toLowerCase();
			
			if(baseData.containsKey(key) || obj instanceof Collection){
				Object value = baseData.get(key);
				
				if(dataSource != null){
					Data data = viewProperty.getViewData().getTargetMain();
					String ckey = rProperty.getRefProperty();
					value = convertDataType(data,ckey,value,dataSource);
				}

				valueMap.put(rProperty.getName(), value);
			}
		}
		
		
		if(obj instanceof Collection){
			((Collection)obj).add(valueMap);
		}else{
			OneRelation relation = (OneRelation)viewProperty.getRelation();
			Object value = valueMap.get(relation.getOneKey());
			Map<String,Object> dataProMap = (Map<String, Object>) obj;
			Map<String,Object> dataSuperProMap = (Map<String, Object>) superObj;
			if(value != null && !(value instanceof NullData)){
				dataSuperProMap.put(relation.getOneMainkey(), value);
			}
			dataProMap.putAll(valueMap);
		}
	}
	
	public static void convert(Object superObj,Object obj,ViewProperty viewProperty,BaseData baseData){
		convert(superObj,obj,viewProperty,baseData.getValues(),null);
	}
	
	public static void convert(ModelData viewBaseData,BaseData baseData){
		
		//��ȡView Data������Ϣ
		//ConfigInfo configInfo = ConfigManager.getInstance().getConfigInfo();
		
		ViewData viewDataConfig = viewBaseData.getViewInfo();
		
		Map<String,String> refMap = null;
		
		String dataName = baseData.getName();
		
		if(viewDataConfig.getTargetMain().getName().equals(dataName)){
			refMap = viewDataConfig
					.getViewPropertyInfo()
					.getRefMap();
			
			//���ת��
			Map<String, Object> dataMap = baseData.getValues();
			
			Set<String> keySet = dataMap.keySet();
			
			Iterator<String> it = keySet.iterator();
			
			while(it.hasNext()){
				String key = it.next();
				Object value = dataMap.get(key);
				
				String viewDataKey = refMap.get(key);
				viewBaseData.setValue(viewDataKey, value);
			}
			
		}else{
			RelationInfo relationInfo = viewDataConfig.getRelationInfo();
			
			ViewProperty viewProperty = relationInfo.getRelationByDataName1(dataName).getViewProperty();
			//RelationView relationView = relationInfo.getRelationByDataName(dataName);
			
			Object obj = viewBaseData.getValue(viewProperty.getName());
			
			convert(viewBaseData.getAllValues(),obj,viewProperty,baseData);
			//Collection<RelationProperty> rProCollection = relationView.getAll();
			/*
			Map<String,ViewProperty> dataMap = viewProperty.getViewData().getViewPropertyInfo().getProperty();
			Map<String,Object> valueMap = new HashMap<String,Object>(dataMap.size());
			
			Iterator<ViewProperty> it = dataMap.values().iterator();
			
			while(it.hasNext()){
				ViewProperty rProperty =  it.next();
				valueMap.put(rProperty.getName(), 
						baseData.getValue(rProperty.getRefProperty()));
			}
			
			if(obj instanceof Collection){
				((Collection)obj).add(valueMap);
			}else{
				OneRelation relation = (OneRelation)viewProperty.getRelation();
				Object value = valueMap.get(relation.getOneKey());
				if(value != null && !(value instanceof NullData)){
					viewBaseData.setValue(relation.getOneMainkey(), value);
				}
				viewBaseData.setValue(viewProperty.getName(), 
						valueMap);
			}*/
		}
	}
	
	
	public static void convert(Map<String,Object> viewValue,Data data,Map<String,ViewProperty> viewPropertyMap,Map<String, Object> baseData,String dataSource){
		Iterator<ViewProperty> it = viewPropertyMap.values().iterator();
		while(it.hasNext()){
			ViewProperty viewProperty = it.next();
			if(viewValue.containsKey(viewProperty.getName()) && viewProperty.getRelation() == null){
				Object value = baseData.get(viewProperty.getRefProperty().toLowerCase());
				if(dataSource != null){
					String ckey = viewProperty.getRefProperty();
					value = convertDataType(data,ckey,value,dataSource);
				}
				viewValue.put(viewProperty.getName(), value);
			}
		}
	}
	
	public static Object convertDataType(Data data,String key,Object value,String dataSource){
		DataTable dataTable = data.getTableInfo().getTable(dataSource);
		
		Column column = dataTable.getColumns().get(key);
		String originType = column.getOriginType();
	
		if(originType!=null && !"".equals(originType)){
			value = DataTypeConvert.convert(value, column.getType(), originType,dataSource,false);
		}
		return value;
	}
	
}

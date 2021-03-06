package com.orm.context.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.orm.common.xml.exception.DataNotDefineException;
import com.orm.common.xml.model.config.ConfigInfo;
import com.orm.common.xml.model.view.RelationInfo;
import com.orm.common.xml.model.view.RelationProperty;
import com.orm.common.xml.model.view.RelationView;
import com.orm.common.xml.model.view.ViewData;
import com.orm.common.xml.model.view.ViewProperty;
import com.orm.common.xml.util.Constanst;



public class ModelDataFactory {

	private static final ModelDataFactory viewDataFactory =  new ModelDataFactory();
	
	private ModelDataFactory()
	{
		
	}

	public static ModelDataFactory getInstance()
	{
		return viewDataFactory;
	}
	
	public ModelData createData(String name) throws DataNotDefineException{
		ModelData baseData = new ModelData();
		
		ConfigInfo configInfo = DataUtil.getConfigInfo();
		ViewData viewDataConfig = configInfo.getViewData(name);
		
		if(viewDataConfig == null)
			throw new DataNotDefineException("The view data:"+name+" is not defined!");
		
		baseData.setName(name);
		
		//��ȡ����������Ϣ
		Map<String, ViewProperty> map 
			= viewDataConfig.getViewPropertyInfo().getProperty();
		
		baseData.setViewInfo(viewDataConfig);
		
		convert(baseData.getAllValues(),map);
		
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
	
	private void convert(ModelData viewBaseData,Map<String, ViewProperty> map){
		Set<String> keySet = map.keySet();
		Iterator<String> it = keySet.iterator();
		while(it.hasNext()){
			String key = it.next();
			viewBaseData.addKey(key);
		}
		
	}
	
	private void convert(Map<String,Object> viewMap,Map<String, ViewProperty> map){
		Set<String> keySet = map.keySet();
		Iterator<String> it = keySet.iterator();
		while(it.hasNext()){
			String key = it.next();
			ViewProperty viewProperty = map.get(key);
			if(viewProperty.getRelation() !=null){
				if(viewProperty.getRelation().getType().equals(Constanst.RELATION_TYPE_ONE_TO_MANY)){
					viewMap.put(key, new ArrayList<Object>());
				}else{
					Map<String,Object> subViewMap = new HashMap<String,Object>();
					viewMap.put(key, subViewMap);
					convert(subViewMap,viewProperty.getViewData().getViewPropertyInfo().getProperty());
				}
			}else{
				viewMap.put(key,null);
			}
		}
	}
}

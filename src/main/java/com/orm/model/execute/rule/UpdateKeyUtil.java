package com.orm.model.execute.rule;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.orm.common.xml.model.data.Data;
import com.orm.common.xml.model.relation.ManyRelation;
import com.orm.common.xml.model.relation.OneRelation;
import com.orm.common.xml.model.relation.Relation;
import com.orm.common.xml.model.view.RelationInfo;
import com.orm.common.xml.model.view.RelationProperty;
import com.orm.common.xml.model.view.RelationView;
import com.orm.common.xml.model.view.ViewData;
import com.orm.common.xml.model.view.ViewProperty;
import com.orm.common.xml.util.Constanst;
import com.orm.context.data.DataUtil;
import com.orm.context.data.ModelData;

public class UpdateKeyUtil {

	private String dataSourceName;
	
	private Map<String,Object> value;
	
	private ViewData viewData;
	
	public UpdateKeyUtil(String dataSourceName,Object value,ViewData viewData){
		this.dataSourceName = dataSourceName;
		
		this.value = DataUtil.getModelValues((ModelData) value);
		
		this.viewData = viewData;
	}
	
	public void update(boolean isMain,String propertyName){
		if(isMain){
			updateToOther(propertyName);
		}else{
			updateToMain(propertyName);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private void updateToOther(String propertyName){
		
		Data dataInfo = viewData.getTargetMain();
		
		String idKey = dataInfo.getTableInfo().getTable(dataSourceName).getPropertyKey();
		
		Object idValue = ((Map<String,Object>)value).get(idKey);
		
		String proRefName = idKey;// Util.getKeyPropertyName(dataName,dataSourceName);
		
		RelationInfo relationInfo = viewData.getRelationInfo();
		if(relationInfo == null){
			return;
		}
		Collection<Relation> rCollection = relationInfo.getRelation1();
		
		value.put(proRefName, idValue);
		
		Iterator<Relation> it = rCollection.iterator();
		
		while(it.hasNext()){
			Relation relation = it.next();
			
			String objName = relation.getViewProperty().getName();
			
			Object obj = value.get(objName);
			
			Map<String,ViewProperty> proMap = relation.getViewProperty().getViewData().getViewPropertyInfo().getProperty();
			
			if(relation.getType().equals(Constanst.RELATION_TYPE_ONE_TO_ONE)){
				
				OneRelation oRrelation = (OneRelation)relation;
				if(oRrelation.getOneMainkey().equals(proRefName)){
					ViewProperty relationProperty = proMap.get(oRrelation.getOneKey());
					if(relationProperty != null){
						Map<String,Object> data = (Map<String,Object>)obj;
						data.put(relationProperty.getName(), idValue);
					}
				}
			}else{
				ManyRelation manyRrelation = (ManyRelation)relation;
				
				ViewProperty relationProperty = proMap.get(manyRrelation.getManyKey());
				if(relationProperty != null){
					Collection<Map<String,Object>> oCollection = (Collection<Map<String,Object>>)obj;
					
					Iterator<Map<String,Object>> itObj = oCollection.iterator();
					
					while(itObj.hasNext()){
						Map<String,Object> objPor = itObj.next();
						objPor.put(relationProperty.getName(), idValue);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void updateToMain(String propertyName){
		
		Object propertyValue = value.get(propertyName);
		
		if(propertyValue instanceof Collection)
			return;
		
		Data dataInfo = viewData
				.getRelationInfo()
				.getRelationByPropertyName1(propertyName).getViewProperty().getViewData().getTargetMain();

		String idKey =dataInfo.getTableInfo()
				.getTable(dataSourceName)
				.getPropertyKey();
		
		Object idValue = ((Map<String,Object>)propertyValue).get(idKey);
		
		RelationInfo relationInfo = viewData.getRelationInfo();
		
		Collection<Relation> rCollection = relationInfo.getRelation1();
		
		Iterator<Relation> it = rCollection.iterator();
		
		while(it.hasNext()){
			Relation relation = it.next();
			//Relation relation = relationView.getRef();//Util.getRelation(relationView.getRef());
			
			if(!relation.getType().equals(Constanst.RELATION_TYPE_ONE_TO_ONE))
				continue;
			
			OneRelation oRrelation = (OneRelation)relation;
			
			if(oRrelation.getOneRef().equals(dataInfo.getName())){
				value.put(oRrelation.getOneMainkey(), idValue);
				break;
			}
		}
		
	}

}

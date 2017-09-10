package com.orm.service;

import java.util.Iterator;
import java.util.Map;

import com.orm.common.xml.model.data.Data;
import com.orm.common.xml.model.data.DataProperty;
import com.orm.common.xml.model.view.ViewData;
import com.orm.common.xml.model.view.ViewProperty;
import com.orm.context.data.DataUtil;
import com.orm.context.data.ModelData;

public class DataTypeConvert {


	public void convert(ModelData mData){
		ViewData viewData = DataUtil.getViewInfo(mData);
		

	}
	
	private void convert(ViewData viewData,Map<String,Object> dataMap){
		Data data = viewData.getTargetMain();
		
		Map<String,ViewProperty> viewPropertyInfo = viewData.getViewPropertyInfo().getProperty();
		
		Iterator<ViewProperty> it = viewPropertyInfo.values().iterator();
		
		while(it.hasNext()){
			ViewProperty viewProperty = it.next();
			
			DataProperty dataProperty = data.getPropertyInfo().getProperty(viewProperty.getRefProperty());
		}
	}
	
	private void convertData(String type,String name,Map<String,Object> dataMap){
		Object objValue = dataMap.get(name);
		
		if(!type.equals("string")){
			
		}
	}
	
	
}

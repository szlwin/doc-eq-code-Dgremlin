package com.orm.api.mongodb.convert.container;

import java.util.HashMap;
import java.util.Map;

import com.orm.api.mongodb.convert.MongoDBConvert;
import com.orm.api.mongodb.convert.common.DeleteConvert;
import com.orm.api.mongodb.convert.common.SelectConvert;
import com.orm.api.mongodb.convert.common.UpdateConvert;
import com.orm.api.mongodb.convert.single.SingleConvert;
import com.orm.common.convert.container.ConvertContainer;
import com.orm.api.dom.ApiConvertInfo;
import com.orm.sql.dom.ConvertParam;

public class MongoDBConvertContainer implements ConvertContainer<ConvertParam, ApiConvertInfo>{

	private Map<String,Class<? extends MongoDBConvert>> convertMap = new HashMap<String,Class<? extends MongoDBConvert>>(3);
	
	private Map<String,Class<? extends MongoDBConvert>> comConvertMap = new HashMap<String,Class<? extends MongoDBConvert>>(3);
	
	public ApiConvertInfo convert(ConvertParam e) {
		MongoDBConvert convert = null;
		String sql = e.getSql();
		Class<? extends MongoDBConvert> convertClass = null;
		
		if(sql != null && !"".equals(sql)){
			convertClass = comConvertMap.get(e.getType());
		}else{
			convertClass = convertMap.get(e.getType());
		}
		
		try {
			convert = convertClass.newInstance();
		} catch (Exception e1) {
			//e1.printStackTrace();
		}
		convert.setDataSource(e.getDataSource());
		return convert.convert(e);
	}

	public void init() {
		convertMap.put("delete", SingleConvert.class);
		convertMap.put("update", SingleConvert.class);
		convertMap.put("insert", SingleConvert.class);
		convertMap.put("get", SingleConvert.class);
		
		comConvertMap.put("delete", DeleteConvert.class);
		comConvertMap.put("update", UpdateConvert.class);
		comConvertMap.put("query", SelectConvert.class);
		comConvertMap.put("get", SelectConvert.class);
	}

}

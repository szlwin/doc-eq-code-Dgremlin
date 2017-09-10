package com.orm.api.redis.convert;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.orm.api.dom.FieldInfo;
import com.orm.common.xml.model.data.DataTable;
import com.orm.context.data.BaseData;
import com.orm.sql.dom.DataInfo;
import com.orm.sql.util.Util;

public class MapConvert implements DataConvert<Collection<DataInfo>,Collection<FieldInfo>>{

	public Collection<FieldInfo> convertFieldInfo(BaseData data, String dataSourceName) {
		Collection<FieldInfo> fieldInfoSet= Util.convertFieldInfo(data,dataSourceName);
		
		//Iterator<DataInfo> it = dataInfoSet.iterator();
		
		//Map<String,String> dataMap =  new HashMap<String,String>(dataInfoSet.size());
		
		//DataTable dataTable = Util.getTableInfo(data.getName(), dataSourceName);
		//String key = dataTable.getKey();
		
		//while(it.hasNext()){
		//	DataInfo dataInfo = it.next();
			
			//dataMap.put(dataInfo.getColumn(), (String)dataInfo.getValue());
			
			//去除key
		//}
		//去除key
		//DataTable dataTable = Util.getTableInfo(data.getName(), dataSourceName);
		
		//dataMap.remove(dataTable.getKey());
		return fieldInfoSet;
	}
	
	public Collection<DataInfo> convert(BaseData data, String dataSourceName) {
		Collection<DataInfo> dataInfoSet= Util.convert(data,dataSourceName,true);
		
		//Iterator<DataInfo> it = dataInfoSet.iterator();
		
		//Map<String,String> dataMap =  new HashMap<String,String>(dataInfoSet.size());
		
		//DataTable dataTable = Util.getTableInfo(data.getName(), dataSourceName);
		//String key = dataTable.getKey();
		
		//while(it.hasNext()){
		//	DataInfo dataInfo = it.next();
			
			//dataMap.put(dataInfo.getColumn(), (String)dataInfo.getValue());
			
			//去除key
		//}
		//去除key
		//DataTable dataTable = Util.getTableInfo(data.getName(), dataSourceName);
		
		//dataMap.remove(dataTable.getKey());
		return dataInfoSet;
	}

}

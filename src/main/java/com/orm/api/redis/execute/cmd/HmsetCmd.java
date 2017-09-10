package com.orm.api.redis.execute.cmd;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.orm.sql.dom.DataInfo;
import com.orm.sql.util.Util;

public class HmsetCmd extends AbstractCmd{

	@SuppressWarnings("unchecked")
	public Object execute() {
		
		Collection<DataInfo> dataInfoSet= (Collection<DataInfo>) this.data;
		
		Iterator<DataInfo> it = dataInfoSet.iterator();
		
		Map<String,String> dataMap =  new HashMap<String,String>(dataInfoSet.size());
		
		//DataTable dataTable = Util.getTableInfo(data.getName(), dataSourceName);
		//String key = dataTable.getKey();
		
		while(it.hasNext()){
			DataInfo dataInfo = it.next();
			
			dataMap.put(dataInfo.getColumn(), (String)dataInfo.getValue());
			
		}
		//去除key
		//DataTable dataTable = Util.getTableInfo(data.getName(), dataSourceName);
		
		//dataMap.remove(dataTable.getKey());
		
		return this.jedis.hmset(key, dataMap);
	}
}

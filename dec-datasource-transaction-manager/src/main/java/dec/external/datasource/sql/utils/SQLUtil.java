package dec.external.datasource.sql.utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import dec.core.datasource.dom.DataInfo;

public class SQLUtil {

	public static String convert(PreparedStatement preState,Collection<DataInfo> dataSet, boolean isNeedParam) throws SQLException{
		if(dataSet == null || dataSet.isEmpty())
			return null;
		
		int i = 1;
		
		Iterator<DataInfo> it = dataSet.iterator();
		
		String param = null;
		StringBuffer paramBuffer = null;
		
		boolean isSingle = isNeedParam && dataSet.size() == 1;
				
		if(!isSingle){
			paramBuffer = new StringBuffer();
		}
		
		while(it.hasNext()){
			DataInfo dataInfo = it.next();
			Object value = dataInfo.getValue();
			preState.setObject(i, value);
			i++;
			
			if(isSingle){
				param = dataInfo.getValue().toString();
				
			}else{
				paramBuffer.append(dataInfo.getValue()+",  ");
			}
		}
		
		if(isSingle){
			return param;
		}else{
			return paramBuffer.toString();
		}
	}
	
	public static void convert(PreparedStatement preState,Collection<DataInfo> dataSet,String key) throws SQLException{
		int i = 1;
		
		Iterator<DataInfo> it = dataSet.iterator();
		
		while(it.hasNext()){
			DataInfo dataInfo = it.next();
			String column = dataInfo.getColumn();
			
			if(column.equals(key))
				continue;
			Object value = dataInfo.getValue();
			
			preState.setObject(i, value);
		}
	}
	
	public static void convertRsToMap(ResultSet rs,Map<String,Object> map) throws SQLException{
		
		//Map<String,Object> dataMap = ToolUtil.getValues(data);
		//Set<String> keySet = dataMap.keySet();
		//Iterator<String> it = keySet.iterator();
		ResultSetMetaData rsMeta = rs.getMetaData();
		int columnCount = rsMeta.getColumnCount();
		
		for(int i = 1; i <= columnCount; i++){
			String key = rsMeta.getColumnLabel(i).toLowerCase();
			
			Object value = rs.getObject(key);
			
			//if(value instanceof oracle.sql.DATE){
			//	value = new java.sql.Date(((oracle.sql.DATE)value).timestampValue().getTime());
			//}else if(value instanceof oracle.sql.TIMESTAMP){
			//	value = new java.sql.Timestamp(((oracle.sql.TIMESTAMP)value).timestampValue().getTime());
			//}
			
			map.put(key, value);
		}
	}
}

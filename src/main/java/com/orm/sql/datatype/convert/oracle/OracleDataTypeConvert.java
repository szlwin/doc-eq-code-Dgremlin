package com.orm.sql.datatype.convert.oracle;

import java.util.HashMap;
import java.util.Map;

import com.orm.sql.datatype.convert.ConvertContainer;
import com.orm.sql.datatype.convert.DataConvert;

public class OracleDataTypeConvert implements ConvertContainer{

	private Map<String,Class<? extends DataConvert>> funMap
		= new HashMap<String,Class<? extends DataConvert>>();
	
	private Map<String,Class<? extends DataConvert>> funrMap
		= new HashMap<String,Class<? extends DataConvert>>();
	
	public Object convert(Object data,String fun,boolean isTo){
		if(data == null)
			return data;
		DataConvert convert = null;
		
		try {
			if(isTo){
				convert = funMap.get(fun).newInstance();
			}else{
				convert = funrMap.get(fun).newInstance();
			}
		} catch (Exception e) {
		
		}
		
		return convert.convert(data);
		
	}
	
	public Object convert(Object data,String fun){
		return convert(data,fun,true);
	}

	public void init() {
		funMap.put("date_date", DateToSqlDate.class);
		funMap.put("date_timestamp", DateToSqlTimestamp.class);
		
		funrMap.put("date_date", SqlDateToDate.class);

		funrMap.put("date_timestamp", SqlTimestampToDate.class);
		
	}
	
	public boolean check(String oriType, String targetType) {
		// TODO Auto-generated method stub
		return funMap.containsKey(oriType+"_"+targetType);
	}
}

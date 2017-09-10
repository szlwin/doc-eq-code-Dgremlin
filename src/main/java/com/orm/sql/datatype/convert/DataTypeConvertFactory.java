package com.orm.sql.datatype.convert;

import java.util.HashMap;
import java.util.Map;

import com.orm.sql.datatype.convert.hbase.HbaseDataTypeConvert;
import com.orm.sql.datatype.convert.mysql.MySQLDataTypeConvert;
import com.orm.sql.datatype.convert.oracle.OracleDataTypeConvert;

public class DataTypeConvertFactory {

	private static Map<String,Class<? extends ConvertContainer>> containerMap
		= new HashMap<String,Class<? extends ConvertContainer>>();
	
	static{
		containerMap.put("MySQL", MySQLDataTypeConvert.class);
		containerMap.put("Oracle", OracleDataTypeConvert.class);
		containerMap.put("HBase", HbaseDataTypeConvert.class);
	}
	
	public static ConvertContainer getConvertContainer(String type){
		ConvertContainer convertContainer = null;
		try {
			convertContainer = containerMap.get(type).newInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		return convertContainer;
	}
}

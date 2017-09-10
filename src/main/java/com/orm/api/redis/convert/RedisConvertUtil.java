package com.orm.api.redis.convert;

import java.util.HashMap;
import java.util.Map;

import com.orm.context.data.BaseData;

public class RedisConvertUtil {

	private static Map<String,Map<String,String>> cmdMap =new HashMap<String,Map<String,String>>();
	
	private static Map<String,Class<?extends DataConvert<?,?>>> convertMap =new HashMap<String,Class<?extends DataConvert<?,?>>>();
	
	static{
		Map<String,String> map_cmd = new HashMap<String,String>();
		map_cmd.put("insert", "hmset");
		map_cmd.put("update", "hmset");
		map_cmd.put("get", "hgetall");
		map_cmd.put("delete", "del");
		cmdMap.put("map", map_cmd);
		
		
		convertMap.put("map", MapConvert.class);
	}
	
	
	public static String getCmd(String type,String execute){
		return cmdMap.get(type).get(execute);
	}
	
	public static Object convertFieldInfo(BaseData data,String dataSource,String type){
		DataConvert<?,?> dataConvert = null; 
		try {
			dataConvert = convertMap.get(type).newInstance();
		} catch (Exception e) {

		}
		return dataConvert.convertFieldInfo(data, dataSource);
	}
	
	public static Object convert(BaseData data,String dataSource,String type){
		DataConvert<?,?> dataConvert = null; 
		try {
			dataConvert = convertMap.get(type).newInstance();
		} catch (Exception e) {

		}
		return dataConvert.convert(data, dataSource);
	}
}

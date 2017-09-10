package com.orm.api.mongodb.execute;

import java.util.HashMap;
import java.util.Map;

public class CompareFlag {
	
	private static Map<String,String> flagMap = new HashMap<String,String>();
	
	static{
		flagMap.put(">", "$gt");
		flagMap.put(">=", "$gte");
		flagMap.put("<=", "$lte");
		flagMap.put("<", "$lt");
		flagMap.put("!=", "$ne");
		flagMap.put("=", "");
	}
	
	public static String getFlag(String flag){
		
		return flagMap.get(flag);
	}

}

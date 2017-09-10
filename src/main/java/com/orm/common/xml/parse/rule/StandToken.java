package com.orm.common.xml.parse.rule;

import java.util.HashMap;
import java.util.Map;

public class StandToken {

	public static Map<String,Parser> tokenMap = new HashMap<String,Parser>();
	
	public final String SELECT = "select";
	
	static{
		tokenMap.put("select", null);
		tokenMap.put("update", null);
		tokenMap.put("delete", null);
		tokenMap.put("insert", null);
		
		
		tokenMap.put("from", null);
		tokenMap.put("as", null);
		
		tokenMap.put("right", null);
		tokenMap.put("left", null);
		tokenMap.put("join", null);
		tokenMap.put("on", null);
		
		tokenMap.put("where", null);
		
		tokenMap.put("and", null);
		tokenMap.put("or", null);
		
		tokenMap.put("(", null);
		tokenMap.put(")", null);
		
		tokenMap.put("=", null);
		tokenMap.put(">", null);
		tokenMap.put("<", null);
		
		tokenMap.put("+", null);
		tokenMap.put("-", null);
		tokenMap.put("*", null);
		tokenMap.put("/", null);
		
		tokenMap.put("order", null);
		tokenMap.put("group", null);
		tokenMap.put("by", null);
		
		tokenMap.put("count", null);
		tokenMap.put("max", null);
		tokenMap.put("min", null);
		tokenMap.put("avg", null);
	}
}

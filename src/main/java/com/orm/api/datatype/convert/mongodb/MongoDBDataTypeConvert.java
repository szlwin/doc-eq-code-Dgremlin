package com.orm.api.datatype.convert.mongodb;

import com.orm.sql.datatype.convert.ConvertContainer;

public class MongoDBDataTypeConvert implements ConvertContainer{

	public Object convert(Object data,String fun,boolean isTo){
		return data;
	}
	
	public Object convert(Object data,String fun){
		return data;
	}

	public void init() {
		
		
	}
	
	public boolean check(String oriType, String targetType) {
		// TODO Auto-generated method stub
		return false;
	}
}

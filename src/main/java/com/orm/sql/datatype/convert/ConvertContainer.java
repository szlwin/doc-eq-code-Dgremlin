package com.orm.sql.datatype.convert;

public interface ConvertContainer {
	
	public void init();
	
	public Object convert(Object data,String fun,boolean isTo);
	
	public Object convert(Object data,String fun);
	
	public boolean check(String origType,String targetType);
}

package com.orm.common.convert;

public interface DataConvert<E,R> {
	public R convert(E e);
	
	public void setDataSource(String dataSource);
	
	public String getDataSource();
}

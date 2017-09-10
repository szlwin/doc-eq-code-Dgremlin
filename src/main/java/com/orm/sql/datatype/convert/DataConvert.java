package com.orm.sql.datatype.convert;

public interface DataConvert<E,R> {

	public R convert(E data);
}

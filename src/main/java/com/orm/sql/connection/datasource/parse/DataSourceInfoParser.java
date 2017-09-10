package com.orm.sql.connection.datasource.parse;

public interface DataSourceInfoParser<E,V> {

	public E parser(V v) throws Exception;
}

package com.orm.query;

import java.util.Collection;

import com.orm.common.execute.exception.ExecuteException;

public interface Query<E,V> {

	public void addQueryInfo(String sql,V v,Collection<E> dataCollection) throws ExecuteException;
	
	public void executeQuery() throws ExecuteException;
}

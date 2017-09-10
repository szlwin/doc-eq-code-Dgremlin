package com.orm.common.execute.container;

import com.orm.common.execute.exception.ExecuteException;

public interface ExecuteContainer<E,V> {

	public V execute(E e) throws ExecuteException;
	
	public void init();
}

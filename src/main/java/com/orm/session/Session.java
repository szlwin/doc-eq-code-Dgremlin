package com.orm.session;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.connection.exception.ConectionException;

public interface Session<E> {
	
	public void save(E e) throws ExecuteException;
	
	public void update(E e) throws ExecuteException;
	
	public void delete(E e) throws ExecuteException;
	
	public E get(E e) throws ExecuteException;
	
	public void begian() throws ConectionException;
	
	public void commit() throws ConectionException;
	
	public void rollback() throws ConectionException;
	
	public void close() throws ConectionException;
}

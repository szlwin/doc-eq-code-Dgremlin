package com.orm.connection;

import com.orm.common.convert.container.ConvertContainer;
import com.orm.common.execute.container.ExecuteContainer;
import com.orm.common.execute.exception.ExecuteException;
import com.orm.connection.exception.ConectionException;

public interface DataConnection<E,V> {

	public void connect() throws ConectionException;
	
	public void commit() throws ConectionException;
	
	public void close() throws ConectionException;
	
	public V execute(E e) throws ExecuteException;
	
	public void rollback() throws ConectionException;
	
	public void setConvertContainer(ConvertContainer<?, ?> convertContainer);
	
	public void setExecuteContainer(ExecuteContainer<?, ?> executeContainer);
	
	public String getConName();
	
	public String getDataSource();
	
	public void setConName(String name);
	
	public void setDataSource(String dataSource);
	
	public boolean isClosed() throws ConectionException;
	
	public void setAutoCommit(boolean isAuto) throws ConectionException;
}

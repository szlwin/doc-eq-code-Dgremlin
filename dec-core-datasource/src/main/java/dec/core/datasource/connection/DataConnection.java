package dec.core.datasource.connection;

import dec.core.datasource.connection.exception.ConectionException;
import dec.core.datasource.convert.container.ConvertContainer;
import dec.core.datasource.execute.container.ExecuteContainer;
import dec.core.datasource.execute.exception.ExecuteException;

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

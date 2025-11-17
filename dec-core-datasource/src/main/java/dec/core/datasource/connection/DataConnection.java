package dec.core.datasource.connection;

import dec.core.datasource.connection.exception.ConectionException;
import dec.core.datasource.convert.container.ConvertContainer;
import dec.core.datasource.execute.container.ExecuteContainer;
import dec.core.datasource.execute.exception.ExecuteException;

public interface DataConnection<E,V> {

	void connect() throws ConectionException;
	
	void commit() throws ConectionException;
	
	void close() throws ConectionException;
	
	V execute(E e) throws ExecuteException;
	
	void rollback() throws ConectionException;
	
	void setConvertContainer(ConvertContainer<?, ?> convertContainer);
	
	void setExecuteContainer(ExecuteContainer<?, ?> executeContainer);
	
	String getConName();
	
	String getDataSource();
	
	void setConName(String name);
	
	void setDataSource(String dataSource);
	
	boolean isClosed() throws ConectionException;

	boolean isConnect() throws ConectionException;

	void setAutoCommit(boolean isAuto) throws ConectionException;

	boolean isAutoCommit();

	void setTransactionType(int type);

	int getTransactionType();
	void setFlag(int flag);

	int getFlag();


}

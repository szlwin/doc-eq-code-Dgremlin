package dec.core.datasource.connection.datasource;

import java.util.Map;

import dec.core.datasource.connection.exception.ConectionException;

public interface DataSource<E,V> {

	void setDataSource(E e);
	
	E getDataSource();
	
	V getConnection() throws ConectionException;
	
	void init(String type,Map<String,String> property) throws ConectionException;
}

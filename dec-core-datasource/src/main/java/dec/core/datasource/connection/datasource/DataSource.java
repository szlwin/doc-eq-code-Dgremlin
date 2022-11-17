package dec.core.datasource.connection.datasource;

import java.util.Map;

import dec.core.datasource.connection.exception.ConectionException;

public interface DataSource<E,V> {

	public void setDataSource(E e);
	
	public E getDataSource();
	
	public V getConnection() throws ConectionException;
	
	public void init(String type,Map<String,String> property) throws ConectionException;
}

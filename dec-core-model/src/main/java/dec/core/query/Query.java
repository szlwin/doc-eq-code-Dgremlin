package dec.core.query;

import java.util.Collection;

import dec.core.datasource.execute.exception.ExecuteException;

//import dec.core.common.execute.exception.ExecuteException;

public interface Query<E,V> {

	public void addQueryInfo(String sql,V v,Collection<E> dataCollection) throws ExecuteException;
	
	public void executeQuery() throws ExecuteException;
}

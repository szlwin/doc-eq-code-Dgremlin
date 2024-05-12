package dec.core.query;

import java.util.Collection;

import dec.core.datasource.execute.exception.ExecuteException;

//import dec.core.common.execute.exception.ExecuteException;

public interface Query<E,V> {

	void addQueryInfo(String sql,V v,Collection<E> dataCollection) throws ExecuteException;
	
	void executeQuery() throws ExecuteException;
}

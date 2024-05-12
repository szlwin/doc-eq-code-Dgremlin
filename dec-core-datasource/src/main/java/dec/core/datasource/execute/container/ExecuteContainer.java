package dec.core.datasource.execute.container;

import dec.core.datasource.execute.exception.ExecuteException;

public interface ExecuteContainer<E,V> {

	V execute(E e) throws ExecuteException;
	
	void init();
}

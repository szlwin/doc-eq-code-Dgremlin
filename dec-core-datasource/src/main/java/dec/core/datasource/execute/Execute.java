package dec.core.datasource.execute;

import dec.core.datasource.execute.exception.ExecuteException;

public interface Execute<E,R,C> {

	void execute() throws ExecuteException;
	
	void setConnection(C con);
	
	void setCmd(String cmd);
	
	void setValue(E e);
	
	void setKeyType(String keyType);
	
	void setKeyValue(Object keyValue);
	
	R getResult();
}

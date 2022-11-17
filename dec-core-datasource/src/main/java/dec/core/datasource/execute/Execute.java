package dec.core.datasource.execute;

import dec.core.datasource.execute.exception.ExecuteException;

public interface Execute<E,R,C> {

	public void execute() throws ExecuteException;
	
	public void setConnection(C con);
	
	public void setCmd(String cmd);
	
	public void setValue(E e);
	
	public void setKeyType(String keyType);
	
	public void setKeyValue(Object keyValue);
	
	public R getResult();
}

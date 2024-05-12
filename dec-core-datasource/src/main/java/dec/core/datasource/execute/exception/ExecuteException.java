package dec.core.datasource.execute.exception;

public class ExecuteException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ExecuteException()
	{
		
	}
	
	public ExecuteException(Exception e)
	{
		super(e);
	}
	
	public ExecuteException(String msg)
	{
		super(msg);
	}
}

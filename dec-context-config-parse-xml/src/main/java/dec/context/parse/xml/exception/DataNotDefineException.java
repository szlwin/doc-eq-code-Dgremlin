package dec.context.parse.xml.exception;

public class DataNotDefineException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DataNotDefineException()
	{
		
	}
	
	public DataNotDefineException(Exception e)
	{
		super(e);
	}
	
	public DataNotDefineException(String msg)
	{
		super(msg);
	}
}

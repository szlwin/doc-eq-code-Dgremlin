package dec.expand.declare.conext.parser.xml.exception;

public class XMLParseException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public XMLParseException()
	{
		
	}
	
	public XMLParseException(Exception e)
	{
		super(e);
	}
	
	public XMLParseException(String msg)
	{
		super(msg);
	}
}

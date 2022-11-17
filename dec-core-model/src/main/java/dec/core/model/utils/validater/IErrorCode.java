package dec.core.model.utils.validater;


public interface IErrorCode {

    
	public long getCode();
	
	public String getMsg();
	
	public ErrorType getType();
	
	public String getUserMsg();

	public void setUserMsg(String userMsg);
}

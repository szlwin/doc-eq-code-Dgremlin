package dec.core.model.utils.validater;


public interface IErrorCode {

    
	long getCode();
	
	String getMsg();
	
	ErrorType getType();
	
	String getUserMsg();

	void setUserMsg(String userMsg);
}

package dec.core.model.container;

//import java.util.Map;

//import javolution.util.FastMap;

public class ResultInfo {

	private boolean isSuccess = false;
	
	private String errorName;
	
	private String errorMsg;

	private String errorCode;
	
	private String errorLevel;
	
	private String ruleName;
	
	private String viewName;
	
	private Object data;
	
	//private Map<String,Object> valueMap;// = new FastMap<String,Object>();
	public static ResultInfo fail(String errorCode, String errorMsg){
		return fail(errorCode, errorMsg, null);
		
	}
	
	public static ResultInfo fail(String errorCode, String errorMsg, String level){
		ResultInfo resultInfo = new ResultInfo();
		resultInfo.setSuccess(false);
		resultInfo.setErrorCode(errorCode);
		resultInfo.setErrorMsg(errorMsg);
		resultInfo.setErrorLevel(level);
		
		return resultInfo;
	}
	
	public static ResultInfo success(){
		ResultInfo resultInfo = new ResultInfo();
		resultInfo.setSuccess(true);
		return resultInfo;
	}
	
	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public String getErrorName() {
		return errorName;
	}

	public void setErrorName(String errorName) {
		this.errorName = errorName;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	//protected Map<String,Object> getValues(){
	//	return valueMap;
	//}
	
	//protected Object getValue(String key){
	//	return valueMap.get(key);
	//}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorLevel() {
		return errorLevel;
	}

	public void setErrorLevel(String errorLevel) {
		this.errorLevel = errorLevel;
	}

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	//public Map<String, Object> getValueMap() {
	//	return valueMap;
	//}

	//public void setValueMap(Map<String, Object> valueMap) {
	//	this.valueMap = valueMap;
	//}
	
	
}

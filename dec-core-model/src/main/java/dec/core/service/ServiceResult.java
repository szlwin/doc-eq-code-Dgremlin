package dec.core.service;

import java.util.HashMap;
import java.util.Map;

import dec.core.model.execute.rule.exception.ExecuteRuleException;

public class ServiceResult {

	private boolean isSuccess = true;
	
	private String errorCode;
	
	private String errorMsg;
	
	private Object resultData;

	private String name;
	
	private String version;
	
	private Map<String,ExecuteRuleException> errorMap 
		= new HashMap<String,ExecuteRuleException>();
	
	public boolean getIsSuccess() {
		return isSuccess;
	}

	public boolean isSuccess(String taskName) {
		return !errorMap.containsKey(taskName);
	}
	
	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public Object getResultData() {
		return resultData;
	}

	public void setResultData(Object resultData) {
		this.resultData = resultData;
	}
	
	public void addError(ExecuteRuleException e,String taskName){
		errorMap.put(taskName, e);
		this.isSuccess = false;
	}

	public Map<String, ExecuteRuleException> getErrorMap() {
		return errorMap;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	
}

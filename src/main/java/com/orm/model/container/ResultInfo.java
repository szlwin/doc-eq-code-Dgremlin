package com.orm.model.container;

import java.util.HashMap;
import java.util.Map;

public class ResultInfo {

	private boolean isSuccess = false;
	
	private String errorName;
	
	private String errorMsg;

	private String ruleName;
	
	private Map<String,Object> valueMap = new HashMap<String,Object>(10);
	
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

	protected Map<String,Object> getValues(){
		return valueMap;
	}
	
	protected Object getValue(String key){
		return valueMap.get(key);
	}
	
	
}

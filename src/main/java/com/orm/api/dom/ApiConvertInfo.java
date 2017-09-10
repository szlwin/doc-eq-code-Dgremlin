package com.orm.api.dom;

import java.util.Collection;

import com.orm.sql.dom.DataInfo;
public class ApiConvertInfo {

	private String cmd;
	
	private Collection<DataInfo> dataInfo;

	private String keyType;
	
	private Object keyValue;
	
	private String keyId;
	
	private Collection<FieldInfo> fieldInfo;
	
	private Collection<ConditionInfo> conditionInfo;
	
	private String dataName;
	
	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public Collection<DataInfo> getDataInfo() {
		return dataInfo;
	}

	public void setDataInfo(Collection<DataInfo> dataInfo) {
		this.dataInfo = dataInfo;
	}

	public String getKeyType() {
		return keyType;
	}

	public void setKeyType(String keyType) {
		this.keyType = keyType;
	}

	public Object getKeyValue() {
		return keyValue;
	}

	public void setKeyValue(Object keyValue) {
		this.keyValue = keyValue;
	}

	public String getKeyId() {
		return keyId;
	}

	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}

	public Collection<FieldInfo> getFieldInfo() {
		return fieldInfo;
	}

	public void setFieldInfo(Collection<FieldInfo> fieldInfo) {
		this.fieldInfo = fieldInfo;
	}

	public Collection<ConditionInfo> getConditionInfo() {
		return conditionInfo;
	}

	public void setConditionInfo(Collection<ConditionInfo> conditionInfo) {
		this.conditionInfo = conditionInfo;
	}

	public String getDataName() {
		return dataName;
	}

	public void setDataName(String dataName) {
		this.dataName = dataName;
	}
	
	
}

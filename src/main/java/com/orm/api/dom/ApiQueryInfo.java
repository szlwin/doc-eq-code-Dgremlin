package com.orm.api.dom;

import java.util.Collection;
import java.util.Map;

import com.orm.sql.dom.DataInfo;

public class ApiQueryInfo {

	private String sql;
	
	private Object con;
	
	private Collection<DataInfo> dataInfoCollection;
	
	private String type;
	
	private String keyType;
	
	private Object keyValue;
	
	private Map<String,String> conParamMap;
	
	private Collection<FieldInfo> fieldInfo;
	
	private Collection<ConditionInfo> conditionInfo;
	
	private String keyId;
	
	private String dataName;
	
	public ApiQueryInfo(){
		
	}
	
	
	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public Object getCon() {
		return con;
	}
	public void setCon(Object con) {
		this.con = con;
	}
	public Collection<DataInfo> getDataInfoCollection() {
		return dataInfoCollection;
	}
	public void setDataInfoCollection(Collection<DataInfo> dataInfoCollection) {
		this.dataInfoCollection = dataInfoCollection;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
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

	public Map<String, String> getConParamMap() {
		return conParamMap;
	}

	public void setConParamMap(Map<String, String> conParamMap) {
		this.conParamMap = conParamMap;
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


	public String getKeyId() {
		return keyId;
	}


	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}


	public String getDataName() {
		return dataName;
	}


	public void setDataName(String dataName) {
		this.dataName = dataName;
	}
	
}

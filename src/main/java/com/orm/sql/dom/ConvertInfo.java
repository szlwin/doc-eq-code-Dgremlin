package com.orm.sql.dom;

import java.util.Collection;
public class ConvertInfo {

	private String cmd;
	
	private Collection<DataInfo> dataInfo;

	private String keyType;
	
	private Object keyValue;
	
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
	
	
}

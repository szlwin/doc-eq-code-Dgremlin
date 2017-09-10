package com.orm.common.xml.model.config.connection;

import java.util.HashMap;
import java.util.Map;

import com.orm.common.xml.model.config.ConfigBaseData;
import com.orm.common.xml.model.config.ConnectionInfo;

public class Connection extends ConfigBaseData{

	public static final String DATA_SOURCE  = "data-source";
	
	public static final String PROPERTY  = "property";
	
	public static final String DATA_SOURCE_REF  = "ref";
	
	public static final String NAME  = "name";
	
	private String name;
	
	private DataSourceInfo dataSourceInfo = new DataSourceInfo();
	
	private ConnectionInfo connectionInfo;
	
	private Map<String,String> propertyInfo = new HashMap<String,String>();
	
	public DataSourceInfo getDataSourceInfo() {
		return dataSourceInfo;
	}

	public void setDataSourceInfo(DataSourceInfo dataSourceInfo) {
		this.dataSourceInfo = dataSourceInfo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ConnectionInfo getConnectionInfo() {
		return connectionInfo;
	}

	public void setConnectionInfo(ConnectionInfo connectionInfo) {
		this.connectionInfo = connectionInfo;
	}

	public Map<String, String> getPropertyInfo() {
		return propertyInfo;
	}

	public void setPropertyInfo(Map<String, String> propertyInfo) {
		this.propertyInfo = propertyInfo;
	}
	
	
}

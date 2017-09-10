package com.orm.common.xml.model.config.datasource;

import com.orm.common.xml.model.config.ConfigBaseData;

public class DataSource extends ConfigBaseData{

	public static final String TYPE  = "type";
	
	public static final String NAME  = "name";
	
	public static final String DATA_SOURCE_CLASS  = "data-source-class";
	
	public static final String INTERFACE  = "interface";
	
	public static final String DRIVER_CLASS  = "driver-class";
	
	public static final String URL  = "url";
	
	public static final String USERNAME  = "username";
	
	public static final String PASSWORD  = "password";
	
	public static final String PROPERTIES  = "properties";
	
	private String type;
	
	private String name;
	
	private String driverClass;
	
	private String url;
	
	private	String userName;
	
	private String passWord;
	
	private String conName;
	
	private com.orm.connection.datasource.DataSource dataSource;
	
	public String getDriverClass() {
		return driverClass;
	}

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getConName() {
		return conName;
	}

	public void setConName(String conName) {
		this.conName = conName;
	}

	@SuppressWarnings("rawtypes")
	public com.orm.connection.datasource.DataSource getDataSource() {
		return dataSource;
	}

	@SuppressWarnings("rawtypes")
	public void setDataSource(com.orm.connection.datasource.DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	
}

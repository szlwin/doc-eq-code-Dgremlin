package com.orm.common.xml.model.data;

import com.orm.common.xml.model.config.ConfigBaseData;


public class Data extends ConfigBaseData{

	public static final String NODE_NAME = "data";
	
	public static final String NAME = "name";
	
	public static final String CLASS = "class";
	
	public static final String PROPERTY_INFO = "property-info";
	
	public static final String TABLE_INFO = "table-info";
	
	private String name;
	
	private String className;
	
	private PropertyInfo propertyInfo;
	
	private TableInfo tableInfo;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PropertyInfo getPropertyInfo() {
		return propertyInfo;
	}

	public void setPropertyInfo(PropertyInfo propertyInfo) {
		this.propertyInfo = propertyInfo;
	}

	public TableInfo getTableInfo() {
		return tableInfo;
	}

	public void setTableInfo(TableInfo tableInfo) {
		this.tableInfo = tableInfo;
	}
	
}

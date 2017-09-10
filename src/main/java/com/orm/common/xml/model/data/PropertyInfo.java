package com.orm.common.xml.model.data;

import java.util.HashMap;
import java.util.Map;

public class PropertyInfo {

	public static final String PROERTY = "property";
	
	private Map<String,DataProperty> properInfo = new HashMap<String,DataProperty>();

	public DataProperty getProperty(String name) {
		return properInfo.get(name);
	}
	
	public void addProperty(DataProperty dataProperty) {
		properInfo.put(dataProperty.getName(),dataProperty);
	}
	
	public Map<String, DataProperty> getProperty() {
		return properInfo;
	}
	
}

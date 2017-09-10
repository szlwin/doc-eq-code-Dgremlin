package com.orm.common.xml.model.view;

import java.util.HashMap;
import java.util.Map;

public class ViewPropertyInfo {

	public static final String PROERTY = "property";
	
	private Map<String,ViewProperty> properInfo = new HashMap<String,ViewProperty>();

	private Map<String,String> refMap = new HashMap<String,String>();
	
	public void addProperty(ViewProperty viewProperty) {
		properInfo.put(viewProperty.getName(),viewProperty);
		refMap.put(viewProperty.getRefProperty(), viewProperty.getName());
	}
	
	public ViewProperty addProperty(String name) {
		return properInfo.get(name);
	}
	
	public Map<String, ViewProperty> getProperty() {
		return properInfo;
	}
	
	public Map<String, String> getRefMap() {
		return refMap;
	}
}

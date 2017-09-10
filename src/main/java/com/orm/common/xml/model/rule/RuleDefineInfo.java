package com.orm.common.xml.model.rule;

public class RuleDefineInfo {

	public static String TYPE = "type";
	
	public static String NAME = "name";
	
	public static String PROPERTY = "property";
	
	private String type;
	
	private String name;
	
	private String property;
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}
	
}

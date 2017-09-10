package com.orm.common.xml.model.relation;

import com.orm.common.xml.model.config.ConfigBaseData;
import com.orm.common.xml.model.view.ViewProperty;

public class Relation extends ConfigBaseData{

	public static final String NODE_NAME = "relation";
	
	public static final String NAME = "name";
	
	public static final String TYPE = "type";
	
	private String name;
	
	private String type;

	private ViewProperty viewProperty;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ViewProperty getViewProperty() {
		return viewProperty;
	}

	public void setViewProperty(ViewProperty viewProperty) {
		this.viewProperty = viewProperty;
	}
	
	
}

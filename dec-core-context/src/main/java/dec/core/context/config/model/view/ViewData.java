package dec.core.context.config.model.view;

import dec.core.context.config.model.config.data.ConfigBaseData;
import dec.core.context.config.model.data.Data;

public class ViewData extends ConfigBaseData{

	public static final String NODE_NAME = "view";
	
	public static final String NAME = "name";
	
	public static final String CLASS = "class";
	
	public static final String TARGET_MAIN = "target-main";

	public static final String PROPERTY_INFO = "property-info";
	
	public static final String RELATION_INFO = "relation-info";
	
	private String name;
	
	private String className;
	
	private Data targetMain;

	private ViewPropertyInfo viewPropertyInfo;
	
	private RelationInfo relationInfo;
	
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

	public Data getTargetMain() {
		return targetMain;
	}

	public void setTargetMain(Data targetMain) {
		this.targetMain = targetMain;
	}



	public ViewPropertyInfo getViewPropertyInfo() {
		return viewPropertyInfo;
	}

	public void setViewPropertyInfo(ViewPropertyInfo viewPropertyInfo) {
		this.viewPropertyInfo = viewPropertyInfo;
	}

	public RelationInfo getRelationInfo() {
		return relationInfo;
	}

	public void setRelationInfo(RelationInfo relationInfo) {
		this.relationInfo = relationInfo;
	}
	
	
}

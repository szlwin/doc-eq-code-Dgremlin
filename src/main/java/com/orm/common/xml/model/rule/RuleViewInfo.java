package com.orm.common.xml.model.rule;

import java.util.ArrayList;
import java.util.List;

import com.orm.common.xml.model.config.ConfigBaseData;
import com.orm.common.xml.model.view.ViewData;

public class RuleViewInfo extends ConfigBaseData{

	public final static String NODE_NAME = "rule-view-info";
	
	public final static String RULE = "rule";
	
	public final static String NAME = "name";
	
	public final static String VIEW_REF = "view-ref";
	
	private String name;

	private ViewData viewData;
	
	private List<RuleDefineInfo> ruleList = new ArrayList<RuleDefineInfo>(15);
	
	public String getName() {
		return name;
	}

	public void setName(String viewName) {
		this.name = viewName;
	}

	//public String getViewRef() {
	//	return viewRef;
	//}

	//public void setViewRef(String viewRef) {
	//	this.viewRef = viewRef;
	//}
	
	public void addRule(RuleDefineInfo ruleDefineInfo){
		ruleList.add(ruleDefineInfo);
	}
	
	public List<RuleDefineInfo> getRules(){
		return ruleList;
	}

	public ViewData getViewData() {
		return viewData;
	}

	public void setViewData(ViewData viewData) {
		this.viewData = viewData;
	}
	
	
}

package com.orm.model.execute.rule.common;

import com.orm.common.xml.model.rule.RuleDefineInfo;
import com.orm.common.xml.model.view.ViewData;
import com.orm.connection.DataConnection;

public abstract class AbstractRuleExecute implements RuleExecute{

	protected Object value;
	
	protected DataConnection con;
	
	protected ViewData viewData;
	
	protected RuleDefineInfo ruleInfo;
	
	public void setValue(Object obj) {
		this.value = obj;
		
	}

	public void setCon(DataConnection con) {
		this.con = con;
		
	}

	public void setViewData(ViewData data) {
		this.viewData = data;
		
	}
	
	public void setRuleInfo(RuleDefineInfo ruleInfo){
		this.ruleInfo = ruleInfo;
	}

}

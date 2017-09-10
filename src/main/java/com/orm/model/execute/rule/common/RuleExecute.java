package com.orm.model.execute.rule.common;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.common.xml.model.rule.RuleDefineInfo;
import com.orm.common.xml.model.view.ViewData;
import com.orm.connection.DataConnection;

public interface RuleExecute{

	public boolean execute() throws ExecuteException;
	
	public void setValue(Object obj);
	
	public void setCon(DataConnection con);
	
	public void setViewData(ViewData viewData);
	
	public void setRuleInfo(RuleDefineInfo e);
}

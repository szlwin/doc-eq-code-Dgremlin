package dec.core.model.execute.rule.common;

import java.util.Map;

import dec.core.context.config.model.rule.RuleDefineInfo;
import dec.core.context.config.model.view.ViewData;
import dec.core.datasource.connection.DataConnection;

/*import dec.core.common.xml.model.rule.RuleDefineInfo;
import dec.core.common.xml.model.view.ViewData;
import dec.core.connection.DataConnection;*/

public abstract class AbstractRuleExecute implements RuleExecute{

	protected Object value;
	
	protected DataConnection con;
	
	protected ViewData viewData;
	
	protected RuleDefineInfo ruleInfo;
	
	protected Map<String,Object> externalParam;
	
	public void setExternalParam(Map<String, Object> param){
		this.externalParam = param;
	}
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

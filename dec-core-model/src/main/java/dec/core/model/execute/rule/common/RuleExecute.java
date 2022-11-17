package dec.core.model.execute.rule.common;

import java.util.Map;

import dec.core.context.config.model.rule.RuleDefineInfo;
import dec.core.context.config.model.view.ViewData;
import dec.core.datasource.connection.DataConnection;
import dec.core.datasource.execute.exception.ExecuteException;

/*import dec.core.common.execute.exception.ExecuteException;
import dec.core.common.xml.model.rule.RuleDefineInfo;
import dec.core.common.xml.model.view.ViewData;
import dec.core.connection.DataConnection;*/

public interface RuleExecute{

	public boolean execute() throws ExecuteException;
	
	public void setValue(Object obj);
	
	public void setCon(DataConnection con);
	
	public void setViewData(ViewData viewData);
	
	public void setRuleInfo(RuleDefineInfo e);
	
	public void setExternalParam(Map<String, Object> param);
}

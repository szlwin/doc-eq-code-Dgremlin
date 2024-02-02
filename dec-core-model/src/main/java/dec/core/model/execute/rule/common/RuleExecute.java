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

	boolean execute() throws ExecuteException;
	
	void setValue(Object obj);
	
	void setCon(DataConnection con);
	
	void setViewData(ViewData viewData);
	
	void setRuleInfo(RuleDefineInfo e);
	
	void setExternalParam(Map<String, Object> param);
}

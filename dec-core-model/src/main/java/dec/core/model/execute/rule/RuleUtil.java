package dec.core.model.execute.rule;

import java.util.Map;

import dec.core.context.config.model.config.ConfigConstanst;
import dec.core.context.config.model.rule.RuleDefineInfo;
import dec.core.context.config.model.view.ViewData;
import dec.core.datasource.connection.DataConnection;
/*import dec.core.common.xml.model.rule.RuleCheckDataPattern;
import dec.core.common.xml.model.rule.RuleDefineInfo;
import dec.core.common.xml.model.view.ViewData;
import dec.core.common.xml.util.Constanst;
import dec.core.connection.DataConnection;*/
import dec.core.model.execute.rule.common.CheckDataExecute;
import dec.core.model.execute.rule.common.CheckDataPatternExecute;
import dec.core.model.execute.rule.common.CheckExecute;
import dec.core.model.execute.rule.common.RuleExecute;
import dec.core.model.execute.rule.data.DeleteExecute;
import dec.core.model.execute.rule.data.GetExecute;
import dec.core.model.execute.rule.data.InsertExecute;
import dec.core.model.execute.rule.data.SelectExecute;
import dec.core.model.execute.rule.data.UpdateExecute;
import dec.core.model.execute.rule.grammer.GrammerExecute;
import javolution.util.FastMap;
public class RuleUtil {

	private static Map<String,Class<? extends RuleExecute>> ruleExecuteClass 
		= new FastMap<String,Class<? extends RuleExecute>>();
	
	static{
		ruleExecuteClass.put(ConfigConstanst.RULE_TYPE_CHECK, CheckExecute.class);
		ruleExecuteClass.put(ConfigConstanst.RULE_TYPE_CHECK_PATTERN, CheckExecute.class);
		ruleExecuteClass.put(ConfigConstanst.RULE_TYPE_CHECK_DATA, CheckDataExecute.class);
		ruleExecuteClass.put(ConfigConstanst.RULE_TYPE_CHECK_DATA_PATTERN, CheckDataPatternExecute.class);

		ruleExecuteClass.put(ConfigConstanst.RULE_TYPE_EXECUTE_INSERT,InsertExecute.class);
		ruleExecuteClass.put(ConfigConstanst.RULE_TYPE_EXECUTE_UPDATE, UpdateExecute.class);
		ruleExecuteClass.put(ConfigConstanst.RULE_TYPE_EXECUTE_DELETE, DeleteExecute.class);
		ruleExecuteClass.put(ConfigConstanst.RULE_TYPE_EXECUTE_GET, GetExecute.class);
		ruleExecuteClass.put(ConfigConstanst.RULE_TYPE_EXECUTE_SELECT, SelectExecute.class);
		ruleExecuteClass.put(ConfigConstanst.RULE_TYPE_EXECUTE_GRAMMER, GrammerExecute.class);
		
	}
	
	public static boolean isQuery(String type){
		
		if(type.equals(ConfigConstanst.RULE_TYPE_EXECUTE_GET)
				|| type.equals(ConfigConstanst.RULE_TYPE_EXECUTE_SELECT))
			return true;
		
		return false;
	}
	
	public static boolean isInsert(String type){
		
		if(type.equals(ConfigConstanst.RULE_TYPE_EXECUTE_INSERT))
			return true;
		
		return false;
	}
	
	public static RuleExecute createRuleExeute(String type, RuleDefineInfo ruleInfo, Object value,
			DataConnection con, ViewData viewData, Map<String,Object> externalParam){
		RuleExecute ruleExecute = null;
		
		try {
			ruleExecute = ruleExecuteClass.get(type).newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
		ruleExecute.setCon(con);
		ruleExecute.setRuleInfo(ruleInfo);
		ruleExecute.setValue(value);
		ruleExecute.setViewData(viewData);
		ruleExecute.setExternalParam(externalParam);
		
		return ruleExecute;
		
	}
}

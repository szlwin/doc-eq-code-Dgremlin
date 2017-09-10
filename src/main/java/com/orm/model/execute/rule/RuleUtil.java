package com.orm.model.execute.rule;

import java.util.HashMap;
import java.util.Map;

import com.orm.common.xml.model.rule.RuleCheckDataPattern;
import com.orm.common.xml.model.rule.RuleDefineInfo;
import com.orm.common.xml.model.view.ViewData;
import com.orm.common.xml.util.Constanst;
import com.orm.connection.DataConnection;
import com.orm.model.execute.rule.common.CheckDataExecute;
import com.orm.model.execute.rule.common.CheckDataPatternExecute;
import com.orm.model.execute.rule.common.CheckExecute;
import com.orm.model.execute.rule.common.DataExecute;
import com.orm.model.execute.rule.common.RuleExecute;
import com.orm.model.execute.rule.data.DeleteExecute;
import com.orm.model.execute.rule.data.InsertExecute;
import com.orm.model.execute.rule.data.SelectExecute;
import com.orm.model.execute.rule.data.UpdateExecute;
import com.orm.model.execute.rule.data.GetExecute;
public class RuleUtil {

	private static Map<String,Class<? extends RuleExecute>> ruleExecuteClass 
		= new HashMap<String,Class<? extends RuleExecute>>(8);
	
	static{
		ruleExecuteClass.put(Constanst.RULE_TYPE_CHECK, CheckExecute.class);
		ruleExecuteClass.put(Constanst.RULE_TYPE_CHECK_PATTERN, CheckExecute.class);
		ruleExecuteClass.put(Constanst.RULE_TYPE_CHECK_DATA, CheckDataExecute.class);
		ruleExecuteClass.put(Constanst.RULE_TYPE_CHECK_DATA_PATTERN, CheckDataPatternExecute.class);

		ruleExecuteClass.put(Constanst.RULE_TYPE_EXECUTE_INSERT,InsertExecute.class);
		ruleExecuteClass.put(Constanst.RULE_TYPE_EXECUTE_UPDATE, UpdateExecute.class);
		ruleExecuteClass.put(Constanst.RULE_TYPE_EXECUTE_DELETE, DeleteExecute.class);
		ruleExecuteClass.put(Constanst.RULE_TYPE_EXECUTE_GET, GetExecute.class);
		ruleExecuteClass.put(Constanst.RULE_TYPE_EXECUTE_SELECT, SelectExecute.class);
	}
	
	public static boolean isQuery(String type){
		
		if(type.equals(Constanst.RULE_TYPE_EXECUTE_GET)
				|| type.equals(Constanst.RULE_TYPE_EXECUTE_SELECT))
			return true;
		
		return false;
	}
	
	public static boolean isInsert(String type){
		
		if(type.equals(Constanst.RULE_TYPE_EXECUTE_INSERT))
			return true;
		
		return false;
	}
	
	public static RuleExecute createRuleExeute(String type,RuleDefineInfo ruleInfo,Object value,DataConnection con,ViewData viewData){
		RuleExecute ruleExecute = null;
		
		try {
			ruleExecute = ruleExecuteClass.get(type).newInstance();
		} catch (Exception e) {
			System.out.println(type);
		} 
		ruleExecute.setCon(con);
		ruleExecute.setRuleInfo(ruleInfo);
		ruleExecute.setValue(value);
		ruleExecute.setViewData(viewData);
		
		return ruleExecute;
		
	}
}

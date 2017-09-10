package com.orm.model.execute.rule.common;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import smarter.common.exception.ExecuteExpection;
import smarter.common.express.check.PatternCheck;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.common.xml.model.rule.RuleCheckDataPattern;
import com.orm.sql.dom.ExecuteInfo;
import com.orm.sql.dom.ExecuteParam;

public class CheckDataPatternExecute extends AbstractRuleExecute{

	//private DataConnection con;
	
	//private RuleCheckDataPattern rule;
	
	//private Object value;
	
	private String keyArray[];
	
	/*public CheckDataPatternExecute(RuleCheckDataPattern ruleExecuteInfo,Object value, DataConnection con){
		this.rule = ruleExecuteInfo;
		this.value = value;
		this.con = con;
	}*/
	
	
	public boolean execute() throws ExecuteException{
		
		Collection dataList = executeSQL();
		
		if(dataList.size() == 0)
			return false;
		
		Iterator it = dataList.iterator();
		recordRemoveKey((Map<String, Object>) it.next());
		
		boolean result = false;
		try {
			result = checkData(dataList);
		} catch (ExecuteExpection e) {
			throw new ExecuteException(e);
		}
		
		remove();
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private Collection executeSQL() throws ExecuteException{
		RuleCheckDataPattern rule = (RuleCheckDataPattern)this.ruleInfo;
		ExecuteParam executeParam = new ExecuteParam();
		executeParam.setType("query");
		executeParam.setValue(value);
		executeParam.setCmd(rule.getSql());
		
		ExecuteInfo result;
		
		result = (ExecuteInfo) con.execute(executeParam);
		
		
		/*QuerySQLExecute sqlExecute 
		= new QuerySQLExecute(con.getConnection(),con.getDataSource());

		QueryInfo queryInfo = new QueryInfo();
		queryInfo.setSql(rule.getSql());
		queryInfo.setParamMap((Map<String,Object>)value);

		List<Map<String,Object>> dataCollection = new ArrayList<Map<String,Object>>();
	
		sqlExecute.setDataCollection(dataCollection);
		sqlExecute.execute(queryInfo);
		
		return dataCollection;*/
		return result.getDataCollection();
	}
	
	private void recordRemoveKey(Map<String,Object> param){
		Set<String> set = param.keySet();
		Iterator<String> it = set.iterator();
		
		keyArray = new String[param.values().size()];
		
		int i = 0;
		while(it.hasNext()){
			keyArray[i] = it.next();
			i++;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private void remove(){
		for(int i = 0; i < keyArray.length;i++){
			((Map<String,Object>)value).remove(keyArray[i]);
		}
	}
	
	private boolean checkPattern(Map<String,Object> param) throws ExecuteExpection{
		RuleCheckDataPattern rule = (RuleCheckDataPattern)this.ruleInfo;
		PatternCheck patternCheck = new PatternCheck();
		patternCheck.setCheckValue(param);
		patternCheck.setPattern(rule.getPattern());
		
		return patternCheck.check();
	}
	
	@SuppressWarnings("unchecked")
	private boolean checkData(Collection dataList) throws ExecuteExpection{
		Iterator it = dataList.iterator();
		while(it.hasNext()){
			Map<String,Object> param = (Map<String, Object>) it.next();
			((Map<String,Object>)value).putAll(param);
			boolean result = checkPattern((Map<String,Object>)value);
			if(!result)
				return false;
		}
		
		return true;
	}
}

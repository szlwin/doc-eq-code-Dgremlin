package dec.core.model.execute.rule.common;



import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import dec.core.context.config.model.rule.RuleCheckDataPattern;
import dec.core.datasource.execute.dom.ExecuteInfo;
/*import dec.core.common.execute.exception.ExecuteException;
import dec.core.common.xml.model.rule.RuleCheckDataPattern;
import dec.core.sql.dom.ExecuteInfo;
import dec.core.sql.dom.ExecuteParam;*/
import dec.core.datasource.execute.exception.ExecuteException;
import dec.core.model.check.rule.CheckFactory;
import dec.external.datasource.sql.dom.ExecuteParam;
import smarter.common.exception.ExecuteExpection;
import smarter.common.express.check.PatternCheck;
import smarter.common.express.check.RuleCheck;

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
		RuleCheck patternCheck = CheckFactory.getCheck(this.ruleInfo.getType());
		//patternCheck.setCheckValue(param);
		//patternCheck.setPattern(rule.getPattern());
		
		return patternCheck.check(rule.getPattern(), param);
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

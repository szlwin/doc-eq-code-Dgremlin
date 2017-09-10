package com.orm.model.execute.rule.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.common.xml.model.rule.RuleExecuteInfo;
import com.orm.model.execute.rule.data.DataExecuteFactory;
import com.orm.model.execute.rule.data.Execute;

public class DataExecute extends AbstractRuleExecute{

	Log log = LogFactory.getLog(DataExecute.class);
	
	//private RuleExecuteInfo ruleExecuteInfo;
	
	//private Object value;
	
	//private DataConnection con;
	
	//private ViewData viewData;
	
	/*public DataExecute(RuleExecuteInfo ruleExecuteInfo,Object value, DataConnection con){
		this.ruleExecuteInfo = ruleExecuteInfo;
		this.value = value;
		this.con = con;
	}*/
	
	public boolean execute() throws ExecuteException{
		
		boolean isSuccess = false;
		

		isSuccess = executeOne();
		

		return isSuccess;
	}
	

	@SuppressWarnings("unchecked")
	private boolean executeOne() throws ExecuteException{
		
		RuleExecuteInfo ruleExecuteInfo = (RuleExecuteInfo)this.ruleInfo;
		
		String type = ruleExecuteInfo.getType();
		Execute<Object> execute = (Execute<Object>)DataExecuteFactory.createExecute(type);

		/*if(log.isDebugEnabled()){
			log.debug("create execute");
		}*/
		
		execute.setSql(ruleExecuteInfo.getSql());
		execute.setValue(this.value);
		
		if(viewData.getName().equals(ruleExecuteInfo.getProperty())){
			execute.setPropertyName(viewData.getTargetMain().getName());
		}else{
			execute.setPropertyName(ruleExecuteInfo.getProperty());
		}
		execute.setDataConnection(con);
		//execute.setConnection(con.getConnection());
		execute.setDataSource(con.getDataSource());
		execute.setViewData(viewData);
		execute.setType(type);
		return execute.execute();
	}
	
}

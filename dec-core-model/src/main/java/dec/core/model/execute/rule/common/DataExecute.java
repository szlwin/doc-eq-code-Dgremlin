package dec.core.model.execute.rule.common;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import dec.core.context.config.model.rule.RuleExecuteInfo;
import dec.core.datasource.execute.exception.ExecuteException;
//import dec.core.common.execute.exception.ExecuteException;
//import dec.core.common.xml.model.rule.RuleExecuteInfo;
import dec.core.model.execute.rule.data.DataExecuteFactory;
import dec.core.model.execute.rule.data.Execute;

public class DataExecute extends AbstractRuleExecute{
	
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

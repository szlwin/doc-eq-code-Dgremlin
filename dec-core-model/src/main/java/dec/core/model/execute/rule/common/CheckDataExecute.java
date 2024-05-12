package dec.core.model.execute.rule.common;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dec.core.collections.list.SimpleList;
import dec.core.context.config.exception.DataNotDefineException;
import dec.core.context.config.model.relation.Relation;
import dec.core.context.config.model.rule.RuleCheckData;
import dec.core.context.data.BaseData;
import dec.core.datasource.execute.dom.ExecuteInfo;
import dec.core.datasource.execute.exception.ExecuteException;
import dec.core.model.utils.DataUtil;
import dec.external.datasource.sql.dom.ExecuteParam;
import smarter.common.exception.ExecuteExpection;
import smarter.common.express.check.PropertyCheck;

/*import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dec.core.common.execute.exception.ExecuteException;
import dec.core.common.xml.exception.DataNotDefineException;
import dec.core.common.xml.model.relation.Relation;
import dec.core.common.xml.model.rule.RuleCheckData;
import dec.core.context.data.BaseData;
import dec.core.context.data.DataUtil;
import dec.core.sql.dom.ExecuteInfo;
import dec.core.sql.dom.ExecuteParam;
import smarter.common.exception.ExecuteExpection;
import smarter.common.express.check.PropertyCheck;*/

public class CheckDataExecute extends AbstractRuleExecute{
	
	private final static Logger log = LoggerFactory.getLogger(CheckDataExecute.class);
	
	//private DataConnection con;
	
	//private RuleCheckData rule;
	
	//private Object value;
	
	//private ViewData viewData;
	
	/*public CheckDataExecute(RuleCheckData ruleExecuteInfo,Object value, DataConnection con){
		this.rule = ruleExecuteInfo;
		this.value = value;
		this.con = con;
	}*/
	
	
	@SuppressWarnings("unchecked")
	public boolean execute() throws ExecuteException{
		RuleCheckData rule = (RuleCheckData) this.ruleInfo;
		Relation relationView = viewData.getRelationInfo().getRelationByPropertyName1(rule.getProperty());
		//Util.getRelationView(viewName, rule.getProperty());
		
		if(relationView == null){
			return false;
		}
		
		BaseData data;
		try {
			data = DataUtil.createBaseData(relationView.getViewProperty().getViewData().getTargetMain().getName());
		} catch (DataNotDefineException e) {
			log.error("Execute error", e);
			return false;
		}
		
		List<Map<String,Object>> list = null;
		
		if(value instanceof Collection){
			
			Collection values = (Collection<Map<String,Object>>)value;
			
			list = new SimpleList<Map<String,Object>>(values.size(), 2);
			list.addAll((Collection<Map<String,Object>>)value);
			
		}else{
			list = new SimpleList<Map<String,Object>>(1, 2);
			list.add((Map<String,Object>)value);
		}
		
		List<Object> resultList = getResult(list,data,relationView);
		
		try {
			return check(resultList);
		} catch (ExecuteExpection e) {
			throw new ExecuteException(e);
		}
	}

	public List<Object> getResult(List<Map<String,Object>> checkList,BaseData data,Relation relationView) throws ExecuteException{
		List<Object> list = new SimpleList<Object>(checkList.size(), 2);
		
		for(int i=0; i < checkList.size(); i++){
			
			DataUtil.convertDataToBaseData(checkList.get(i), data, relationView);
			
			ExecuteParam executeParam = new ExecuteParam();
			executeParam.setType("get");
			executeParam.setValue(data);
			
			ExecuteInfo result = null;
			
			result = (ExecuteInfo) con.execute(executeParam);
			
			
			//GetSQLExecute getSQLExecute = new GetSQLExecute(con.getConnection(),con.getDataSource());
			
			//getSQLExecute.execute(data);
			Iterator<?> it = result.getDataCollection().iterator();
			list.add(it.next());
		}
		

		return list;
	}
	
	private boolean check(List<Object> checkList) throws ExecuteExpection{
		for(int i=0; i < checkList.size(); i++){
			if(!checkByPattern(checkList.get(i))){
				return false;
			}
		}
		return true;
	}
	
	private boolean checkByPattern(Object value) throws ExecuteExpection{
		RuleCheckData rule = (RuleCheckData) this.ruleInfo;
		
		PropertyCheck propertyCheck = new PropertyCheck();
		propertyCheck.setCheckValue(value);
		propertyCheck.setPattern(rule.getPattern());
		return propertyCheck.check();
	}
}

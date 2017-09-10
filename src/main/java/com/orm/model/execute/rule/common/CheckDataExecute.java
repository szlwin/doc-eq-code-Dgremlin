package com.orm.model.execute.rule.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import smarter.common.exception.ExecuteExpection;
import smarter.common.express.check.PropertyCheck;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.common.xml.exception.DataNotDefineException;
import com.orm.common.xml.model.relation.Relation;
import com.orm.common.xml.model.rule.RuleCheckData;
import com.orm.context.data.BaseData;
import com.orm.context.data.DataUtil;
import com.orm.sql.dom.ExecuteInfo;
import com.orm.sql.dom.ExecuteParam;

public class CheckDataExecute extends AbstractRuleExecute{
	
	Log log = LogFactory.getLog(CheckDataExecute.class);
	
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
			log.error(e);
			return false;
		}
		
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		
		if(value instanceof Collection){
			list.addAll((Collection<Map<String,Object>>)value);
		}else{
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
		List<Object> list = new ArrayList<Object>(checkList.size());
		
		for(int i=0; i < checkList.size(); i++){
			
			DataUtil.convertDataToBaseData(checkList.get(i), data, relationView);
			
			ExecuteParam executeParam = new ExecuteParam();
			executeParam.setType("get");
			executeParam.setValue(data);
			
			ExecuteInfo result;
			
			result = (ExecuteInfo) con.execute(executeParam);
			
			
			//GetSQLExecute getSQLExecute = new GetSQLExecute(con.getConnection(),con.getDataSource());
			
			//getSQLExecute.execute(data);
			Iterator<Object> it = result.getDataCollection().iterator();
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

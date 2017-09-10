package com.orm.model.execute.rule.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.common.xml.exception.DataNotDefineException;
import com.orm.common.xml.model.relation.Relation;
import com.orm.common.xml.model.rule.RuleExecuteInfo;
import com.orm.common.xml.model.view.RelationInfo;
import com.orm.context.data.BaseData;
import com.orm.context.data.DataUtil;
import com.orm.model.execute.rule.common.AbstractRuleExecute;
import com.orm.sql.dom.ExecuteParam;

public abstract class AbstractDataExecute extends AbstractRuleExecute{

	protected String sql;
	
	//protected Object paramValue;

	//protected ViewData viewData;
	
	protected String propertyName;
	
	protected String dataSource;
	
	//protected SimpleSQLExecute simpleSQLExecute;
	
	//protected DataConnection<Object, Object> dataCon;
	
	protected String type;
	
	protected Object propertyValue;
	
	public void setSql(String sql) {
		this.sql = sql;
		
	}

	//public void setValue(Object e) {
	//	this.paramValue = e;
		
	//}
	
	public void setPropertyName(String name) {
		this.propertyName = name;
	}
	
	//public void setViewData(ViewData viewData) {
	//	this.viewData = viewData;
		
	//}

	public void setDataSource(String name) {
		this.dataSource = name;
		
	}
	
	public boolean execute() throws ExecuteException{
		
		RuleExecuteInfo ruleExecuteInfo = (RuleExecuteInfo)this.ruleInfo;
		
		if(viewData.getName().equals(ruleExecuteInfo.getProperty())){
			this.setPropertyName(viewData.getTargetMain().getName());
		}else{
			this.setPropertyName(ruleExecuteInfo.getProperty());
		}
		this.setSql(ruleExecuteInfo.getSql());
		this.setType(ruleExecuteInfo.getType());
		this.setDataSource(con.getDataSource());
		
		if(sql == null || "".equals(sql)){
			//simpleSQLExecute = createExecute();
			return executeByData();
		}else{
			return executeBySql();
		}
	}
	
	@SuppressWarnings("unchecked")
	protected boolean executeByData() throws ExecuteException{
		boolean isMain = viewData.getTargetMain().getName().equals(propertyName);
		Object dataValue = null;
		if(!isMain){
			dataValue = ((Map)value).get(propertyName);
		}else{
			dataValue = value;
		}
		
		if(dataValue instanceof Collection){
			Iterator<Object> it = ((Collection<Object>)dataValue).iterator();
			while(it.hasNext()){
				propertyValue = it.next();
				boolean result = executeByData(propertyValue);
				if(!result)
					return false;
			}
			return true;
		}else{
			
			propertyValue = dataValue;
			
			return executeByData(propertyValue);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected boolean executeBySql() throws ExecuteException{
		//Convert convert = new Convert(sql,viewName);
		//String tempSql = convert.convert();
		
		boolean isMain = viewData.getTargetMain().getName().equals(propertyName);
		
		if(!isMain){
			Object dataValue = ((Map)value).get(propertyName);
			
			if(dataValue instanceof Collection){
				Iterator<Object> it = ((Collection<Object>)dataValue).iterator();
				
				while(it.hasNext()){
					propertyValue = it.next();
					boolean result = executeBysql(sql,propertyValue);
					if(!result)
						return false;
				}
				return true;
			}else{
				propertyValue = dataValue;
				return executeBysql(sql,value);
			}
			
		}else{
			propertyValue = value;
			return executeBysql(sql,value);
		}
		/*
		if(dataValue instanceof Collection){
			Iterator<Object> it = ((Collection<Object>)dataValue).iterator();
			
			while(it.hasNext()){
				propertyValue = it.next();
				boolean result = executeBysql(sql,propertyValue);
				if(!result)
					return false;
			}
			return true;
		}else{
			
			if(!isMain){
				propertyValue = ((Map)paramValue).get(propertyName);
			}else{
				propertyValue = paramValue;
			}
			
			//propertyValue = dataValue;
			return executeBysql(sql,paramValue);
		}*/
	}
	
	@SuppressWarnings("unchecked")
	protected boolean executeByData(Object value) throws ExecuteException{
		RelationInfo relationInfo = viewData.getRelationInfo();
		Relation relationView = null;
		String dataName = null;
		boolean isMain = viewData.getTargetMain().getName().equals(propertyName);
		
		if(isMain){
			dataName = viewData.getTargetMain().getName();
		}else{
			relationView = relationInfo.getRelationByPropertyName1(propertyName);//Util.getRelationView(viewName, propertyName)
			//if(relationView == null){
			//	dataName = propertyName;
			//}else{
				dataName = relationView.getViewProperty().getViewData().getTargetMain().getName();
			//}
			//dataName = propertyName;
		}
		//viewData.getRelationInfo().getRelation(name);
		

		BaseData data = null;
		try {
			data = DataUtil.createBaseData(dataName);
		} catch (DataNotDefineException e) {
			return false;
		}
		
		if(!isMain){
			Map<String,Object> dataValue = (Map<String, Object>) value;
			DataUtil.convertDataToBaseData(dataValue, data, relationView);
		}
		else
			DataUtil.convertDataToBaseData((Map<String,Object>)value, data,viewData);
		
		//simpleSQLExecute.execute(data);
		Object object = null;
		ExecuteParam execParam = new ExecuteParam();
		execParam.setType(type);
		execParam.setValue(data);
		//execParam.setCmd(cmd);
		
		object = con.execute(execParam);
		
		
		doAfter(object);
		return true;
	}
	
	protected boolean executeBysql(String sql,Object value) throws ExecuteException{
		Object object = null;
		ExecuteParam execParam = new ExecuteParam();
		execParam.setType(type);
		execParam.setValue(value);
		execParam.setCmd(sql);
		object = this.con.execute(execParam);

		doAfter(object);
		return true;
	}

	//protected abstract SimpleSQLExecute createExecute();
	
	protected abstract void doAfter(Object result);
	

	
	public void setType(String type){
		this.type = type;
	}
}

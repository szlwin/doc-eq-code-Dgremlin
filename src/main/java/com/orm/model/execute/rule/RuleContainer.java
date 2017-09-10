package com.orm.model.execute.rule;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.common.xml.model.rule.RuleCheckData;
import com.orm.common.xml.model.rule.RuleCheckDataPattern;
import com.orm.common.xml.model.rule.RuleCheckInfo;
import com.orm.common.xml.model.rule.RuleDefineInfo;
import com.orm.common.xml.model.rule.RuleExecuteInfo;
import com.orm.common.xml.model.rule.RuleViewInfo;
import com.orm.common.xml.util.Constanst;
import com.orm.connection.DataConnection;
import com.orm.context.data.DataUtil;
import com.orm.context.data.ModelData;
import com.orm.model.container.ModelLoader;
import com.orm.model.container.ResultInfo;
import com.orm.model.execute.rule.common.CheckDataExecute;
import com.orm.model.execute.rule.common.CheckDataPatternExecute;
import com.orm.model.execute.rule.common.CheckExecute;
import com.orm.model.execute.rule.common.DataExecute;
import com.orm.model.execute.rule.exception.ExecuteRuleException;

public class RuleContainer {

	Log log = LogFactory.getLog(RuleContainer.class);
	
	private ModelLoader modelLoader;
	
	//private String conName;
	
	private RuleViewInfo ruleViewInfo;
	
	private DataConnection con;
	
	public RuleContainer(ModelLoader modelLoader){
		this.modelLoader = modelLoader;
	}
	
	public RuleContainer(ModelLoader modelLoader,DataConnection con){
		this.modelLoader = modelLoader;
		this.con = con;
	}
	
	public ResultInfo execute() throws ExecuteException, ExecuteRuleException{
		
		ruleViewInfo = getRuleInfo();
		
		ResultInfo resultInfo = executeAllRule();
		
		return resultInfo;
	}
	
	private ResultInfo executeAllRule() throws ExecuteException, ExecuteRuleException{
		
		ResultInfo resultInfo = null;
		
		List<RuleDefineInfo> ruleList = ruleViewInfo.getRules();
		
		for(int i = 0;i < ruleList.size(); i++){
			RuleDefineInfo ruleDefineInfo = ruleList.get(i);
			
			resultInfo = executeOneRule(ruleDefineInfo);
			
			if(!resultInfo.isSuccess()){
				resultInfo.setErrorName(ruleDefineInfo.getName());
				break;
			}
		}
		return resultInfo;
	}
	
	private ResultInfo executeOneRule(RuleDefineInfo rule) throws ExecuteException, ExecuteRuleException{
		
		if(log.isDebugEnabled()){
			log.debug("Execute the rule: "+ruleViewInfo.getName()+":"+rule.getName()+" start!");
		}
		
		ResultInfo resultInfo = new ResultInfo();
		
		resultInfo.setRuleName(modelLoader.getRuleName());
		
		boolean isMain = ruleViewInfo.getViewData().getName()
				.equals(rule.getProperty());
		
		Object obj = null;
		
		if(!isMain){
			obj = convert(rule.getProperty(),rule.getType());
		}else{
			obj = convertByMain();
		}
		
		/*if(log.isDebugEnabled()){
			log.debug("get the op value");
		}*/
		
		if(obj == null){
			String errorMsg = "The property: " + rule.getProperty()+" is not exist!";
			resultInfo.setSuccess(false);
			resultInfo.setErrorMsg(errorMsg);
			
			log.error(errorMsg);

			throw new ExecuteRuleException(errorMsg);
		}
		
		boolean isOk = false;
		

		isOk = RuleUtil.createRuleExeute(rule.getType(), rule, obj, con, ruleViewInfo.getViewData())
				.execute();

		
		if(isOk && 
				(RuleUtil.isInsert(rule.getType()))){
			
			UpdateKeyUtil updateKeyUtil = new UpdateKeyUtil(con.getDataSource(),
					modelLoader.get(),
					ruleViewInfo.getViewData());
			
			String propertyName = rule.getProperty();
			
			updateKeyUtil.update(isMain, propertyName);
		}
		
		if(!isOk){
			log.error("Execute the rule: "+ruleViewInfo.getName()+":"+rule.getName()+" is fail!");
		}
		
		if(log.isDebugEnabled()){
			log.debug("Execute the rule: "+ruleViewInfo.getName()+":"+rule.getName()+" end,result is "+isOk);
		}
		
		resultInfo.setSuccess(isOk);
		
		return resultInfo;
	}
	
	private RuleViewInfo getRuleInfo(){
		
		return DataUtil.getRuleViewInfo(modelLoader.getRuleName());
	}
	
	private Object convert(String property,String type){
		
		if(type.equals("check") || type.equals("checkData")){
			return DataUtil.getValueByKey(property, modelLoader.get());
		}
		return DataUtil.getModelValues((ModelData) modelLoader.get());
		/*
		if(type.equals("checkPattern") || type.equals("checkDataPattern"))
			return DataUtil.getModelValues((ModelData) modelLoader.get());
		
		if(property ==null || "".equals(property) || RuleUtil.isQuery(type)){
			return DataUtil.getModelValues((ModelData) modelLoader.get());
		}else{
			Map<String,Object> value = DataUtil.getModelValues((ModelData) modelLoader.get());
			
			return value.get(property);
			
		}*/

		//return null;
	}
	
	private Object convertByMain(){
		
		Map<String,Object> paramMap = DataUtil.getModelValues((ModelData) modelLoader.get());

		/*
		Map<String,Object> valueMap = new HashMap<String,Object>(paramMap.size());
		
		Set<String> keySet = paramMap.keySet();
		Iterator<String> it = keySet.iterator();
		while(it.hasNext()){
			String key = it.next();
			Object value = paramMap.get(key);
			
			if(value instanceof Collection || value instanceof Map)
				continue;
			
			valueMap.put(key, value);
		}*/
		//return valueMap;
		return paramMap;
	}
	
}

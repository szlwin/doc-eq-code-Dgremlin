package com.orm.common.xml.parse.rule;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import com.orm.common.xml.exception.XMLParseException;
import com.orm.common.xml.model.config.ConfigInfo;
import com.orm.common.xml.model.rule.RuleCheckData;
import com.orm.common.xml.model.rule.RuleCheckDataPattern;
import com.orm.common.xml.model.rule.RuleCheckInfo;
import com.orm.common.xml.model.rule.RuleDefineInfo;
import com.orm.common.xml.model.rule.RuleExecuteInfo;
import com.orm.common.xml.model.rule.RuleViewInfo;
import com.orm.common.xml.model.view.ViewData;
import com.orm.common.xml.parse.AbstarctElementsParser;
import com.orm.common.xml.util.Constanst;
import com.orm.context.data.DataUtil;

public class RuleParser extends AbstarctElementsParser{
	
	Log log = LogFactory.getLog(RuleParser.class);
	
	private static Map<String,String> typeMap = new HashMap<String,String>(15);
	static{
		typeMap.put(Constanst.RULE_TYPE_CHECK, null);
		typeMap.put(Constanst.RULE_TYPE_CHECK_DATA, null);
		typeMap.put(Constanst.RULE_TYPE_CHECK_DATA_PATTERN, null);
		typeMap.put(Constanst.RULE_TYPE_CHECK_PATTERN, null);
		typeMap.put(Constanst.RULE_TYPE_EXECUTE_INSERT, null);
		typeMap.put(Constanst.RULE_TYPE_EXECUTE_UPDATE, null);
		typeMap.put(Constanst.RULE_TYPE_EXECUTE_GET, null);
		typeMap.put(Constanst.RULE_TYPE_EXECUTE_DELETE, null);
		typeMap.put(Constanst.RULE_TYPE_EXECUTE_SELECT, null);
	}
	
	@SuppressWarnings("rawtypes")
	public RuleViewInfo parse(Element element) throws XMLParseException {
		RuleViewInfo ruleViewInfo = new RuleViewInfo();
		Iterator list = element.elementIterator(RuleViewInfo.RULE);
		
		String name = element.attributeValue(RuleViewInfo.NAME);
		String viewName = element.attributeValue(RuleViewInfo.VIEW_REF);
		
		log.info("Load the rule:"+name+" start!");
		
		if(!checkView(viewName)){
			String msg = "Load the rule:"+name+" is error,the view-ref is error: "+viewName;
			log.error(msg);
			throw new XMLParseException(msg);
		}
		
		ruleViewInfo.setName(name);
		//ruleViewInfo.setViewRef(viewName);
		
		ConfigInfo configInfo = DataUtil.getConfigInfo();
		ViewData viewData = configInfo.getViewData(viewName);
		
		ruleViewInfo.setViewData(viewData);
		
		while(list.hasNext())
		{
			Element ruleElement = (Element)list.next();
			
			RuleDefineInfo ruleDefineInfo = parseRuleDefineInfo(ruleElement,viewData);
			ruleViewInfo.addRule(ruleDefineInfo);
		}
		log.info("Load the rule:"+ruleViewInfo.getName()+" success!");
		return ruleViewInfo;
	}
	
	private RuleDefineInfo parseRuleDefineInfo(Element element,ViewData viewData) throws XMLParseException
	{
		String type = element.attributeValue(RuleDefineInfo.TYPE);
		if(!typeMap.containsKey(type)){
			String errorMsg = "The rule type: "+type+" is not exist!";
			log.error(errorMsg);
			throw new XMLParseException(errorMsg);
		}
		
		if(type.equals(Constanst.RULE_TYPE_CHECK) || type.equals(Constanst.RULE_TYPE_CHECK_PATTERN)){
			return parseRuleCheckInfo(element,viewData);
		}else if(type.equals(Constanst.RULE_TYPE_CHECK_DATA_PATTERN)){
			return parseRuleCheckDataPatternInfo(element,viewData);
		}else if(type.equals(Constanst.RULE_TYPE_CHECK_DATA)){
			return parseRuleCheckDataInfo(element,viewData);
		}else{
			return parseRuleExecuteInfo(element,viewData);
		}
		
		
	}
	
	private RuleExecuteInfo parseRuleExecuteInfo(Element element,ViewData viewData) throws XMLParseException
	{
		RuleExecuteInfo ruleExecuteInfo = new RuleExecuteInfo();
		
		ruleExecuteInfo.setName(element.attributeValue(RuleDefineInfo.NAME));
		ruleExecuteInfo.setProperty(element.attributeValue(RuleDefineInfo.PROPERTY));
		ruleExecuteInfo.setType(element.attributeValue(RuleDefineInfo.TYPE));
		
		if(ruleExecuteInfo.getType().equals(Constanst.RULE_TYPE_EXECUTE_GET)
				|| ruleExecuteInfo.getType().equals(Constanst.RULE_TYPE_EXECUTE_SELECT)){
			String propertyStr = ruleExecuteInfo.getProperty();
			
			if(propertyStr == null || "".equals(propertyStr)){
				String errorMsg = "The rule: "+ruleExecuteInfo.getName()+ ", the property can't be empty!";
				throw new XMLParseException(errorMsg);
			}

		}
		

		String sql = element.attributeValue(RuleExecuteInfo.SQL);
		if(sql !=null && !"".equals(sql)){
			Convert convert = new Convert(sql,viewData);
			sql = convert.convert();
		}/*else{
			if(ruleExecuteInfo.getType().equals(Constanst.RULE_TYPE_EXECUTE_GET)){
				String getSql = "select * from " + ruleExecuteInfo.getProperty() =;
				Convert convert = new Convert(getSql,viewData);
				sql = convert.convert();
			}
		}*/
		ruleExecuteInfo.setSql(sql);
		
		return ruleExecuteInfo;
	}

	private RuleCheckInfo parseRuleCheckInfo(Element element,ViewData viewData)
	{
		RuleCheckInfo ruleCheckInfo = new RuleCheckInfo();
		
		ruleCheckInfo.setName(element.attributeValue(RuleDefineInfo.NAME));
		ruleCheckInfo.setProperty(element.attributeValue(RuleDefineInfo.PROPERTY));
		ruleCheckInfo.setType(element.attributeValue(RuleDefineInfo.TYPE));
		ruleCheckInfo.setPattern(element.attributeValue(RuleCheckInfo.PATTERN));
		
		return ruleCheckInfo;
	}
	
	private RuleCheckDataPattern parseRuleCheckDataPatternInfo(Element element,ViewData viewData)
	{
		RuleCheckDataPattern ruleCheckDataPattern = new RuleCheckDataPattern();
		
		ruleCheckDataPattern.setName(element.attributeValue(RuleDefineInfo.NAME));
		//ruleCheckDataPattern.setProperty(element.getAttribute(RuleDefineInfo.PROPERTY));
		ruleCheckDataPattern.setType(element.attributeValue(RuleDefineInfo.TYPE));
		ruleCheckDataPattern.setPattern(element.attributeValue(RuleCheckDataPattern.PATTERN));
		
		String sql = element.attributeValue(RuleCheckDataPattern.SQL);
		
		if(sql !=null && !"".equals(sql)){
			Convert convert = new Convert(sql,viewData);
			sql = convert.convert();
		}
		ruleCheckDataPattern.setSql(sql);
		
		return ruleCheckDataPattern;
	}
	
	private RuleCheckData parseRuleCheckDataInfo(Element element,ViewData viewData)
	{
		RuleCheckData ruleCheckData = new RuleCheckData();
		
		ruleCheckData.setName(element.attributeValue(RuleDefineInfo.NAME));
		ruleCheckData.setProperty(element.attributeValue(RuleDefineInfo.PROPERTY));
		ruleCheckData.setType(element.attributeValue(RuleDefineInfo.TYPE));
		ruleCheckData.setPattern(element.attributeValue(RuleCheckDataPattern.PATTERN));
		
		//String sql = element.getAttribute(RuleCheckDataPattern.SQL);
		
		//if(sql !=null && !"".equals(sql)){
		//	Convert convert = new Convert(sql,viewName);
		//	sql = convert.convert();
		//}
		//ruleCheckDataPattern.setSql(sql);
		
		return ruleCheckData;
	}
	
	private boolean checkView(String name){
		ConfigInfo configInfo = DataUtil.getConfigInfo();
		if(configInfo.getViewData(name) == null){
			return false;
		}
		return true;
	}
}

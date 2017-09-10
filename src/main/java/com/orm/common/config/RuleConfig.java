package com.orm.common.config;

import java.util.HashMap;
import java.util.Map;

import com.orm.common.xml.model.config.ConfigBaseData;
import com.orm.common.xml.model.rule.RuleViewInfo;

public class RuleConfig implements Config<RuleViewInfo>{
	
	private static final RuleConfig ruleConfig =  new RuleConfig();
	
	private Map<String,RuleViewInfo> ruleMap = new HashMap<String,RuleViewInfo>();
	
	private RuleConfig()
	{
		
	}

	public static RuleConfig getInstance()
	{
		return ruleConfig;
	}

	public void add(RuleViewInfo ruleViewInfo)
	{
		ruleMap.put(ruleViewInfo.getName(),ruleViewInfo);
	}
	
	public RuleViewInfo get(String name)
	{
		return ruleMap.get(name);
	}

	public <E extends ConfigBaseData> void add(E ruleViewInfo) {
		add((RuleViewInfo)ruleViewInfo);
	}
	

}

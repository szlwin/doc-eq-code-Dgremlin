package dec.core.context.config.model.rule.config;

import java.util.Map;

import dec.core.context.config.model.config.Config;
import dec.core.context.config.model.config.data.ConfigBaseData;
import dec.core.context.config.model.rule.RuleViewInfo;
import javolution.util.FastMap;

public class RuleConfig implements Config<RuleViewInfo>{
	
	private static final RuleConfig ruleConfig =  new RuleConfig();
	
	private Map<String,RuleViewInfo> ruleMap = new FastMap<String,RuleViewInfo>();
	
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

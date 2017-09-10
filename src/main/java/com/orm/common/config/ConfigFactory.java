package com.orm.common.config;

import com.orm.common.xml.model.config.ConfigBaseData;

public class ConfigFactory {

	private static Config<?> configArray[] = new Config[Config.SIZE];
	
	static{
		configArray[Config.CONNECTION] = ConnectionConfig.getInstance();
		configArray[Config.DATA]       = DataConfig.getInstance();
		configArray[Config.DATASOURCE] = DataSourceConfig.getInstance();
		configArray[Config.RELATION]   = RelationConfig.getInstance();
		configArray[Config.RULE]       = RuleConfig.getInstance();
		configArray[Config.VIEWDATA]   = ViewDataConfig.getInstance();
		configArray[Config.SERVICE]    = ServiceConfig.getInstance();
		configArray[Config.CONNECTION_CONFIG]   = ConectionInfoConfig.getInstance();
		configArray[Config.DATASOURCE_CONFIG]   = DataSourceInfoConfig.getInstance();
	}
	
	public static Config<? extends ConfigBaseData> getConfig(int type){
		return configArray[type];
	}
}

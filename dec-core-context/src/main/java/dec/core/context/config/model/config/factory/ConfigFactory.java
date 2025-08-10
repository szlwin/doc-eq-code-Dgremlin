package dec.core.context.config.model.config.factory;

import dec.core.context.config.model.config.Config;
import dec.core.context.config.model.config.data.ConfigBaseData;
import dec.core.context.config.model.connection.config.ConectionInfoConfig;
import dec.core.context.config.model.connection.config.ConnectionConfig;
import dec.core.context.config.model.data.config.DataConfig;
import dec.core.context.config.model.datasource.config.DataSourceConfig;
import dec.core.context.config.model.datasource.config.DataSourceInfoConfig;
import dec.core.context.config.model.relation.config.RelationConfig;
import dec.core.context.config.model.rule.config.RuleConfig;
import dec.core.context.config.model.service.config.ServiceConfig;
import dec.core.context.config.model.view.config.ViewDataConfig;

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

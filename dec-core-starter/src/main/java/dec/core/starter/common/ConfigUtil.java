package dec.core.starter.common;

import dec.context.parse.xml.exception.XMLParseException;
import dec.context.parse.xml.parse.config.ConfigFileParser;
import dec.context.parse.xml.parse.config.ConnectionInfoParser;
import dec.core.context.config.manager.ConfigManager;
import dec.core.context.config.model.config.Config;
import dec.core.context.config.model.config.ConfigInfo;
import dec.core.context.config.model.connection.ConnectionInfo;
import dec.core.context.config.model.datasource.DataSourceConfigInfo;
import dec.core.context.config.utils.ConfigContextUtil;

public class ConfigUtil {
	
	public static void addDataSourceConfig(String name, String clzName){
		ConfigInfo configInfo = new ConfigInfo();
		
		ConfigManager.getInstance().setConfigInfo(configInfo);
		
		DataSourceConfigInfo dataSource = new DataSourceConfigInfo();
		dataSource.setName(name);
		
		dataSource.setDataSource(clzName);
		
		configInfo.add(Config.DATASOURCE_CONFIG, dataSource);
		
		
		ConnectionInfo connectionInfo = new ConnectionInfo();
		connectionInfo.setName(name);
		//connectionInfo.setType("sql");
		ConfigContextUtil.getConfigInfo().add(Config.CONNECTION_CONFIG, connectionInfo);
	}
	
	public static void parseConfigInfo(String filePath) throws XMLParseException
	{
		ConfigFileParser configFileParser = new ConfigFileParser();
		
		configFileParser.parse(filePath);
		
	}
	
	public static void parseConnectionInfo(String filePath) throws XMLParseException{
		ConnectionInfoParser connectionInfoParser = new ConnectionInfoParser();
		
		connectionInfoParser.parse(filePath);
	}
	

}

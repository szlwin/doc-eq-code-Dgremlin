package dec.core.context.config.manager;

import dec.core.context.config.model.config.ConfigInfo;
import dec.core.context.config.model.connection.Connection;
import dec.core.context.config.model.datasource.DataSource;

//import com.orm.common.xml.model.config.ConfigInfo;
//import com.orm.common.xml.model.config.connection.Connection;
//import com.orm.common.xml.model.config.datasource.DataSource;

public class ConfigManager {

	private static final ConfigManager configManager =  new ConfigManager();
	
	private ConfigInfo configInfo;
	
	private ConfigManager()
	{
		
	}

	public static ConfigManager getInstance()
	{
		return configManager;
	}

	public ConfigInfo getConfigInfo() {
		return configInfo;
	}

	public void setConfigInfo(ConfigInfo configInfo) {
		this.configInfo = configInfo;
	}
	
	public String getDefaultConName(){
		return configInfo.getDefaultConnection();
	}
	
	public DataSource<?> getDataSourceByName(String dataName){
		return configInfo.getDataSource(dataName);
	}
	
	public DataSource<?> getDataSourceByCon(String conName){
		Connection con = configInfo.getConnection(conName);
		return con.getDataSourceInfo().getDataSource();
	}
}

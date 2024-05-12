package dec.core.context.config.model.datasource.config;

import java.util.Map;

import dec.core.context.config.model.config.Config;
import dec.core.context.config.model.config.data.ConfigBaseData;
import dec.core.context.config.model.datasource.DataSourceConfigInfo;
import javolution.util.FastMap;

public class DataSourceInfoConfig implements Config<DataSourceConfigInfo>{
	
	private static final DataSourceInfoConfig dataSourceInfoConfig =  new DataSourceInfoConfig();
	
	private Map<String,DataSourceConfigInfo> dataSourceInfoConfigeMap = new FastMap<String,DataSourceConfigInfo>();
	
	private String defaultName;
	
	private DataSourceInfoConfig()
	{
		
	}
	
	public static DataSourceInfoConfig getInstance()
	{
		return dataSourceInfoConfig;
	}
	
	public void add(DataSourceConfigInfo dataSource)
	{
		dataSourceInfoConfigeMap.put(dataSource.getName(),dataSource);
	}
	
	public DataSourceConfigInfo get(String name)
	{
		return dataSourceInfoConfigeMap.get(name);
	}
	
	public String getDefaultName() {
		return defaultName;
	}

	public void setDefaultName(String defaultName) {
		this.defaultName = defaultName;
	}

	public <E extends ConfigBaseData> void add(E dataSource) {
		add((DataSourceConfigInfo)dataSource);
		
	}
}

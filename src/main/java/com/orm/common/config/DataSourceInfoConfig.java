package com.orm.common.config;

import java.util.HashMap;
import java.util.Map;

import com.orm.common.xml.model.config.ConfigBaseData;
import com.orm.common.xml.model.config.DataSourceConfigInfo;

public class DataSourceInfoConfig implements Config<DataSourceConfigInfo>{
	
	private static final DataSourceInfoConfig dataSourceInfoConfig =  new DataSourceInfoConfig();
	
	private Map<String,DataSourceConfigInfo> dataSourceInfoConfigeMap = new HashMap<String,DataSourceConfigInfo>();
	
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

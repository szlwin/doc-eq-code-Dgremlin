package com.orm.common.config;

import java.util.HashMap;
import java.util.Map;

import com.orm.common.xml.model.config.ConfigBaseData;
import com.orm.common.xml.model.config.datasource.DataSource;

public class DataSourceConfig implements Config<DataSource>{

	private static final DataSourceConfig dataSourceManager =  new DataSourceConfig();
	
	private Map<String,DataSource> dataSourceMap = new HashMap<String,DataSource>();
	
	private String defaultName;
	
	private DataSourceConfig()
	{
		
	}
	
	public static DataSourceConfig getInstance()
	{
		return dataSourceManager;
	}
	
	public void add(DataSource dataSource)
	{
		dataSourceMap.put(dataSource.getName(),dataSource);
	}
	
	public DataSource get(String name)
	{
		return dataSourceMap.get(name);
	}

	public DataSource get()
	{
		return dataSourceMap.get(defaultName);
	}
	
	public String getDefaultName() {
		return defaultName;
	}

	public void setDefaultName(String defaultName) {
		this.defaultName = defaultName;
	}

	public <E extends ConfigBaseData> void add(E dataSource) {
		add((DataSource)dataSource);
		
	}
	
	
}

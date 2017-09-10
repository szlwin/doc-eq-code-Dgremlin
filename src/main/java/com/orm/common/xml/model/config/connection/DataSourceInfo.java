package com.orm.common.xml.model.config.connection;

import java.util.HashMap;
import java.util.Map;

import com.orm.common.xml.model.config.ConfigBaseData;
import com.orm.common.xml.model.config.datasource.DataSource;

public class DataSourceInfo extends ConfigBaseData{

	private Map<String,DataSource> dataSourceMap = new HashMap<String,DataSource>();

	public DataSource getDataSource() {
		return dataSourceMap.get("default");
	}
	
	public void addDataSource(DataSource dataSource) {
		dataSourceMap.put("default",dataSource);
	}
}

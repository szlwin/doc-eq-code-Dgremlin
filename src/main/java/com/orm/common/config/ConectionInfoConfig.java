package com.orm.common.config;

import java.util.HashMap;
import java.util.Map;

import com.orm.common.xml.model.config.ConfigBaseData;
import com.orm.common.xml.model.config.ConnectionInfo;

public class ConectionInfoConfig implements Config<ConnectionInfo>{

	private static final ConectionInfoConfig conectionInfoConfig =  new ConectionInfoConfig();
	
	private Map<String,ConnectionInfo> conectionInfoConfigMap = new HashMap<String,ConnectionInfo>();
	
	private ConectionInfoConfig()
	{
		
	}

	public static ConectionInfoConfig getInstance()
	{
		return conectionInfoConfig;
	}

	public void add(ConnectionInfo connectionInfo)
	{
		conectionInfoConfigMap.put(connectionInfo.getName(),connectionInfo);
	}
	
	public ConnectionInfo get(String name)
	{
		return conectionInfoConfigMap.get(name);
	}

	public <E extends ConfigBaseData> void add(E connectionInfo) {
		add((ConnectionInfo)connectionInfo);
	}

}

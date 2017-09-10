package com.orm.common.config;

import java.util.HashMap;
import java.util.Map;

import com.orm.common.xml.model.config.ConfigBaseData;
import com.orm.common.xml.model.data.Data;

public class DataConfig  implements Config<Data>{
	private static final DataConfig dataConfig =  new DataConfig();
	
	private Map<String,Data> dataConfigMap = new HashMap<String,Data>();
	
	private DataConfig()
	{
		
	}

	public static DataConfig getInstance()
	{
		return dataConfig;
	}

	
	public void add(Data data)
	{
		dataConfigMap.put(data.getName(),data);
	}
	
	public Data get(String name)
	{
		return dataConfigMap.get(name);
	}

	public <E extends ConfigBaseData> void add(E data) {
		add((Data)data);
	}
	

}

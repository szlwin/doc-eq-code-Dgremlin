package dec.core.context.config.model.data.config;

import java.util.HashMap;
import java.util.Map;

import dec.core.context.config.model.config.Config;
import dec.core.context.config.model.config.data.ConfigBaseData;
import dec.core.context.config.model.data.Data;
import javolution.util.FastMap;

public class DataConfig  implements Config<Data>{
	private static final DataConfig dataConfig =  new DataConfig();
	
	private Map<String,Data> dataConfigMap = new FastMap<String,Data>();
	
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

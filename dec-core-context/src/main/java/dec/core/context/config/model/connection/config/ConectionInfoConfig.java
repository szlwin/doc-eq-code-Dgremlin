package dec.core.context.config.model.connection.config;

import java.util.Map;

import dec.core.context.config.model.config.Config;
import dec.core.context.config.model.config.data.ConfigBaseData;
import dec.core.context.config.model.connection.ConnectionInfo;
import javolution.util.FastMap;


public class ConectionInfoConfig<C,E,D> implements Config<ConnectionInfo<C,E,D>>{

	private static final ConectionInfoConfig conectionInfoConfig =  new ConectionInfoConfig();
	
	private Map<String,ConnectionInfo<C,E,D>> conectionInfoConfigMap = new FastMap<String,ConnectionInfo<C,E,D>>();
	
	private ConectionInfoConfig()
	{
		
	}

	public static <C,E,D> ConectionInfoConfig<C,E,D> getInstance()
	{
		return conectionInfoConfig;
	}

	public void add(ConnectionInfo<C,E,D> connectionInfo)
	{
		conectionInfoConfigMap.put(connectionInfo.getName(),connectionInfo);
	}
	
	public ConnectionInfo<C,E,D> get(String name)
	{
		return conectionInfoConfigMap.get(name);
	}

	public <E extends ConfigBaseData> void add(E connectionInfo) {
		add((ConnectionInfo)connectionInfo);
	}

}

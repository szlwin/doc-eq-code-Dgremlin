package dec.core.context.config.model.service.config;

import java.util.HashMap;
import java.util.Map;

import dec.core.context.config.model.config.Config;
import dec.core.context.config.model.config.data.ConfigBaseData;
import dec.core.context.config.model.service.ServiceInfo;

public class ServiceConfig implements Config<ServiceInfo>{

	private static final ServiceConfig serviceConfig =  new ServiceConfig();
	
	private Map<String,ServiceInfo> serviceMap = new HashMap<String,ServiceInfo>();
	
	private ServiceConfig()
	{
		
	}

	public static ServiceConfig getInstance()
	{
		return serviceConfig;
	}
	
	public <E extends ConfigBaseData> void add(E v) {
		add((ServiceInfo)v);
		
	}

	public void add(ServiceInfo serviceInfo)
	{
		serviceMap.put(serviceInfo.getName()+"_"+serviceInfo.getVersion(),serviceInfo);
	}
	
	public ServiceInfo get(String name) {
		return serviceMap.get(name);
	}

}

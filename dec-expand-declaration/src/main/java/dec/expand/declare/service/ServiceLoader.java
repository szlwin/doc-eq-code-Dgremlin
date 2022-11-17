package dec.expand.declare.service;

import java.util.HashMap;
import java.util.Map;

public class ServiceLoader {

	private Map<String, ServiceDeclare> serviceLoaderMap = new HashMap<>();
	
	public void loader(ServiceDeclare serviceDeclare){
		serviceLoaderMap.put(serviceDeclare.getName(), serviceDeclare);
	}
	
	public ServiceDeclare get(String key){
		return serviceLoaderMap.get(key);
	}
	
	
}

package dec.expand.declare.system;

import java.util.HashMap;
import java.util.Map;

public class SystemContext {

	private Map<String,System> systemMap = new HashMap<>();
	
	private static final SystemContext systemContext = new SystemContext();
	
	private SystemContext(){
		
	}
	
	public static SystemContext get(){
		return systemContext;
	}
	
	public void add(System system){
		systemMap.put(system.getName(), system);
	}
	
	public System get(String name){
		return systemMap.get(name);
	}
}

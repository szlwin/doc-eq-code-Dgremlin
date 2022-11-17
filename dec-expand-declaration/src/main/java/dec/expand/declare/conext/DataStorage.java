package dec.expand.declare.conext;

import java.util.HashMap;
import java.util.Map;

public class DataStorage {

	private Map<String, Object> dataMap = new HashMap<>();
	

	public void remove(String key){
		dataMap.remove(key);
	}
	
	public void add(String key, Object data){
		dataMap.put(key, data);
	}
	
	public Object get(String key){
		return dataMap.get(key);
	}

	public Map<String, Object> getDataMap() {
		return dataMap;
	}

	public void setDataMap(Map<String, Object> dataMap) {
		this.dataMap = dataMap;
	}

	
	public boolean containsData(String  data){
		return dataMap.containsKey(data);
	}
	
}

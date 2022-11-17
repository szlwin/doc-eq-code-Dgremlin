package dec.expand.declare.conext;

import java.util.HashMap;
import java.util.Map;

import dec.expand.declare.conext.desc.Desc;

public class ContextStorage {

	private Map<String,Desc>[] objecetMaps;
	
	@SuppressWarnings("unchecked")
	public ContextStorage(int size){
		objecetMaps = new HashMap[2];
	}
	
	public void add(int type, String name, Desc desc){
		
		if(objecetMaps[type] == null){
			objecetMaps[type] = new HashMap<>();
		}
		objecetMaps[type].put(name, desc);
	}
	
	
	public Desc get(int type, String name){
		return objecetMaps[type].get(name);
	}
}

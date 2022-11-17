package dec.core.context.data;

import java.io.Serializable;
import java.util.Map;

import dec.core.context.config.model.data.Data;
import javolution.util.FastMap;

/**
 * @author pc
 *
 */
public class BaseData implements Cloneable, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7899190729784277565L;

	private String name;
	
	private Map<String,Object> dataMap = new FastMap<String,Object>();
	
	private static final NullData NULLDATA = new NullData();
	
	private Data data;
	
	protected BaseData(){
		
	}
	
	protected void addKey(String key){
		dataMap.put(key, null);
	}
	
	protected void addData(String key,Object value){
		dataMap.put(key, value);
	}
	
	public Map<String, Object> getValues(){
		return dataMap;
	}
	
	public void setValue(String key,Object value){

		if(checkContainKey(key))
			dataMap.put(key, value);
		
	}
	
	public Object getValue(String key){
		return dataMap.get(key);
	}
	
	public void setNULL(String key){
		if(checkContainKey(key))
			dataMap.put(key, NULLDATA);
	}
	
	public boolean checkContainKey(String key){
		return dataMap.containsKey(key);
	}

	public String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	public Data getData() {
		return data;
	}

	protected void setData(Data data) {
		this.data = data;
	}
	
	
}

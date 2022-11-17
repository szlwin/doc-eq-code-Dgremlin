package dec.core.context.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import dec.core.context.config.model.view.ViewData;
import dec.core.context.config.utils.ToolUtils;

//import com.orm.common.xml.model.view.ViewData;

public class ModelData implements Cloneable, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -116431757329278259L;

	private String name;
	
	private Map<String,Object> dataMap;
	
	private static final NullData NULLDATA = new NullData();
	
	private ViewData viewData;
	
	protected ModelData(){
		dataMap = ToolUtils.getMap();
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
		String keyArray[] = key.split("//.");
		
		if(keyArray.length == 1){
			if(checkContainKey(key))
				addData(key, value);
		}else{
			setValueByKey(keyArray,value);
		}
	}
	
	public void addValue(String key,Object value){
		String keyArray[] = key.split("//.");
		
		if(keyArray.length == 1){
			if(checkContainKey(key)){
				Object obj = getValue(key);
				if(obj instanceof Collection)
					((Collection) obj).add(value);
			}
		}else{
			addValueByKey(keyArray,value);
		}
	}
	
	public Object getValue(String key){
		return dataMap.get(key);
	}
	
	public void setNULL(String key){
		if(checkContainKey(key))
			dataMap.put(key, NULLDATA);
	}
	
	private boolean checkContainKey(String key){
		return dataMap.containsKey(key);
	}
	
	//private boolean checkProperKey(String key){
	//	return viewData.getViewPropertyInfo().getProperty().containsKey(key);
	//}

	public String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	@SuppressWarnings("unchecked")
	protected void setValueByKey(String keyArray[],Object value){
		Map<String,Object> data = (Map<String, Object>) dataMap.get(keyArray[0]);
		
		int length = keyArray.length;
		for(int i = 0; i < length-1; i++)
		{
			if(checkContainKey(keyArray[i]))
				data = (Map<String, Object>)data;
		}
		
		if(data != null)
		{
			String key = keyArray[length-1];
			if(checkContainKey(key))
				data.put(key, value);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void addValueByKey(String keyArray[],Object value){
		Map<String,Object> data = (Map<String, Object>) dataMap.get(keyArray[0]);
		
		int length = keyArray.length;
		for(int i = 0; i < length-1; i++)
		{
			if(checkContainKey(keyArray[i]))
				data = (Map<String, Object>) data;
		}
		
		if(data != null)
		{
			String key = keyArray[length-1];
			if(checkContainKey(key)){
				Object obj = data.get(key);
				if(obj instanceof Collection)
					((Collection) obj).add(value);
			}
				
		}
	}
	
	public Map<String,Object> getAllValues(){
		return this.getValues();
	}
	
	protected void setViewInfo(ViewData viewData){
		this.viewData = viewData;
	}
	
	public ViewData getViewInfo(){
		return viewData;
	}
}

package dec.core.context.config.model.data;

import java.util.Map;

import javolution.util.FastMap;

public class PropertyInfo {

	public static final String PROERTY = "property";
	
	private Map<String,DataProperty> properInfo = new FastMap<String,DataProperty>();

	public DataProperty getProperty(String name) {
		return properInfo.get(name);
	}
	
	public void addProperty(DataProperty dataProperty) {
		properInfo.put(dataProperty.getName(),dataProperty);
	}
	
	public Map<String, DataProperty> getProperty() {
		return properInfo;
	}
	
}

package dec.core.context.config.model.datasource;

import java.util.Map;

import dec.core.context.config.model.config.data.ConfigBaseData;
import javolution.util.FastMap;

public class DataSourceInfo extends ConfigBaseData{

	private Map<String,DataSource<?>> dataSourceMap = new FastMap<String,DataSource<?>>();

	public DataSource<?> getDataSource() {
		return dataSourceMap.get("default");
	}
	
	public void addDataSource(DataSource<?> dataSource) {
		dataSourceMap.put("default",dataSource);
	}
}

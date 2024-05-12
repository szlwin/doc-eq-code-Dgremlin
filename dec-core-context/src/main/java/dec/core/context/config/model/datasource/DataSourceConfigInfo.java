package dec.core.context.config.model.datasource;

import dec.core.context.config.model.config.data.ConfigBaseData;

public class DataSourceConfigInfo extends ConfigBaseData{

	public static final String NAME = "name";
	
	public static final String TYPE = "type";
	
	public static final String DATASOURCE = "dataSource";
	
	private String name;
	
	private String type;
	
	private String dataSource;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}
	
	
	
}

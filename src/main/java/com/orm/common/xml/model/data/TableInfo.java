package com.orm.common.xml.model.data;

import java.util.HashMap;
import java.util.Map;

public class TableInfo {

	public static final String TABLE = "table";
	
	private Map<String,DataTable> tableInfo = new HashMap<String,DataTable>();
	
	//private Map<String,DataTable> tableRefDataSource = new HashMap<String,DataTable>();
	
	public DataTable getTable(String name) {
		return tableInfo.get(name);
	}
	
	public void addTable(DataTable dataTable) {
		tableInfo.put(dataTable.getDataSourceName(),dataTable);
	}
	
	/*public DataTable getTableByDataSource(String name){
		return tableRefDataSource.get(name);
	}*/
}

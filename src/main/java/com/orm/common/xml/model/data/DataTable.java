package com.orm.common.xml.model.data;

import java.util.HashMap;
import java.util.Map;


public class DataTable {

	public static final String NAME = "name";
	
	public static final String DATA_SOURCE="data-source";
	
	public static final String KEY = "key";
	
	public static final String KEY_TYPE = "key-type";
	
	public static final String COLUMN = "column";
	
	public static final String SEQ = "seq";
	
	public static final String CON = "con";
	
	private String name;
	
	private String dataSourceName;
	
	private String key;
	
	private String keyType;
	
	private String propertyKey;
	
	private String seq;
	
	private String con;
	
	public String getPropertyKey() {
		return propertyKey;
	}

	public void setPropertyKey(String propertyKey) {
		this.propertyKey = propertyKey;
	}

	private Map<String,Column> columnInfo = new HashMap<String,Column>();

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Column getColumn(String name) {
		return columnInfo.get(name);
	}

	public void addColumn(Column column) {
		columnInfo.put(column.getRefproperty(),column);
	}
	
	public String getKeyType() {
		return keyType;
	}

	public void setKeyType(String keyType) {
		this.keyType = keyType;
	}
	
	public Map<String,Column> getColumns() {
		return columnInfo;
	}

	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}

	public String getCon() {
		return con;
	}

	public void setCon(String con) {
		this.con = con;
	}
	
}

package com.orm.common.xml.model.config;

import com.orm.common.convert.container.ConvertContainer;
import com.orm.common.execute.container.ExecuteContainer;

public class ConnectionInfo extends ConfigBaseData{

	public static final String NAME = "name";
	
	public static final String TYPE = "type";
	
	public static final String CONNECTION = "connection";
	
	public static final String CONVERT = "convert";
	
	public static final String EXECUTER = "executer";
	
	public static final String DATA_CONVERT = "data-convert";
	
	private String name;
	
	private String type;
	
	private String connectionClass;
	
	private String convertClass;
	
	private String executeClass;

	private String dataConvertClass;
	
	private ConvertContainer convertContainer;
	
	private ExecuteContainer executeContainer;
	
	private com.orm.sql.datatype.convert.ConvertContainer dataConvertContainer;
	
	private Class conClass;
	
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

	public String getConnectionClass() {
		return connectionClass;
	}

	public void setConnectionClass(String connectionClass) {
		this.connectionClass = connectionClass;
	}

	public String getConvertClass() {
		return convertClass;
	}

	public void setConvertClass(String convertClass) {
		this.convertClass = convertClass;
	}

	public String getExecuteClass() {
		return executeClass;
	}

	public void setExecuteClass(String executeClass) {
		this.executeClass = executeClass;
	}

	public ConvertContainer getConvertContainer() {
		return convertContainer;
	}

	public void setConvertContainer(ConvertContainer convertContainer) {
		this.convertContainer = convertContainer;
	}

	public ExecuteContainer getExecuteContainer() {
		return executeContainer;
	}

	public void setExecuteContainer(ExecuteContainer executeContainer) {
		this.executeContainer = executeContainer;
	}

	public Class getConClass() {
		return conClass;
	}

	public void setConClass(Class conClass) {
		this.conClass = conClass;
	}

	public String getDataConvertClass() {
		return dataConvertClass;
	}

	public void setDataConvertClass(String dataConvertClass) {
		this.dataConvertClass = dataConvertClass;
	}

	public com.orm.sql.datatype.convert.ConvertContainer getDataConvertContainer() {
		return dataConvertContainer;
	}

	public void setDataConvertContainer(
			com.orm.sql.datatype.convert.ConvertContainer dataConvertContainer) {
		this.dataConvertContainer = dataConvertContainer;
	}
	
	
	
}

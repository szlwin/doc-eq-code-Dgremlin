package com.orm.sql.connection;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.orm.common.convert.container.ConvertContainer;
import com.orm.common.execute.container.ExecuteContainer;
import com.orm.common.execute.exception.ExecuteException;
import com.orm.connection.DataConnection;
import com.orm.context.data.DataUtil;
import com.orm.sql.dom.ConvertInfo;
import com.orm.sql.dom.ConvertParam;
import com.orm.sql.dom.DataInfo;
import com.orm.sql.dom.ExecuteInfo;
import com.orm.sql.dom.ExecuteParam;
import com.orm.sql.dom.QueryInfo;

public abstract class AbstractConnection<E,V> implements DataConnection<E,V>{

	protected String conName;
	
	protected String dataSource;
	
	protected Object con;
	
	protected ConvertContainer sqlConvertContainer;

	protected ExecuteContainer sqlExecuteContainer;
	

	public void setConvertContainer(ConvertContainer convertContainer) {
		this.sqlConvertContainer =  convertContainer;
		
	}
	
	public void setExecuteContainer(ExecuteContainer sqlExecuteContainer) {
		this.sqlExecuteContainer =  sqlExecuteContainer;
		
	}
	
	public String getConName() {
		return conName;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setConName(String name) {
		this.conName = name;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}
}

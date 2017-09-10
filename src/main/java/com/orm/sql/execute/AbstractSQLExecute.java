package com.orm.sql.execute;

import java.sql.Connection;
import java.util.Collection;

import com.orm.common.execute.AbstrcatExecute;
import com.orm.sql.dom.DataInfo;
import com.orm.sql.dom.ExecuteInfo;

public abstract class AbstractSQLExecute extends AbstrcatExecute<Collection<DataInfo>, ExecuteInfo, Connection> implements SQLExecute{

	protected Connection con;
	
	protected Collection<DataInfo> dataInfoCollection;
	
	

	public void setConnection(Connection con) {
		this.con = con;
	}

	public void setValue(Collection<DataInfo> e) {
		this.dataInfoCollection = e;
		
	}

}

package com.orm.model.execute.rule.data;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.common.xml.model.view.ViewData;
import com.orm.connection.DataConnection;

public interface Execute<E> {

	public boolean execute() throws ExecuteException;
	
	public void setSql(String sql);
	
	public void setValue(E e);
	
	public void setPropertyName(String name);
	
	public void setViewData(ViewData viewData);
	
	//public void setConnection(Connection con);
	
	public void setDataSource(String name);
	
	public void setDataConnection(DataConnection<?, ?> con);
	
	public void setType(String type);
}

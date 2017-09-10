package com.orm.sql.execute.container;

import java.util.HashMap;
import java.util.Map;

import com.orm.sql.dom.QueryInfo;
import com.orm.sql.execute.GetExecute;
import com.orm.sql.execute.InsertExecute;
import com.orm.sql.execute.QueryExecute;
import com.orm.sql.execute.SQLExecute;
import com.orm.sql.execute.UpdateExecute;

public class DefaultSqlExecuteContainer extends AbstractSqlExecuteContainer{

	protected Map<String,Class<? extends SQLExecute>> executeMap 
		= new HashMap<String,Class<? extends SQLExecute>>(5);
	
	public void init() {
		executeMap.put("get", GetExecute.class);
		executeMap.put("insert", InsertExecute.class);
		executeMap.put("query", QueryExecute.class);
		executeMap.put("update", UpdateExecute.class);
		executeMap.put("delete", UpdateExecute.class);
	}
	
	protected SQLExecute getExecute(QueryInfo e) {
		SQLExecute execute = null;
		try {
			execute = executeMap.get(e.getType()).newInstance();
		} catch (Exception e1) {
	
		} 
		
		return execute;
	}
}

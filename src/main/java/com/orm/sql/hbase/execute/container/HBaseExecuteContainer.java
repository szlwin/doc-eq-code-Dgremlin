package com.orm.sql.hbase.execute.container;

import com.orm.sql.execute.GetExecute;
import com.orm.sql.execute.InsertExecute;
import com.orm.sql.execute.QueryExecute;
import com.orm.sql.execute.UpdateExecute;
import com.orm.sql.execute.container.DefaultSqlExecuteContainer;

public class HBaseExecuteContainer extends DefaultSqlExecuteContainer{

	public void init() {
		executeMap.put("get", GetExecute.class);
		executeMap.put("insert", InsertExecute.class);
		executeMap.put("query", QueryExecute.class);
		executeMap.put("update", UpdateExecute.class);
		executeMap.put("delete", UpdateExecute.class);
	}
}

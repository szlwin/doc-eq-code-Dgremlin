package com.orm.sql.execute.container;

import java.sql.Connection;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.sql.dom.ExecuteInfo;
import com.orm.sql.dom.QueryInfo;
import com.orm.sql.execute.SQLExecute;

public abstract class AbstractSqlExecuteContainer implements SqlExecuteContainer{

	public ExecuteInfo execute(QueryInfo e) throws ExecuteException {
		SQLExecute sqlExecute = getExecute(e);
		
		sqlExecute.setCmd(e.getSql());
		sqlExecute.setValue(e.getDataInfoCollection());
		sqlExecute.setConnection((Connection) e.getCon());
		sqlExecute.setKeyType(e.getKeyType());
		sqlExecute.setKeyValue(e.getKeyValue());
		sqlExecute.execute();
		return sqlExecute.getResult();
	}

	protected abstract SQLExecute getExecute(QueryInfo e);

}

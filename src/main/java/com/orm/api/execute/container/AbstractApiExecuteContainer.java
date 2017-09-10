package com.orm.api.execute.container;

import java.sql.Connection;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.api.dom.ApiQueryInfo;
import com.orm.sql.dom.ExecuteInfo;
import com.orm.sql.execute.SQLExecute;

public abstract class AbstractApiExecuteContainer implements ApiExecuteContainer{

	public ExecuteInfo execute(ApiQueryInfo e) throws ExecuteException {
		SQLExecute sqlExecute = getExecute(e);
		
		sqlExecute.setCmd(e.getSql());
		sqlExecute.setValue(e.getDataInfoCollection());
		sqlExecute.setConnection((Connection) e.getCon());
		sqlExecute.setKeyType(e.getKeyType());
		sqlExecute.setKeyValue(e.getKeyValue());
		sqlExecute.execute();
		return sqlExecute.getResult();
	}


	protected abstract SQLExecute getExecute(ApiQueryInfo e);

}

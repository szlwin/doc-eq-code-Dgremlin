package dec.external.datasource.sql.execute.container;

import java.sql.Connection;

import dec.core.datasource.execute.dom.ExecuteInfo;
import dec.core.datasource.execute.exception.ExecuteException;
import dec.external.datasource.sql.dom.QueryInfo;
import dec.external.datasource.sql.execute.SQLExecute;



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

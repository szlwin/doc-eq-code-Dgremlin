package dec.external.datasource.sql.mysql.connection.factory;

import dec.core.datasource.connection.DataConnection;
import dec.core.datasource.connection.factory.DBConectionFacory;
import dec.core.datasource.execute.dom.ExecuteInfo;
import dec.external.datasource.sql.connection.DataBaseConnection;
import dec.external.datasource.sql.dom.ExecuteParam;

public class MySQLDBConnectionFactory implements DBConectionFacory<ExecuteParam,ExecuteInfo>{

	@Override
	public DataConnection<ExecuteParam,ExecuteInfo> getDataConnection() {

		return new DataBaseConnection();
	}

}

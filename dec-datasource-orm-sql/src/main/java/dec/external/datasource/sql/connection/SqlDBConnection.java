package dec.external.datasource.sql.connection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

import dec.core.datasource.connection.DataConnection;

public interface SqlDBConnection<E,V> extends DataConnection<E,V>{

	Connection getConnection();
	
	Savepoint getSavepoint(String name) throws SQLException;
	
	Savepoint getSavepoint() throws SQLException;
	
	void rollback(Savepoint savepoint) throws SQLException;
	
	void releaseSavepoint(Savepoint savepoint)throws SQLException;
}

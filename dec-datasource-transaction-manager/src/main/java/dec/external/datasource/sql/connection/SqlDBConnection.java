package dec.external.datasource.sql.connection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

import dec.core.datasource.connection.DataConnection;

public interface SqlDBConnection<E,V> extends DataConnection<E,V>{

	public Connection getConnection();
	
	public Savepoint getSavepoint(String name) throws SQLException;
	
	public Savepoint getSavepoint() throws SQLException;
	
	public void rollback(Savepoint savepoint) throws SQLException;
	
	public void releaseSavepoint(Savepoint savepoint)throws SQLException;
}

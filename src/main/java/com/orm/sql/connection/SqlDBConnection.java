package com.orm.sql.connection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

import com.orm.connection.DataConnection;
import com.orm.sql.dom.ExecuteInfo;
import com.orm.sql.dom.ExecuteParam;

public interface SqlDBConnection<E,V> extends DataConnection<E,V>{

	public Connection getConnection();
	
	public Savepoint getSavepoint(String name) throws SQLException;
	
	public Savepoint getSavepoint() throws SQLException;
	
	public void rollback(Savepoint savepoint) throws SQLException;
	
	public void releaseSavepoint(Savepoint savepoint)throws SQLException;
}

package dec.external.datasource.sql.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import dec.core.datasource.dom.DataInfo;

public abstract class AbstractQuery<E> implements Query<E>{
	
	protected Connection connection;
	
	protected String sqlString;
	
	protected Collection<DataInfo> paramCollection;
	
	protected PreparedStatement preStatement;
	
	public AbstractQuery(Connection con){
		connection = con;
	}
	
	public AbstractQuery(Connection con,String sql,Collection<DataInfo> param){
		connection = con;
		sqlString = sql;
		paramCollection = param;
	}
	
	protected abstract PreparedStatement createStatement() throws SQLException;
	
}

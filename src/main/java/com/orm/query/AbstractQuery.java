package com.orm.query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.connection.DataConnection;
import com.orm.connection.exception.ConectionException;
import com.orm.connection.factory.DataConnectionFactory;
import com.orm.sql.util.Util;

public abstract class AbstractQuery<E,V> implements Query<E,V>{
	
	Log log = LogFactory.getLog(AbstractQuery.class);
	
	protected DataConnection con;
	
	protected String dataSourceName;
	
	protected String connectionName;
	
	public AbstractQuery(String connectionName){
		this.connectionName = con.getConName();
		this.dataSourceName = con.getDataSource();
	}
	
	public AbstractQuery(){
		this.dataSourceName = Util.getDataSource().getName();
	}
	
	protected void initConnection() throws ClassNotFoundException, ConectionException{
		if(con != null)
			return;
		
		con = DataConnectionFactory.getInstance().getConnection(connectionName);
		
		con.connect();
	}
	
	protected void close() throws ConectionException{
		if(con != null)
			con.close();
	}
	
	public final void executeQuery() throws ExecuteException{
		try{
			
			initConnection();

			
			execute();
			
		}catch(ClassNotFoundException e){
			log.error("The connection config is error!",e);
			throw new ExecuteException("The connection is error!");
		}catch(ConectionException e){
			log.error("The connection is error!",e);
			throw new ExecuteException(e.getMessage());
		}finally{
			try{
				close();
			}catch(ConectionException e){
				throw new ExecuteException(e.getMessage());
			}
			
		} 
		
	}
	
	protected abstract void execute() throws ExecuteException;
}

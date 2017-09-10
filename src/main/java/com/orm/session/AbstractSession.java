package com.orm.session;

import com.orm.connection.DataConnection;
import com.orm.connection.exception.ConectionException;
import com.orm.connection.factory.DataConnectionFactory;

public abstract class AbstractSession<E> implements Session<E> {

	protected DataConnection con;
	
	protected String conName;
	
	public AbstractSession(){
		
	}
	
	public AbstractSession(String conName){
		this.conName = conName;
	}
	
	public void begian() throws ConectionException{
			if(con == null || con.isClosed()){
				con = null;
				con = DataConnectionFactory.getInstance().getConnection(conName);
				con.connect();
			}
	}
	
	public void commit() throws ConectionException{
		if(con != null)
			con.commit();
	}
	
	public void close() throws ConectionException {
		if(con != null )
			con.close();
	}
	
	public void rollback() throws ConectionException {
		if(con != null)
			con.rollback();
	}
}

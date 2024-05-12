package dec.core.session;

import dec.core.datasource.connection.DataConnection;
import dec.core.datasource.connection.exception.ConectionException;
import dec.core.model.connection.DataConnectionFactory;


//import dec.core.connection.DataConnection;
//import dec.core.connection.exception.ConectionException;
//import dec.core.connection.factory.DataConnectionFactory;

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

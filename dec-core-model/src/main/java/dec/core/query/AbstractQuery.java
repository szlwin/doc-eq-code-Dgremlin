package dec.core.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dec.core.context.config.utils.ConfigContextUtil;
import dec.core.datasource.connection.DataConnection;
import dec.core.datasource.connection.exception.ConectionException;
import dec.core.datasource.execute.exception.ExecuteException;
import dec.core.model.connection.DataConnectionFactory;

/*import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dec.core.common.execute.exception.ExecuteException;
import dec.core.connection.DataConnection;
import dec.core.connection.exception.ConectionException;
import dec.core.connection.factory.DataConnectionFactory;
import dec.core.sql.util.Util;*/

public abstract class AbstractQuery<E,V> implements Query<E,V>{
	
	private final static Logger log = LoggerFactory.getLogger(AbstractQuery.class);
	
	protected DataConnection con;
	
	protected String dataSourceName;
	
	protected String connectionName;
	
	public AbstractQuery(String connectionName){
		this.connectionName = con.getConName();
		this.dataSourceName = con.getDataSource();
	}
	
	public AbstractQuery(){
		this.dataSourceName = ConfigContextUtil.getDataSource().getName();
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

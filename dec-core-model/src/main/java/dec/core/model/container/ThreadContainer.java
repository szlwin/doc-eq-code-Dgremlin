package dec.core.model.container;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dec.core.datasource.connection.DataConnection;
import dec.core.datasource.connection.exception.ConectionException;
import dec.core.model.connection.DataConnectionFactory;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

//import dec.core.connection.DataConnection;
//import dec.core.connection.exception.ConectionException;
//import dec.core.connection.factory.DataConnectionFactory;
import dec.core.model.execute.rule.RuleContainer;
//import dec.core.sql.connection.DataBaseConnection;

public class ThreadContainer {

	private final static Logger log = LoggerFactory.getLogger(ThreadContainer.class);
	
	protected Map<String,DataConnection<?,?>> conMap = new HashMap<String,DataConnection<?,?>>();
	
	private String name;
	
	private int number = -1;
	
	private long timeOut = -1;
	
	private int currentNumner;
	
	private boolean isError = false;
	
	public ResultInfo execute(ModelLoader modelLoader) throws Exception{
		
		if(isError){
			log.error("The thread container: "+name+" have an error!");
			return null;
		}
		
		log.info("Execute the view rule: "+modelLoader.getRuleName()+" start!");
		
		boolean isSuccess = true;

		ResultInfo resultInfo = null;
		try{
			createConnection(modelLoader.getConName());
			
			RuleContainer ruleExecute = new RuleContainer(modelLoader,conMap.get(modelLoader.getConName()));
			
			resultInfo = ruleExecute.execute();
		}catch(Exception e){
			log.error("Execute error", e);
			throw e;
		}finally{
			this.setError(!(isSuccess & resultInfo.isSuccess()));
			end();
		}
		
		log.info("Execute the view rule: "+modelLoader.getRuleName()+" end!");
		
		return resultInfo;
	}
	
	private void createConnection(String conName) throws ConectionException{
		if(!conMap.containsKey(conName)){
			DataConnection<?,?> con = DataConnectionFactory.getInstance().getConnection(conName);
			
			con.connect();
			conMap.put(conName, con);
		}

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public long getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(long timeout) {
		this.timeOut = timeout;
	}

	public boolean isError() {
		return isError;
	}

	private void setError(boolean isError) {
		this.isError = isError;
	}
	
	public void end() throws ConectionException{
		if(isError){
			try {
				roolback();
			} catch (ConectionException e) {
				throw e;
			}finally{
				close();
			}
			return;
		}
		
		if(isCanCommit()){
			try {
				commit();
			} catch (ConectionException e) {
				throw e;
			}finally{
				close();
			}
		}
		
	}
	
	private boolean isCanCommit(){
		if(number < 0 )
			return false;
		
		if(number >= currentNumner){
			return true;
		}

		return false;
	}
	
	private void commit() throws ConectionException{
		operator(0);
	}
	
	private void close() throws ConectionException{
		operator(1);
	}
	
	private void roolback() throws ConectionException{
		operator(2);
	}
	
	protected void operator(int type) throws ConectionException{
		
		Collection<DataConnection<?,?>> conCollection = conMap.values();
		Iterator<DataConnection<?,?>> it = conCollection.iterator();
		
		while(it.hasNext()){
			DataConnection<?,?> con = it.next();
			
			if(con == null)
				continue;
			
			switch(type){
				case 0:
					con.commit();
					break;
				case 1:
					try{
						con.close();
					}catch(ConectionException e){
						
					}
					break;
				case 2:
					con.rollback();
					break;
			}
		}
	}

}

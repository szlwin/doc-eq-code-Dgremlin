package dec.core.model.execute.tran.advance;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dec.core.datasource.connection.DataConnection;
import dec.core.datasource.connection.exception.ConectionException;
import dec.core.model.connection.DataConnectionFactory;
//import dec.core.connection.DataConnection;
//import dec.core.connection.exception.ConectionException;
//import dec.core.connection.factory.DataConnectionFactory;
import dec.core.model.container.ModelLoader;
import dec.core.model.container.ResultInfo;
import dec.core.model.execute.tran.DefaultExecuter;
import dec.core.model.execute.tran.TranExecuter;
import dec.core.model.execute.tran.Transaction;
//import dec.core.sql.connection.DataBaseConnection;
//import dec.core.sql.connection.SqlDBConnection;
import dec.external.datasource.sql.connection.SqlDBConnection;

public class TranAdvanceContainer {
	
	private final static Logger log = LoggerFactory.getLogger(TranAdvanceContainer.class);
	
	private static int TRAN_TYPE_MAIN = 0;
	
	private static int TRAN_TYPE_NEW = 1;
	
	private static int TRAN_TYPE_NO = 2;
	
	
	
	private List<TranExecuter> executerList = new ArrayList<TranExecuter>();
	
	private Map<String,DataConnection> conDefaultMap = new HashMap<String,DataConnection>();
	
	private Map<String,List<TranExecuter>> executerRnewMapList = new HashMap<String,List<TranExecuter>>();
	
	private List<TranExecuter> executerNoSupportList = new ArrayList<TranExecuter>();
	
	private List<ResultInfo> resultInfoList = new ArrayList<ResultInfo>(15);
	
	private int conFlag = 1;
	
	private volatile boolean isAllSuccess = true;
	
	private String name;
	
	private long flag;
	
	private boolean isNewOk = true;
	
	private boolean isMainSuccess = true;
	
	private Object lock = new Object();
	
	private Object conLock = new Object();
	
	private static int defaultThreadNum = 4;
	
	//private int count;
	public String getName() {
		return name;
	}

	public TranAdvanceContainer(String name){
		this.name = name;
	}
	
	public void load(ModelLoader modelLoader,int type){
		
		TranExecuter tranExecuter = createTranExecuter(modelLoader,type);
		
		if(type == Transaction.PROPAGATION_REQUIRES_NEW){
			addRewTranExecuter(tranExecuter,null);
		}else{
			addTranExecuter(tranExecuter);
		}
	}
	
	
	public void loadRew(ModelLoader modelLoader,String name){
		
		TranExecuter tranExecuter = createTranExecuter(modelLoader,Transaction.PROPAGATION_REQUIRES_NEW);
		
		addRewTranExecuter(tranExecuter,name);
	}
	
	public void load(ModelLoader modelLoader){
		load(modelLoader,Transaction.PROPAGATION_REQUIRED);
	}
	
	private TranExecuter createTranExecuter(ModelLoader modelLoader,int type){
		TranExecuter tranExecuter = new DefaultExecuter(flag);
		tranExecuter.setName(modelLoader.getRuleName());
		tranExecuter.load(modelLoader);
		tranExecuter.setTranType(type);
		return tranExecuter;
	}
	
	private class Task implements Callable<List<ResultInfo>>{
		private List<TranExecuter> tranExecuterList;
		private List<ResultInfo> resultInfoList;
		public Task(int size){
			tranExecuterList = new ArrayList<TranExecuter>(size);
			resultInfoList = new ArrayList<ResultInfo>(size);
		}

		public void add(TranExecuter tranExecuter){
			tranExecuterList.add(tranExecuter);
		}
		
		public List<ResultInfo> call() throws Exception {
			for(int i = 0 ; i < tranExecuterList.size();i++){
				ResultInfo resultInfo = executeSingle(tranExecuterList.get(i));
				resultInfoList.add(resultInfo);
			}
			return resultInfoList;
		}
	}
	
	public class TaskExecuter extends Thread{
		private List<TranExecuter> tranExecuterList;
		private List<ResultInfo> resultInfoList;
		
		public TaskExecuter(int size){
			tranExecuterList = new ArrayList<TranExecuter>(size);
			resultInfoList = new ArrayList<ResultInfo>(size);
		}

		public void add(TranExecuter tranExecuter){
			tranExecuterList.add(tranExecuter);
		}
		
		public void run() {
			for(int i = 0 ; i < tranExecuterList.size();i++){
				ResultInfo resultInfo = executeSingle(tranExecuterList.get(i));
				resultInfoList.add(resultInfo);
			}
			//synchronized(conLock){
			//	count++;
			//}
		}
		
		public List<ResultInfo> getResultInfoList(){
			return resultInfoList;
		}
	}
	
	private void execute(List<TranExecuter> list,int type) throws ConectionException, SQLException{
		if(list.isEmpty()){
			return;
		}

		int index = 0;
		int maxCount = 0;
		if(list.size() < defaultThreadNum){
			maxCount = list.size();
		}else{
			maxCount = list.size()/defaultThreadNum;
		}
		
		List<Callable<List<ResultInfo>>> taskList = new ArrayList<Callable<List<ResultInfo>>>();
		
		for(int j =0;j < defaultThreadNum;j++){
			Task task = new Task(maxCount);
			for(int count = 0; index < list.size() && count < maxCount; index++,count++){
				TranExecuter tranExecuter = list.get(index);
				task.add(tranExecuter);
			}
			taskList.add(task);
		}
		
		if(index < list.size()-1){
			Task task = (Task) taskList.get(0);
			for(;index < list.size(); index++){
				TranExecuter tranExecuter = list.get(index);
				task.add(tranExecuter);
			}
		}
		
		ExecutorService  executor = Executors.newScheduledThreadPool(defaultThreadNum);
		
		List<Future<List<ResultInfo>>> taskResultList = null;
		
		try {
			taskResultList = executor.invokeAll(taskList);
		} catch (InterruptedException e) {
			setErrorFlag();
			log.error("Executing the tran,id:"+flag+",occur an error! ",e);
		}
		
		executor.shutdown();
		
		try {
			for(int i = 0; i < taskResultList.size(); i++){
				List<ResultInfo> resultInfoList = taskResultList.get(i).get();
				for(int j = 0; j < resultInfoList.size(); j++){
					ResultInfo resultInfo = resultInfoList.get(j);
					this.addResult(resultInfo);
				}
			}
		} catch (Exception e) {
			setErrorFlag();
			log.error("Get the resultInfo from rule: id="+flag+",name: "+name+" error!",e);
		}
		
		list.clear();
		
		if(type == TRAN_TYPE_NEW){
			boolean isSuccess = true;
			
			if(isNewOk){
				try {
					commitAll(false);
				} catch (ConectionException e) {
					isSuccess = false;
					setErrorFlag();
					log.error("Commit error", e);
				}
			}
			
			if(!isSuccess || !isNewOk){
				try {
					rollBackAll(false);
				} catch (ConectionException e) {
					setErrorFlag();
					log.error("", e);
				}
				isNewOk = true;
			}
		}
	}
	
	private ResultInfo executeSingle(TranExecuter tranExecuter){
		
		log.info("Executing the tran,id:"+flag+",name: "+name+",rule:"+tranExecuter.getName()+"  start!");
		
		boolean isSuccess = true;
		
		ResultInfo resultInfo = null;
		
		int tranType = tranExecuter.getTranType();
		
		//判断主事务或某个New事务是否已经出错
		if(((tranType == Transaction.PROPAGATION_REQUIRED
			|| tranType == Transaction.PROPAGATION_REQUIRED_NESTED
			|| tranType == Transaction.PROPAGATION_SUPPORTS)
				&& !isMainSuccess)
			|| (tranType == Transaction.PROPAGATION_REQUIRES_NEW && !isNewOk)){
			
			log.error("Executing the tran: id:"+flag+",name: "+name+" is error,so the rule: "+tranExecuter.getName()+" is not execute!");
			
			resultInfo = new ResultInfo();
			resultInfo.setSuccess(false);
			resultInfo.setRuleName(tranExecuter.getName());
			//addResult(resultInfo);
			
			return resultInfo;  
		}
		
		try {

			DataConnection con = createConnection(tranExecuter.getConName(),tranExecuter.getTranType());
			
			tranExecuter.setConnection(con);
			
			//嵌套事务设置Savepoint
			if(tranType == Transaction.PROPAGATION_REQUIRED_NESTED){
				setSavePoint(tranExecuter);
			}
			
			resultInfo = tranExecuter.execute();
			
		} catch (Exception e) {
			isSuccess = false;
			setErrorFlag();
			log.error("Execute the rule: id="+flag+","+tranExecuter.getName()+" error,an exception is catched!",e);
		} finally{
			//addResult(resultInfo);
			if(!isSuccess){
				resultInfo.setSuccess(false);
			}
			if(!resultInfo.isSuccess()){
				setErrorFlag();
				log.error("Execute the rule: id="+flag+","+tranExecuter.getName()+" fail!");
				
				try {
					finish(resultInfo.isSuccess(),tranExecuter);
				} catch (Exception e) {
					setErrorFlag();
					log.error("Commit or rollback the rule: id="+flag+","+tranExecuter.getName()+" error!",e);
				}
			}
		}
		log.info("Executing the tran,id:"+flag+",name: "+name+",rule:"+tranExecuter.getName()+"  end!");
		return resultInfo;
	}
	
	//private DataBaseConnection getConnection(String name) {
	//	return conDefaultMap.get(name);
	//}

	private DataConnection getConnection(String conName) {
		return conDefaultMap.get(conName);
	}

	public void execute() throws ConectionException, SQLException{
		
		log.info("Execute the tran: "+name+"  start!");
		
		execute(executerNoSupportList,TRAN_TYPE_NO);
		
		if(!executerRnewMapList.isEmpty()){
			Collection<List<TranExecuter>> collection = executerRnewMapList.values();
			Iterator<List<TranExecuter>> it = collection.iterator();
			while(it.hasNext()){
				List<TranExecuter> list = it.next();
				execute(list,TRAN_TYPE_NEW);
			}
		}
		
		execute(executerList,TRAN_TYPE_MAIN);
		
		endAll();
		
		log.info("Execute the tran: id="+flag+" ,name="+name+"  end!");
	}
	
	public List<ResultInfo> getResult(){
		return resultInfoList;
	}
	
	public boolean getSuccess(){
		return isAllSuccess;
	}
	
	private void addResult(ResultInfo resultInfo){
		//synchronized(addLock){
			resultInfoList.add(resultInfo);
		//}
	}
	
	private DataConnection createConnection(String conName,int type) throws ConectionException, SQLException{
		synchronized(conLock){
			if(type == Transaction.PROPAGATION_REQUIRED 
					|| type == Transaction.PROPAGATION_REQUIRED_NESTED){
				return getDefaultConnection(conName);
			}
			
			if(type == Transaction.PROPAGATION_REQUIRES_NEW){
				return getRewConnection(conName);
			}
			
			if(type == Transaction.PROPAGATION_SUPPORTS){
				DataConnection con = null;
				if(conDefaultMap.containsKey(conName)){
					con = conDefaultMap.get(conName);
				}else{
					con =  getNoSupportConnection(conName);
				}
				return con;
			}
			
			if(type == Transaction.PROPAGATION_NOT_SUPPORTED){
				return getNoSupportConnection(conName);
			}
			return null;
		}
	}
	
	public void finish(boolean isSuccess,TranExecuter tranExecuter) throws ConectionException, SQLException{
		
		int tranType = tranExecuter.getTranType();

		switch(tranType){
			case Transaction.PROPAGATION_REQUIRED:
				finishRequired(isSuccess,tranExecuter);
				break;
			case Transaction.PROPAGATION_REQUIRES_NEW:
				finishRnew(isSuccess,tranExecuter);
				break;
			case Transaction.PROPAGATION_REQUIRED_NESTED:
				finishNested(isSuccess,tranExecuter);
				break;
			case Transaction.PROPAGATION_SUPPORTS:
				finishSupport(isSuccess,tranExecuter);
				break;
		}
	}
	
	private void finishRnew(boolean isSuccess,TranExecuter tranExecuter) throws ConectionException{
		//DataBaseConnection con = tranExecuter.getConnection();
		if(!isSuccess){
			synchronized(lock){
				if(!isNewOk){
					return;
				}
				isNewOk = false;
				this.rollBackAll(false);
			}

		}
	}
	
	private void finishRequired(boolean isSuccess,TranExecuter tranExecuter) throws ConectionException{
		if(!isSuccess){
			//DataBaseConnection con = tranExecuter.getConnection();
			//conDefaultMap.remove(con.getConName());
			//con.rollback();
			synchronized(lock){
				if(!isMainSuccess)
					return;
				isMainSuccess = false;
				this.rollBackAll(true);
			}
			
			
		}
	}
	
	private void finishSupport(boolean isSuccess,TranExecuter tranExecuter) throws ConectionException{
		if(!isSuccess){
			synchronized(lock){
				DataConnection con = tranExecuter.getConnection();
				if(isMainSuccess && conDefaultMap.containsValue(con)){
					isMainSuccess = false;
					this.rollBackAll(true);
				}
			}
		}
	}
	
	private void finishNested(boolean isSuccess,TranExecuter tranExecuter) throws SQLException{
		if(!isSuccess){
			synchronized(lock){
				if(!isMainSuccess){
					return;
				}
				
				SqlDBConnection con = (SqlDBConnection) tranExecuter.getConnection();
				Savepoint savepoint = tranExecuter.getSavepoint();
				try{
					con.rollback(savepoint);
				}catch(SQLException e){
					log.error("Roll back error", e);
					throw e;
				}finally{
					try{
						con.releaseSavepoint(savepoint);
					}catch(SQLException e){
						log.error("Release savepoint error", e);
					}
				}
			}

		}
	}
	
	private void commitAll(boolean isClose) throws ConectionException{
		Collection<DataConnection> conCollection = conDefaultMap.values();
		Iterator<DataConnection> it = conCollection.iterator();
		
		while(it.hasNext()){
			DataConnection con = it.next();
			try {
				con.commit();
			} catch (ConectionException e) {
				log.error("Commit error", e);
				throw e;
			}finally{
				
				try {
					if(isClose){
						it.remove();
						con.close();
					}
				} catch (ConectionException e) {
					log.error("Close error", e);
				}
				
			}
		}
	}
	
	private void rollBackAll(boolean isClose) throws ConectionException{
		Collection<DataConnection> conCollection = conDefaultMap.values();
		Iterator<DataConnection> it = conCollection.iterator();
		
		while(it.hasNext()){
			DataConnection con = it.next();
			try {
				con.rollback();
			} catch (ConectionException e) {
				log.error("Roll back error", e);
			}finally{
				try {
					if(isClose){
						it.remove();
						con.close();
					}
				} catch (ConectionException e) {
					log.error("Close error", e);
				}
			}
		}
	}
	
	private void setSavePoint(TranExecuter tranExecuter) throws SQLException{
		SqlDBConnection con = (SqlDBConnection) tranExecuter.getConnection();
		String flag = String.valueOf(conFlag);
		
		Savepoint savepoint = con.getSavepoint(flag);
		tranExecuter.setSavepoint(savepoint);
		tranExecuter.setConnection(con);
		conFlag++;
	}
	
	private DataConnection getDefaultConnection(String conName) throws ConectionException, SQLException{
		DataConnection con = null;
		if(!conDefaultMap.containsKey(conName)){
			con = DataConnectionFactory.getInstance().getConnection(conName);
			con.connect();
			conDefaultMap.put(conName, con);
		}else{
			con = conDefaultMap.get(conName);
		}
		con.setAutoCommit(false);
		return con;
	}
	
	private DataConnection getRewConnection(String conName) throws ConectionException, SQLException{
		DataConnection con = getDefaultConnection(conName);
		return con;
	}
	
	private DataConnection getNoSupportConnection(String conName) throws ConectionException, SQLException{
		DataConnection con = getDefaultConnection(conName);
		con.setAutoCommit(true);
		return con;
	}
	
	private void setErrorFlag(){
		this.isAllSuccess = false;
	}
	
	private void addRewTranExecuter(TranExecuter tranExecuter,String key){
		if(key == null){
			key = "DEFAULT";
		}
		
		if(executerRnewMapList.containsKey(key)){
			executerRnewMapList.get(key).add(tranExecuter);
		}else{
			List<TranExecuter> list = new ArrayList<TranExecuter>();
			list.add(tranExecuter);
			executerRnewMapList.put(key, list);
		}
		
	}
	
	private void addTranExecuter(TranExecuter tranExecuter){
		int type = tranExecuter.getTranType();
		
		if(type == Transaction.PROPAGATION_SUPPORTS){
			if(!conDefaultMap.containsKey(
					tranExecuter.getConName())){
				executerList.add(tranExecuter);
			}else{
				executerNoSupportList.add(tranExecuter);
			}
			
			return;
		}
		
		if(type == Transaction.PROPAGATION_NOT_SUPPORTED){
			executerNoSupportList.add(tranExecuter);
			return;
		}
		
		executerList.add(tranExecuter);
	}

	public long getFlag() {
		return flag;
	}

	public void setFlag(long flag) {
		this.flag = flag;
	}
	
	public void clear(){
		conDefaultMap.clear();
		executerList.clear();
		executerRnewMapList.clear();
		executerNoSupportList.clear();
		resultInfoList.clear();
	}
	
	private void endAll() throws ConectionException{
		
		ConectionException ex = null;
		try {
			commitAll(true);
		} catch (ConectionException e) {
			ex = e;
			setErrorFlag();
			log.error("Commit error", e);
		}
		
		try {
			rollBackAll(true);
		} catch (ConectionException e) {
			ex = e;
			setErrorFlag();
			log.error("Roll back error", e);
		}
		
		//executerRnewMapList.clear();
		clear();
		
		if(ex != null){
			throw ex;
		}
	}
}

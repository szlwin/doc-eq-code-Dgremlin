package com.orm.model.execute.tran.advance.group;

import java.sql.SQLException;
import java.sql.Savepoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.orm.connection.exception.ConectionException;
import com.orm.model.container.ModelLoader;
import com.orm.model.container.ResultInfo;
import com.orm.model.execute.tran.TranExecuter;
import com.orm.model.execute.tran.Transaction;
import com.orm.sql.connection.DataBaseConnection;
import com.orm.sql.connection.SqlDBConnection;

public class MainTranGroup extends TranLevelGroup{

	private Log log = LogFactory.getLog(TranLevelGroup.class);
	
	private boolean isHasError;
	
	@Override
	protected ResultInfo executeSingle(TranExecuter tranExecuter) {
		
		log.info("Executing the tran,id:"+tranFlag+"name: "+null+",rule:"+tranExecuter.getName()+"  start!");
		
		boolean isSuccess = true;
		
		ResultInfo resultInfo = null;
		
		int tranType = tranExecuter.getTranType();
		
		//判断主事务是否已经出错
		if(isHasError){
			
			log.error("Executing the tran,id:"+tranFlag+"name: "+null+" is error,the main tran is error,so the rule: "+tranExecuter.getName()+" is not execute!");
			
			resultInfo = new ResultInfo();
			resultInfo.setSuccess(false);
			resultInfo.setRuleName(tranExecuter.getName());
			return resultInfo;  
		}
		
		try {

			DataBaseConnection con = createConnection(tranExecuter.getConName(),tranExecuter.getTranType());
			
			tranExecuter.setConnection(con);
			
			//嵌套事务设置Savepoint
			if(tranType == Transaction.PROPAGATION_REQUIRED_NESTED){
				setSavePoint(tranExecuter);
			}
			
			resultInfo = tranExecuter.execute();
			
		} catch (Exception e) {
			isSuccess = false;
			setErrorFlag();
			log.error("Execute the rule: id="+tranFlag+","+tranExecuter.getName()+" error,an exception is catched!",e);
		} finally{
			
			
			if(!resultInfo.isSuccess()){
				setErrorFlag();
				log.error("Execute the rule: id="+tranFlag+","+tranExecuter.getName()+" fail!");
			}
			try {
				finish(isSuccess && resultInfo.isSuccess(),tranExecuter);
			} catch (Exception e) {
				setErrorFlag();
				log.error("Commit or rollback the rule: id="+tranFlag+","+tranExecuter.getName()+" error!",e);
			}
			
		}
		log.info("Executing the tran,id:"+tranFlag+"name: "+null+",rule:"+tranExecuter.getName()+"  end!");
		return null;
	}

	
	private void setSavePoint(TranExecuter tranExecuter) {
		// TODO Auto-generated method stub
		
	}


	private DataBaseConnection createConnection(String conName, int tranType) {
		// TODO Auto-generated method stub
		return null;
	}


	private void setErrorFlag(){
		this.isHasError = true;
	}
	
	private void finish(boolean isSuccess,TranExecuter tranExecuter) throws ConectionException, SQLException{
		
		int tranType = tranExecuter.getTranType();
		switch(tranType){
			case Transaction.PROPAGATION_REQUIRED:
				finishRequired(isSuccess,tranExecuter);
				break;
			case Transaction.PROPAGATION_REQUIRED_NESTED:
				finishNested(isSuccess,tranExecuter);
				break;
		}
	}
	
	private void finishRequired(boolean isSuccess,TranExecuter tranExecuter) throws ConectionException{
		if(!isSuccess){
			//DataBaseConnection con = tranExecuter.getConnection();
			//conDefaultMap.remove(con.getConName());
			//con.rollback();
			isHasError = true;
			this.rollBackAll(true);
		}
	}
	
	private void rollBackAll(boolean b) {
		// TODO Auto-generated method stub
		
	}


	private void finishNested(boolean isSuccess,TranExecuter tranExecuter) throws SQLException{
		if(!isSuccess){
			SqlDBConnection con = (SqlDBConnection) tranExecuter.getConnection();
			Savepoint savepoint = tranExecuter.getSavepoint();
			try{
				con.rollback(savepoint);
			}catch(SQLException e){
				log.error(e);
				throw e;
			}finally{
				try{
					con.releaseSavepoint(savepoint);
				}catch(SQLException e){
					log.error(e);
				}
			}
		}
	}
}


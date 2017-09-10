package com.orm.model.execute.tran.advance.group;

import java.util.ArrayList;
import java.util.List;

import com.orm.model.container.ModelLoader;
import com.orm.model.container.ResultInfo;
import com.orm.model.execute.tran.DefaultExecuter;
import com.orm.model.execute.tran.TranExecuter;

public abstract class TranLevelGroup extends AbstractGroup{

	private List<TranExecuter> executerList = new ArrayList<TranExecuter>(15);
	
	public void load(ModelLoader modelLoader, int type) {
		TranExecuter tranExecuter = createTranExecuter(modelLoader,type);
		executerList.add(tranExecuter);
	}
	
	public void execute() {
		for(int i = 0; i < executerList.size(); i++){
			ResultInfo resultInfo = executeSingle(executerList.get(i));
			this.addResultInfo(resultInfo);
		}
	}
	
	protected TranExecuter createTranExecuter(ModelLoader modelLoader,int type){
		TranExecuter tranExecuter = new DefaultExecuter(tranFlag);
		tranExecuter.setName(modelLoader.getRuleName());
		tranExecuter.load(modelLoader);
		tranExecuter.setTranType(type);
		return tranExecuter;
	}
	
	protected abstract ResultInfo executeSingle(TranExecuter tranExecuter);
	
	/*
	private void executeSingle(TranExecuter tranExecuter){
		
		log.info("Executing the tran,id:"+tranFlag+"name: "+null+",rule:"+tranExecuter.getName()+"  start!");
		
		boolean isSuccess = true;
		
		ResultInfo resultInfo = null;
		
		int tranType = tranExecuter.getTranType();
		
		//判断主事务是否已经出错
		if((tranType == Transaction.PROPAGATION_REQUIRED
			|| tranType == Transaction.PROPAGATION_REQUIRED_NESTED
			|| tranType == Transaction.PROPAGATION_SUPPORTS)
				&& !isMainSuccess){
			
			log.error("Executing the tran,id:"+tranFlag+"name: "+null+" is error,the main tran is error,so the rule: "+tranExecuter.getName()+" is not execute!");
			
			resultInfo = new ResultInfo();
			resultInfo.setSuccess(false);
			resultInfo.setRuleName(tranExecuter.getName());
			addResult(resultInfo);
			return;  
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
			
			addResult(resultInfo);
			
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
	}*/
}

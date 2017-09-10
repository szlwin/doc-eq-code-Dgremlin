package com.orm.model.execute.tran.advance.back;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.orm.connection.exception.ConectionException;
import com.orm.model.container.ModelLoader;
import com.orm.model.container.ResultInfo;
import com.orm.model.execute.tran.TransactionContainer;
import com.orm.model.execute.tran.util.TranUtil;

public class TranExecuterThread extends Thread {

	private TranQueue tranQueue;
	
	private Log log = LogFactory.getLog(TranExecuterThread.class);
	
	public void run(){
		while(true){
			TranQueueNode node = null;
			while((node =tranQueue.get()) != tranQueue.getEnd()){
				
				TranNode tranNode = node.getTranNode();
				
				TransactionContainer transactionContainer = new TransactionContainer(tranNode.getName());
				
				boolean isSuccess = true;
				
				List<ModelLoader> list = tranNode.getModelList();
				
				transactionContainer.setFlag(tranNode.getId());
				
				for(int i = 0 ; i < list.size(); i++){
					transactionContainer.load(list.get(i));
				}
				
				try {
					transactionContainer.execute();
					
					isSuccess = transactionContainer.getSuccess();
					
				} catch (ConectionException e) {
					isSuccess = false;
					log.error(e);
				}finally{
					if(!isSuccess){
						notifyError(tranNode.getId(),transactionContainer.getResult());
						log.error("Execute the tran: "+ tranNode.getId()+":"+tranNode.getName()+" error!");
					}else{
						notifySuccess(tranNode.getId(),transactionContainer.getResult());
					}
				}
			}
		}
	}

	public TranQueue getTranQueue() {
		return tranQueue;
	}

	public void setTranQueue(TranQueue tranQueue) {
		this.tranQueue = tranQueue;
	}

	public void start(){
		super.start();
	}
	
	private void notifyError(long id,List<ResultInfo> list){
		TranUtil.notifyError(id, list);
	}
	
	private void notifySuccess(long id,List<ResultInfo> list){
		TranUtil.notifySuccess(id, list);
	}
	

}

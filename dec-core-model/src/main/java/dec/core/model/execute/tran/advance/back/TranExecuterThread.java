package dec.core.model.execute.tran.advance.back;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dec.core.datasource.connection.exception.ConectionException;
//import dec.core.connection.exception.ConectionException;
import dec.core.model.container.ModelLoader;
import dec.core.model.container.ResultInfo;
import dec.core.model.execute.tran.TransactionContainer;
import dec.core.model.execute.tran.util.TranUtil;

public class TranExecuterThread extends Thread {

	private TranQueue tranQueue;
	
	private final static Logger log = LoggerFactory.getLogger(TranExecuterThread.class);
	
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
					log.error("Execute error", e);
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

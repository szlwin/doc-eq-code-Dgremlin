package com.orm.model.execute.tran.advance.back;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.orm.model.container.ModelLoader;
import com.orm.model.container.ResultInfo;
import com.orm.model.execute.tran.Transaction;
import com.orm.model.execute.tran.util.TranUtil;

public class TranAdvanceContainer {

	private Log log = LogFactory.getLog(TranAdvanceContainer.class);
	
	private List<ModelLoader> mainTranList = new ArrayList<ModelLoader>();
	
	private List<ModelLoader> noTranList = new ArrayList<ModelLoader>();
	
	private Map<String,List<ModelLoader>> rewTranMapList = new HashMap<String,List<ModelLoader>>();
	
	private boolean isAllSuccess = false;
	
	private boolean isHasMain =  false;
	
	private List<ResultInfo> listInfo = new ArrayList<ResultInfo>();
	
	private int count;
	
	private int executeCount;
	
	private long startTime;
	
	private String name;
	
	private long id;
	
	private boolean isError = false;
	
	private boolean isFinish = false;
	
	private Object lock = new Object();
	
	private boolean isStart;
	
	public TranAdvanceContainer(String name){
		this.name = name;
	}
	
	public void load(ModelLoader modelLoader){
		load(modelLoader,Transaction.PROPAGATION_REQUIRED);
	}
	
	public void load(ModelLoader modelLoader,int type){
		count++;
		
		if(type == Transaction.PROPAGATION_REQUIRED
				|| type == Transaction.PROPAGATION_REQUIRED_NESTED){
			mainTranList.add(modelLoader);
			isHasMain = true;
			return;
		}
		
		if(type == Transaction.PROPAGATION_NOT_SUPPORTED){
			noTranList.add(modelLoader);
			return;
		}
		
		if(type == Transaction.PROPAGATION_REQUIRES_NEW){
			addRewTran(modelLoader,null);
			return;
		}
		
		if(type == Transaction.PROPAGATION_SUPPORTS){
			if(isHasMain){
				mainTranList.add(modelLoader);
			}else{
				noTranList.add(modelLoader);
			}
		}
	}
	
	public void loadRnew(ModelLoader modelLoader,String key){
		addRewTran(modelLoader,key);
	}
	
	private void addRewTran(ModelLoader modelLoader,String key){
		
		if(key == null){
			key = "DEFAULT";
		}
		
		if(rewTranMapList.containsKey(key)){
			rewTranMapList.get(key).add(modelLoader);
		}else{
			List<ModelLoader> list = new ArrayList<ModelLoader>();
			list.add(modelLoader);
			rewTranMapList.put(key, list);
		}
		
	}

	public void execute(){
		TranUtil.register(this);
		startTime = System.currentTimeMillis();
	}
	
	public boolean isAllSuccess() {
		return isAllSuccess;
	}

	public List<ResultInfo> getListInfo() {
		return listInfo;
	}

	protected List<ModelLoader> getMainTranList() {
		return mainTranList;
	}

	protected List<ModelLoader> getNoTranList() {
		return noTranList;
	}

	protected Map<String, List<ModelLoader>> getRewTranMapList() {
		return rewTranMapList;
	}
	
	public void waitExecute(long timeOut){
		while(true){
			synchronized(lock){
				if(executeCount == count){
					isFinish = true;
					isAllSuccess = !isError;
					TranUtil.notifyFinish(id);
					return;
				}else{
					if(System.currentTimeMillis() - timeOut > startTime){
						TranUtil.notifyFinish(id);
						return;
					}
				}
			}
				
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				log.error(e);
			}
		}
	}
	
	public void waitExecute(){
		waitExecute(6000);
	}

	public String getName() {
		return name;
	}
	
	protected void addResultList(List<ResultInfo> list){
		list.addAll(list);
	}
	
	protected void notifyError(List<ResultInfo> list){
		synchronized(lock){
			addResultList(list);
			doWithResult(false);
		}

	}
	
	protected void notifySuccess(List<ResultInfo> list){
		synchronized(lock){
			addResultList(list);
			doWithResult(true);
		}

	}

	protected void notifyStart(){
		this.isStart = true;
	}
	
	public long getId() {
		return id;
	}

	protected void setId(long id) {
		this.id = id;
	}

	public boolean isFinish() {
		boolean temp;
		synchronized(lock){
			temp = isFinish;
		}
		return temp;
	}
	
	private void doWithResult(boolean isOk){
		
		if(!isOk){
			isAllSuccess = false;
			isError = true;
		}
		
		executeCount++;
		
		if(executeCount == count){
			isFinish = true;
			isAllSuccess = !isError;
		}
			
	}

	public boolean isStart() {
		return isStart;
	}
	
}

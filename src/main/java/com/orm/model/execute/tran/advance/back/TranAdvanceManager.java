package com.orm.model.execute.tran.advance.back;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.orm.model.container.ModelLoader;
import com.orm.model.container.ResultInfo;
import com.orm.model.execute.tran.util.TranUtil;

public class TranAdvanceManager {

	private Log log = LogFactory.getLog(TranAdvanceManager.class);
	
	private TranExecuterThread tranExecuterThreadArray[];
	
	private TranQueue tranQueueArray[];
	
	private int threadCount = 2;
	
	private int otherThreadCount = 2;
	
	private int flag = otherThreadCount;
	
	private Object lock = new Object();
	
	private int NO_TRAN_INDEX = 0;
	
	private int NEW_TRAN_INDEX = 1;

	private static TranAdvanceManager tranAdvanceManager;
	
	private static Map<Long,TranAdvanceContainer> tranMap = new HashMap<Long,TranAdvanceContainer>(20);
	
	private static final ThreadLocal<TranAdvanceContainer> tranContainerLocal = new ThreadLocal<TranAdvanceContainer>();
	
	private TranProtecter tranProtecter;
	
	private TranAdvanceManager(){
		init();
	}
	
	private void load(){
		TranQueueIO tranQueueIO = new TranQueueIO();
		TranQueue tempTranQueueArray[] = tranQueueIO.read();
		if(tempTranQueueArray == null){
			return;
		}
		int length = threadCount+otherThreadCount;
		
		for(int i = 0;i < length;i++){
			tranQueueArray[i] = tempTranQueueArray[i];
		}
		tranQueueIO.remove();
	}
	
	public TranAdvanceContainer getTranAdvanceContainer(String name){
		TranAdvanceContainer tranAdvanceContainer = tranContainerLocal.get();
		
		if(tranAdvanceContainer == null){
			tranAdvanceContainer = new TranAdvanceContainer(name);
			tranAdvanceContainer.setId(TranUtil.getCurrentFlag());
			tranContainerLocal.set(tranAdvanceContainer);
		}
		return tranAdvanceContainer;
	}
	
	public static TranAdvanceManager getInstance(){
		if(tranAdvanceManager == null){
			tranAdvanceManager = new TranAdvanceManager();
		}
		return tranAdvanceManager;
	}
	
	private void init(){
		initTranQueue();
		initTranExecuterThread();
		initTranProtecter();
	}
	
	private void initTranProtecter(){
		tranProtecter = new TranProtecter(tranQueueArray,tranExecuterThreadArray);
		tranProtecter.start();
	}
	
	private void initTranQueue(){
		load();
		
		int length = threadCount+otherThreadCount;
		
		if(tranQueueArray == null)
			tranQueueArray = new TranQueue[length];
		
		for(int i = 0;i < length; i++){
			if(tranQueueArray[i] == null){
				tranQueueArray[i] = new TranQueue();
			}
		}
	}
	
	private void initTranExecuterThread(){
		tranExecuterThreadArray = new TranExecuterThread[threadCount+otherThreadCount];
		for(int i = 0; i < tranQueueArray.length; i++){
			tranExecuterThreadArray[i].setTranQueue(tranQueueArray[i]);
		}
	}
	
	public void start(){
		for(int i = 0; i < tranQueueArray.length; i++){
			tranExecuterThreadArray[i].start();
		}
	}
	
	public void add(TranAdvanceContainer tranAdvanceContainer){
		
		TranQueueNode tranQueueNode = new TranQueueNode();
		
		TranNode tranNode = new TranNode();
		tranNode.setName(tranAdvanceContainer.getName());
		tranNode.setModelList(tranAdvanceContainer.getMainTranList());
		tranNode.setId(tranAdvanceContainer.getId());
		tranQueueNode.setTranNode(tranNode);
		
		tranMap.put(tranAdvanceContainer.getId(), tranAdvanceContainer);
		
		synchronized(lock){
			
			tranQueueArray[flag].add(tranQueueNode);
			
			if(flag+1 < threadCount+otherThreadCount){
				flag = otherThreadCount;
			}else{
				flag++;
			}
		}
		
		addRewTran(tranAdvanceContainer.getRewTranMapList(),tranAdvanceContainer.getName(),tranAdvanceContainer.getId());
		addNoTran(tranAdvanceContainer.getNoTranList(),tranAdvanceContainer.getName(),tranAdvanceContainer.getId());
		
		log.info("The tran: "+tranAdvanceContainer.getId()+",name: "+tranAdvanceContainer.getName()+" is add!");
	}
	
	private void addNoTran(List<ModelLoader> list,String name,long id){
		TranQueueNode tranQueueNode = new TranQueueNode();
		TranNode tranNode = new TranNode();
		tranNode.setName(name);
		tranNode.setModelList(list);
		tranNode.setId(id);
		tranQueueNode.setTranNode(tranNode);
		tranQueueArray[NO_TRAN_INDEX].add(tranQueueNode);
	}
	
	private void addRewTran(Map<String,List<ModelLoader>> mapList,String name,long id){
		List<List<ModelLoader>> list = (List<List<ModelLoader>>)mapList.values();
		for(int i = 0; i < list.size(); i++){
			
			TranQueueNode tranQueueNode = new TranQueueNode();
			TranNode tranNode = new TranNode();
			tranNode.setName(name);
			tranNode.setModelList(list.get(i));
			tranNode.setId(id);
			tranQueueNode.setTranNode(tranNode);
			tranQueueArray[NEW_TRAN_INDEX].add(tranQueueNode);
		}
	}
	
	public void reStart(int index){
		TranExecuterThread tranExecuterThread = new TranExecuterThread();
		tranExecuterThread.setTranQueue(tranQueueArray[index]);
		
		tranExecuterThreadArray[index] = tranExecuterThread; 
		tranExecuterThread.start();
		log.error("The queue: " +index+" is restart!");
	}
	
	public void notifyError(long id,List<ResultInfo> list){
		TranAdvanceContainer tranAdvanceContainer = tranMap.remove(id);
		tranAdvanceContainer.notifyError(list);
		log.error("The tran: " +id+" is error!");
	}
	
	public void notifySuccess(long id,List<ResultInfo> list){
		TranAdvanceContainer tranAdvanceContainer = tranMap.remove(id);
		tranAdvanceContainer.notifySuccess(list);
	}
	
	public void notifyFinish(long id){
		tranMap.remove(id);
		log.info("The tran: " +id+" is finish!");
	}
	
	public void notifyStart(long id){
		TranAdvanceContainer tranAdvanceContainer = tranMap.get(id);
		if(tranAdvanceContainer != null 
				&& !tranAdvanceContainer.isStart()){
			tranAdvanceContainer.notifyStart();
		}
		log.info("The tran: " +id+" is start!");
	}
}

package com.orm.model.execute.tran.util;

import java.util.List;

import com.orm.model.container.ResultInfo;
import com.orm.model.execute.tran.advance.back.TranAdvanceContainer;
import com.orm.model.execute.tran.advance.back.TranAdvanceManager;

public class TranUtil {

	private static long flag = 1;
	
	private static Object lock = new Object();
	
	public static long getCurrentFlag(){
		long tempFlag = 0;
		synchronized(lock){
			tempFlag = flag;
			flag++;
		}
		
		return tempFlag;
	}
	
	public static void register(TranAdvanceContainer tranAdvanceContainer){
		TranAdvanceManager.getInstance().add(tranAdvanceContainer);
	}
	
	public static void notifyError(long id,List<ResultInfo> list){
		TranAdvanceManager.getInstance().notifyError(id, list);
	}
	
	public static void notifySuccess(long id,List<ResultInfo> list){
		TranAdvanceManager.getInstance().notifySuccess(id, list);
	}
	
	public static void notifyFinish(long id){
		TranAdvanceManager.getInstance().notifyFinish(id);
	}
	
	public static void notifyStart(long id){
		TranAdvanceManager.getInstance().notifyStart(id);
	}
}

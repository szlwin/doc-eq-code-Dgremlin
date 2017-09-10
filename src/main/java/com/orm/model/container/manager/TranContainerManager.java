package com.orm.model.container.manager;

import com.orm.model.execute.tran.TransactionContainer;
import com.orm.model.execute.tran.advance.TranAdvanceContainer;
import com.orm.model.execute.tran.util.TranUtil;

public class TranContainerManager {

	private static final ThreadLocal<TransactionContainer> tranContainerLocal = new ThreadLocal<TransactionContainer>();
	
	private static final ThreadLocal<TranAdvanceContainer> tranAdvanceContainerLocal = new ThreadLocal<TranAdvanceContainer>();

	public static synchronized TransactionContainer getTransactionContainer(String name){
		
		TransactionContainer transactionContainer = tranContainerLocal.get();
		
		if(transactionContainer == null){
			transactionContainer = new TransactionContainer(name);
			transactionContainer.setFlag(TranUtil.getCurrentFlag());
			tranContainerLocal.set(transactionContainer);
		}
		
		return transactionContainer;
	}
	
	
	public static TranAdvanceContainer getTranAdvanceContainer(String name){
		TranAdvanceContainer transactionContainer = tranAdvanceContainerLocal.get();
		
		if(transactionContainer == null){
			transactionContainer = new TranAdvanceContainer(name);
			transactionContainer.setFlag(TranUtil.getCurrentFlag());
			tranAdvanceContainerLocal.set(transactionContainer);
		}
		return transactionContainer;
	}
}

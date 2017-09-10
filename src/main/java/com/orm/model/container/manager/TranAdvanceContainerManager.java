package com.orm.model.container.manager;

import com.orm.model.execute.tran.advance.TranAdvanceContainer;
import com.orm.model.execute.tran.util.TranUtil;

public class TranAdvanceContainerManager {
	
	private static final ThreadLocal<TranAdvanceContainer> tranAdvanceContainerLocal = new ThreadLocal<TranAdvanceContainer>();
	
	public static synchronized TranAdvanceContainer getTranAdvanceContainer(String name){
		TranAdvanceContainer transactionContainer = tranAdvanceContainerLocal.get();
		
		if(transactionContainer == null){
			transactionContainer = new TranAdvanceContainer(name);
			transactionContainer.setFlag(TranUtil.getCurrentFlag());
			tranAdvanceContainerLocal.set(transactionContainer);
		}
		return transactionContainer;
	}
}

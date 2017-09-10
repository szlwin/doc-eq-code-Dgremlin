package com.orm.api.execute;

import java.util.Collection;

import com.orm.common.execute.AbstrcatExecute;
import com.orm.common.execute.exception.ExecuteException;
import com.orm.api.dom.ApiQueryInfo;
import com.orm.sql.dom.DataInfo;
import com.orm.sql.dom.ExecuteInfo;

public abstract class AbstractApiExecute<C> extends AbstrcatExecute<Collection<DataInfo>, ExecuteInfo, C> implements ApiExecute<Collection<DataInfo>, ExecuteInfo, C>{

	protected ApiQueryInfo queryInfo;
	
	protected Collection<DataInfo> dataInfoCollection;
	
	public void setValue(Collection<DataInfo> e) {
		this.dataInfoCollection = e;
	}
	
	
	public void execute(ApiQueryInfo e) throws ExecuteException {
		this.setConnection((C) e.getCon());
		this.setValue(e.getDataInfoCollection());
		this.queryInfo = e;
		execute();
	}
}

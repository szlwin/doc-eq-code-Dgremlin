package com.orm.api.execute;

import java.util.Collection;

import com.orm.common.execute.Execute;
import com.orm.common.execute.exception.ExecuteException;
import com.orm.api.dom.ApiQueryInfo;
import com.orm.sql.dom.DataInfo;
import com.orm.sql.dom.ExecuteInfo;

public interface ApiExecute<E,R,C> extends Execute<Collection<DataInfo>, ExecuteInfo, C>{
	
	public void execute(ApiQueryInfo e) throws ExecuteException;
}

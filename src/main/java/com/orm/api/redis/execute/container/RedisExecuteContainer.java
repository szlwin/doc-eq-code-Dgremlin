package com.orm.api.redis.execute.container;

import java.util.HashMap;
import java.util.Map;

import com.orm.api.redis.execute.DeleteExecute;
import com.orm.api.redis.execute.GetExecute;
import com.orm.api.redis.execute.RedisExecute;
import com.orm.api.redis.execute.UpdateExecute;
import com.orm.common.execute.container.ExecuteContainer;
import com.orm.common.execute.exception.ExecuteException;
import com.orm.api.dom.ApiQueryInfo;
import com.orm.sql.dom.ExecuteInfo;

public class RedisExecuteContainer implements ExecuteContainer<ApiQueryInfo,ExecuteInfo>{

	protected Map<String,Class<? extends RedisExecute>> executeMap 
		= new HashMap<String,Class<? extends RedisExecute>>(4);

	public void init() {
		executeMap.put("get", GetExecute.class);
		executeMap.put("insert", UpdateExecute.class);
		executeMap.put("update", UpdateExecute.class);
		executeMap.put("delete", DeleteExecute.class);
	}

	public ExecuteInfo execute(ApiQueryInfo e) throws ExecuteException {
		
		RedisExecute sqlExecute = getExecute(e.getType());
		
		//sqlExecute.setCmd(e.getCmd());
		//sqlExecute.setValue(e.getData());
		//sqlExecute.setConnection(e.getCon());
		
		//sqlExecute.setKeyValue(e.getKey());
		
		sqlExecute.execute(e);
		return sqlExecute.getResult();
	}

	private RedisExecute getExecute(String type) {
		RedisExecute execute = null;
		
		try {
			execute = executeMap.get(type).newInstance();
		} catch (Exception e1) {
	
		}
		return execute;
	}

}

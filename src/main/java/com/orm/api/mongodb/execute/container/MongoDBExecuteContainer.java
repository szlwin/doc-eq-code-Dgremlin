package com.orm.api.mongodb.execute.container;

import java.util.HashMap;
import java.util.Map;

import com.orm.api.mongodb.execute.FindOneCmd;
import com.orm.api.mongodb.execute.InsertCmd;
import com.orm.api.mongodb.execute.MongoDBExecute;
import com.orm.api.mongodb.execute.RemoveCmd;
import com.orm.api.mongodb.execute.UpdateCmd;
import com.orm.common.execute.container.ExecuteContainer;
import com.orm.common.execute.exception.ExecuteException;
import com.orm.api.dom.ApiQueryInfo;
import com.orm.sql.dom.ExecuteInfo;

public class MongoDBExecuteContainer implements ExecuteContainer<ApiQueryInfo,ExecuteInfo>{

	protected Map<String,Class<? extends MongoDBExecute>> executeMap 
		= new HashMap<String,Class<? extends MongoDBExecute>>(5);

	public void init() {
		//executeMap.put("query", QueryExecute.class);
		executeMap.put("findOne", FindOneCmd.class);
		executeMap.put("insert", InsertCmd.class);
		executeMap.put("update", UpdateCmd.class);
		executeMap.put("remove", RemoveCmd.class);
	}

	public ExecuteInfo execute(ApiQueryInfo e) throws ExecuteException {
		
		MongoDBExecute mongoDBExecute = getExecute(e.getSql());
		
		//sqlExecute.setCmd(e.getCmd());
		//sqlExecute.setValue(e.getData());
		//sqlExecute.setConnection(e.getCon());
		
		//sqlExecute.setKeyValue(e.getKey());
		
		mongoDBExecute.execute(e);
		return mongoDBExecute.getResult();
	}

	private MongoDBExecute getExecute(String type) {
		MongoDBExecute execute = null;
		
		try {
			execute = executeMap.get(type).newInstance();
		} catch (Exception e1) {
	
		}
		return execute;
	}

}

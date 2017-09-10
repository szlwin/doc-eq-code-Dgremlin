package com.orm.session;

import java.util.Map;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.connection.DataConnection;
import com.orm.context.data.BaseData;
import com.orm.sql.dom.ExecuteParam;

public class SessionExecuter {

	private DataConnection con;
	
	public SessionExecuter(DataConnection con){
		this.con = con;
	}
	
	public Object execute(String sql,Map<String,Object> paramMap,String type) throws ExecuteException{
		
		return execute(sql,paramMap,type,false);
	}
	
	public Object execute(String sql,Map<String,Object> paramMap,String type,boolean isOrign) throws ExecuteException{
		ExecuteParam param = new ExecuteParam();
		param.setType(type);
		param.setValue(paramMap);
		param.setCmd(sql);
		param.setOrign(isOrign);
		return execute(param);
	}
	
	public Object execute(BaseData baseData,String type) throws ExecuteException{
		ExecuteParam param = new ExecuteParam();
		param.setType(type);
		param.setValue(baseData);
		param.setOrign(false);
		return execute(param);
	}
	
	private Object execute(ExecuteParam param)throws ExecuteException{
		Object result = null;
	
		result = con.execute(param);
			
		return result;
	}
}

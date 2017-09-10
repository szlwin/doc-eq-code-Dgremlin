package com.orm.model.execute.rule.data;

import com.orm.common.xml.util.Constanst;

public class DataExecuteFactory {
	
	public static Execute<?> createExecute(String type){
		/*
		if(type.equals(Constanst.RULE_TYPE_EXECUTE_INSERT)){
			return new InsertExecute();
		}
		
		if(type.equals(Constanst.RULE_TYPE_EXECUTE_UPDATE)){
			return new UpdateExecute();
		}
		
		if(type.equals(Constanst.RULE_TYPE_EXECUTE_DELETE)){
			return new DeleteExecute();
		}
		
		if(type.equals(Constanst.RULE_TYPE_EXECUTE_SELECT)){
			return new SelectExecute();
		}
		
		if(type.equals(Constanst.RULE_TYPE_EXECUTE_GET)){
			return new GetExecute();
		}
		
		//return null;*/
		
		return null;
	}
}

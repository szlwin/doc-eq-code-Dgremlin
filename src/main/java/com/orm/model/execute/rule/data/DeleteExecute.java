package com.orm.model.execute.rule.data;

import java.sql.SQLException;

import com.orm.common.execute.exception.ExecuteException;

public class DeleteExecute extends AbstractDataExecute{

	//@SuppressWarnings("unchecked")
	//@Override
	/*protected boolean executeBysql(String sql, Object value)
			throws SQLException {
		com.orm.sql.execute.common.DeleteExecute sqlExecute 
			= new com.orm.sql.execute.common.DeleteExecute(this.con,this.dataSource);
	
		//System.out.println(sql);
	
		QueryInfo queryInfo = new QueryInfo();
		queryInfo.setSql(sql);
		queryInfo.setParamMap((Map<String,Object>)value);
	
		sqlExecute.execute(queryInfo);
	
		return true;
	}*/

	//@Override
	/*protected SimpleSQLExecute createExecute() {
		return new DeleteSQLExecute(this.con,dataSource);
	}*/
/*
	protected boolean executeByData() throws SQLException{
		propertyValue = this.paramValue;
		return executeByData(this.paramValue);
	}*/
	
	protected boolean executeBySql() throws ExecuteException{
		//propertyValue = this.paramValue;
		return executeBysql(sql,value);
	}
	
	@Override
	protected void doAfter(Object result) {
		// TODO Auto-generated method stub
		
	}

}

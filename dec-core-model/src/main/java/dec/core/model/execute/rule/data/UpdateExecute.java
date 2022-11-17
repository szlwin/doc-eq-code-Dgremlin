package dec.core.model.execute.rule.data;

import dec.core.datasource.execute.exception.ExecuteException;

//import dec.core.common.execute.exception.ExecuteException;

public class UpdateExecute extends AbstractDataExecute{

	/*@SuppressWarnings("unchecked")
	protected boolean executeBysql(String sql,Object value) throws SQLException{
		com.orm.sql.execute.common.UpdateExecute sqlExecute 
			= new com.orm.sql.execute.common.UpdateExecute(this.con,this.dataSource);
		
		//System.out.println(sql);
		
		QueryInfo queryInfo = new QueryInfo();
		queryInfo.setSql(sql);
		queryInfo.setParamMap((Map<String,Object>)value);
		
		sqlExecute.execute(queryInfo);
		
		return true;
	}*/

	/*@Override
	protected SimpleSQLExecute createExecute() {
		return new UpdateSQLExecute(this.con,dataSource);
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

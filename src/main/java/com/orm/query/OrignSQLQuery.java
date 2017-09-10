package com.orm.query;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.session.SessionExecuter;
import com.orm.sql.dom.QueryInfo;

public class OrignSQLQuery extends AbstractQuery<Map<String,Object>,Map<String,Object>>{

	protected QueryInfo queryInfo;
	
	protected Collection<Map<String,Object>> dataCollection;
	
	public OrignSQLQuery(){
		super();
	}
	public OrignSQLQuery(String connectionName){
		super(connectionName);
	}
	
	
	public void addQueryInfo(String sql, Map<String, Object> paramMap,
			Collection<Map<String, Object>> dataCollection) throws ExecuteException {
		
		queryInfo = new QueryInfo(sql,paramMap,null);
		
		this.dataCollection = dataCollection;
	}

	@Override
	protected void execute() throws ExecuteException {
		
		SessionExecuter sessionExecuter = new SessionExecuter(con);
		sessionExecuter.execute(queryInfo.getSql(), queryInfo.getParamMap(), "query",true);
		//QueryOrignSQLEcecute querySQLExecute = new QueryOrignSQLEcecute(con.getConnection());
		
		//querySQLExecute.setDataCollection(dataCollection);
		
		//querySQLExecute.execute(queryInfo);
		
	}

}

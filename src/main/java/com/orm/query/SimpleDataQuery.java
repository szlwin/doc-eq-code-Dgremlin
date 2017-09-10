package com.orm.query;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.common.xml.exception.DataNotDefineException;
import com.orm.context.data.BaseData;
import com.orm.context.data.DataUtil;
import com.orm.context.data.ToolUtil;
import com.orm.session.SessionExecuter;
import com.orm.sql.dom.ExecuteInfo;
import com.orm.sql.dom.QueryInfo;

public class SimpleDataQuery extends AbstractQuery<BaseData,BaseData> {

	protected QueryInfo queryInfo;
	
	protected Collection<BaseData> dataCollection;
	
	public SimpleDataQuery(){
		super();
	}
	public SimpleDataQuery(String connectionName){
		super(connectionName);
	}
	
	public void addQueryInfo(String sql, BaseData data,
			Collection<BaseData> dataCollection) throws ExecuteException {
		
		queryInfo = new QueryInfo(sql,ToolUtil.getValues(data),data.getName());
		
		this.dataCollection = dataCollection;
	}

	@Override
	protected void execute() throws ExecuteException {
		
		SessionExecuter sessionExecuter = new SessionExecuter(con);
		
		ExecuteInfo executeInfo 
			= (ExecuteInfo) sessionExecuter.execute(queryInfo.getSql(), queryInfo.getParamMap(), "query");
		
		Collection<Map<String,Object>>  rsColllection 
			= executeInfo.getDataCollection();
		
		Iterator<Map<String, Object>> it  = rsColllection.iterator();
		while(it.hasNext()){
			Map<String,Object> rsMap = it.next();
			try {
				BaseData data = DataUtil.createBaseData(queryInfo.getBaseDataName());
				DataUtil.convertDataToBaseData(rsMap, data);
				dataCollection.add(data);
			} catch (DataNotDefineException e) {
				throw new ExecuteException(e);
			}

		}
		//QuerySQLExecute querySQLExecute = new QuerySQLExecute(con.getConnection(),dataSourceName);
		
		//querySQLExecute.setDataCollection(dataCollection);
		
		//querySQLExecute.execute(queryInfo);
		
	}

}

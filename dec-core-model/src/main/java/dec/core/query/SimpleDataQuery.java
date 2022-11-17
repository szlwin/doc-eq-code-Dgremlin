package dec.core.query;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import dec.core.context.config.exception.DataNotDefineException;
//import dec.core.common.execute.exception.ExecuteException;
//import dec.core.common.xml.exception.DataNotDefineException;
import dec.core.context.data.BaseData;
import dec.core.datasource.execute.dom.ExecuteInfo;
import dec.core.datasource.execute.exception.ExecuteException;
import dec.core.model.utils.DataUtil;
//import dec.core.context.data.DataUtil;
//import dec.core.context.data.ToolUtil;
import dec.core.session.SessionExecuter;
//import dec.core.sql.dom.ExecuteInfo;
//import dec.core.sql.dom.QueryInfo;
import dec.external.datasource.sql.dom.QueryInfo;

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
		
		queryInfo = new QueryInfo(sql,DataUtil.getValues(data),data.getName());
		
		this.dataCollection = dataCollection;
	}

	@Override
	protected void execute() throws ExecuteException {
		
		SessionExecuter sessionExecuter = new SessionExecuter(con);
		
		ExecuteInfo executeInfo 
			= (ExecuteInfo) sessionExecuter.execute(queryInfo.getSql(), queryInfo.getParamMap(), "query");
		
		Collection<Map<String,Object>>  rsColllection 
			= (Collection<Map<String, Object>>) executeInfo.getDataCollection();
		
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

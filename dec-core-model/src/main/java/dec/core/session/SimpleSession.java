package dec.core.session;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dec.core.context.config.exception.DataNotDefineException;
import dec.core.context.config.utils.ConfigContextUtil;
//import dec.core.common.execute.exception.ExecuteException;
//import dec.core.common.xml.exception.DataNotDefineException;
import dec.core.context.data.BaseData;
import dec.core.datasource.execute.dom.ExecuteInfo;
//import dec.core.context.data.DataUtil;
//import dec.core.context.data.ToolUtil;
//import dec.core.sql.dom.ExecuteInfo;
//import dec.core.sql.util.Util;
import dec.core.datasource.execute.exception.ExecuteException;
import dec.core.model.utils.DataUtil;

public class SimpleSession extends AbstractSession<BaseData> {

	private final static Logger log = LoggerFactory.getLogger(SimpleSession.class);
	
	protected String dataSourceName;
	
	public SimpleSession(){
		super();
		this.dataSourceName = ConfigContextUtil.getDataSource().getName();
	}
	
	public SimpleSession(String connectionName){
		super(connectionName);
		this.dataSourceName = ConfigContextUtil.getDataSourceByCon(connectionName).getName();
	}
	
	public void save(BaseData e) throws ExecuteException {
		
		if(log.isDebugEnabled()){
			log.debug("Save the data:"+e.getName()+" start!");
		}
		
		ExecuteInfo ex = (ExecuteInfo) execute(e,"insert");
		Object keyValue = ex.getKeyValue();
		
		if(keyValue != null){
			String key = e.getData().getTableInfo().getTable(con.getDataSource()).getPropertyKey();
			
			e.setValue(key, keyValue);
		}

		
		//InsertSQLExecute sqlExecute = new InsertSQLExecute(con.getConnection(),dataSourceName);
		//sqlExecute.execute(e);
		
		if(log.isDebugEnabled()){
			log.debug("Save the data:"+e.getName()+" end!");
		}
	}

	public void update(BaseData e) throws ExecuteException {
		if(log.isDebugEnabled()){
			log.debug("Update the data:"+e.getName()+" start!");
		}
		
		execute(e,"update");
		
		//UpdateSQLExecute sqlExecute = new UpdateSQLExecute(con.getConnection(),dataSourceName);
		//sqlExecute.execute(e);
		
		if(log.isDebugEnabled()){
			log.debug("Update the data:"+e.getName()+" end!");
		}
	}

	public void executeUpdate(String sql,BaseData data) throws ExecuteException {
		if(log.isDebugEnabled()){
			log.debug("Execute the sql:"+sql+",data:"+data.getName()+" start!");
		}
		executeUpdate(sql,DataUtil.getValues(data));
		
		if(log.isDebugEnabled()){
			log.debug("Execute the sql:"+sql+",data:"+data.getName()+" end!");
		}
	}
	
	public void executeUpdate(String sql,Map<String,Object> paramMap) throws ExecuteException {
		
		if(log.isDebugEnabled()){
			log.debug("Execute the sql:"+sql+" start!");
		}
		
		execute(sql,paramMap,"update");
		/*UpdateExecute sqlExecute = new UpdateExecute(con.getConnection(),dataSourceName);
		
		QueryInfo queryInfo = new QueryInfo();
		queryInfo.setSql(sql);
		queryInfo.setParamMap(paramMap);
		
		sqlExecute.execute(queryInfo);*/
		
		if(log.isDebugEnabled()){
			log.debug("Execute the sql:"+sql+" end!");
		}
	}
	
	public void delete(BaseData e) throws ExecuteException {
		if(log.isDebugEnabled()){
			log.debug("Delete the data:"+e.getName()+" start!");
		}
		
		execute(e,"delete");
		//DeleteSQLExecute sqlExecute = new DeleteSQLExecute(con.getConnection(),dataSourceName);
		//sqlExecute.execute(e);
		
		if(log.isDebugEnabled()){
			log.debug("Delete the data:"+e.getName()+" end!");
		}
	}

	public BaseData get(BaseData e) throws ExecuteException {
		if(log.isDebugEnabled()){
			log.debug("Get the data:"+e.getName()+" start!");
		}
		
		ExecuteInfo executeInfo = (ExecuteInfo) execute(e,"get");
		
		Iterator<Map<String,Object>> it = (Iterator<Map<String, Object>>) executeInfo.getDataCollection().iterator();
		BaseData data = null;
		if(it.hasNext()){
			Map<String,Object> paramMap = it.next();
			
			
			try {
				data = DataUtil.createBaseData(e.getName());
			} catch (DataNotDefineException e1) {
				throw new ExecuteException(e1);
			}
			DataUtil.convertDataToBaseData(paramMap,data);
		}

		//GetSQLExecute sqlExecute = new GetSQLExecute(con.getConnection(),dataSourceName);
		//sqlExecute.execute(e);
		
		if(log.isDebugEnabled()){
			log.debug("Get the data:"+e.getName()+" end!");
		}
		
		return data;

	}
/*
	public void query(String sql, BaseData data,Collection<BaseData> collection) throws SQLException {
		
		QuerySQLExecute querySQLExecute = new QuerySQLExecute(this.con,dataSourceName);
		
		QueryInfo queryInfo = new QueryInfo(sql,ToolUtil.getValues(data),data.getName());
		
		querySQLExecute.setDataCollection(collection);
		
		querySQLExecute.execute(queryInfo);
	}*/
	
	/*
	public void querySimple(String sql, Map<String,Object> map,Collection<Map<String,Object>> collection) throws SQLException {
		
		QuerySQLExecute querySQLExecute = new QuerySQLExecute(this.con,dataSourceName);
		
		QueryInfo queryInfo = new QueryInfo(sql,map,null);
		
		querySQLExecute.setDataCollection(collection);
		
		querySQLExecute.execute(queryInfo);

	}
	
	public void querySimple(String sql,Collection<Map<String,Object>> collection) {
		querySimple(sql,null);
	}*/
	
	public void executeOrignUpdate(String sql) throws ExecuteException{
		if(log.isDebugEnabled()){
			log.debug("Execute the sql:"+sql+" start!");
		}
		executeOrignUpdate(sql,null);
		
		if(log.isDebugEnabled()){
			log.debug("Execute the sql:"+sql+" end!");
		}
	}
	
	public void executeOrignUpdate(String sql,Map<String,Object> paramMap) throws ExecuteException{
		
		if(log.isDebugEnabled()){
			log.debug("Execute the sql:"+sql+" start!");
		}
		execute(sql,paramMap,"update",true);
		//UpdateOrignSQLExecute updateOrignSQLExecute = new UpdateOrignSQLExecute(con.getConnection());
		
		//QueryInfo queryInfo = new QueryInfo(sql,paramMap,null);
		
		//updateOrignSQLExecute.execute(queryInfo);
		
		if(log.isDebugEnabled()){
			log.debug("Execute the sql:"+sql+" end!");
		}
	}
	
	/*
	public void queryOrign(String sql, Map<String,Object> map,Collection<Map<String,Object>> collection) throws SQLException {
		
		QueryOrignSQLEcecute querySQLExecute = new QueryOrignSQLEcecute(this.con);
		
		QueryInfo queryInfo = new QueryInfo(sql,map,null);
		
		querySQLExecute.setDataCollection(collection);
		
		querySQLExecute.execute(queryInfo);
	}
	
	public void queryOrign(String sql,Collection<Map<String,Object>> collection) {
		queryOrign(sql,null);
	}*/
	
	public Collection<Map<String,Object>> query(String sql,Map<String,Object> paramMap) throws ExecuteException {
		ExecuteInfo exectInfo = (ExecuteInfo) execute(sql,paramMap,"query");
		return (Collection) exectInfo.getDataCollection();
	}
	
	private Object execute(String sql,Map<String,Object> paramMap,String type) throws ExecuteException{
		
		SessionExecuter sessionExecuter = new SessionExecuter(con);
		return sessionExecuter.execute(sql,paramMap,type);
	}
	
	private Object execute(String sql,Map<String,Object> paramMap,String type,boolean isOrign) throws ExecuteException{
		SessionExecuter sessionExecuter = new SessionExecuter(con);
		return sessionExecuter.execute(sql,paramMap,type,isOrign);
	}
	
	private Object execute(BaseData baseData,String type) throws ExecuteException{
		SessionExecuter sessionExecuter = new SessionExecuter(con);
		return sessionExecuter.execute(baseData,type);
	}
	
}

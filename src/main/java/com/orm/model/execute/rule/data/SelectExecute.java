package com.orm.model.execute.rule.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.common.xml.exception.DataNotDefineException;
import com.orm.common.xml.model.view.ViewProperty;
import com.orm.context.data.BaseData;
import com.orm.context.data.DataConvert;
import com.orm.context.data.DataUtil;
import com.orm.sql.datatype.convert.DataTypeConvert;
import com.orm.sql.dom.ExecuteInfo;

public class SelectExecute extends AbstractDataExecute{
	
	protected boolean executeBySql() throws ExecuteException{
		//Convert convert = new Convert(sql,viewName);
		//String tempSql = convert.convert();
		return executeBysql(sql,value);
	}
	
	
	protected boolean executeByData() throws ExecuteException{
		return executeByData(value);
	}
	
	/*@Override
	protected boolean executeByData(Object value) throws SQLException {
		return false;
	}*/

	/*@SuppressWarnings({ "rawtypes", "unchecked" })
	protected boolean executeBysql(String sql, Object value)
			throws SQLException {
		com.orm.sql.execute.common.QuerySQLExecute sqlExecute 
			= new com.orm.sql.execute.common.QuerySQLExecute(this.con,this.dataSource);
	
		QueryInfo queryInfo = new QueryInfo();
		queryInfo.setSql(sql);
		queryInfo.setParamMap((Map<String,Object>)value);
	
		List dataCollection = new ArrayList();
		
		sqlExecute.setDataCollection(dataCollection);
		sqlExecute.execute(queryInfo);
		
		Object dataValue = null;
		
		if(((Map)value).containsKey(propertyName)){
			dataValue = ((Map)value).get(propertyName);
		}else{
			return false;
		}
		
		if(dataValue instanceof Collection){
			((Collection)dataValue).clear();
			
			if(dataCollection != null && !dataCollection.isEmpty()){
				for(int i = 0; i < dataCollection.size();i++){
					((Collection)dataValue).add(dataCollection.get(i));
				}
			}
				
			
		}else{
			
			if(dataCollection.size() > 1)
				return false;

			//((Map)value).clear();
			
			Iterator it = dataCollection.iterator();
			
			if(it.hasNext()){
				((Map)dataValue).putAll((Map)it.next());
			}
			
		}
			
		return true;
	}*/

	/*@Override
	protected SimpleSQLExecute createExecute() {
		
		return new GetSQLExecute(con, dataSource);
	}*/
	//Oracle大小写转换
	@Override
	protected void doAfter(Object result) {
		//DataTypeConvert.setDataSourceType(dataSource);
		Collection dataCollection = ((ExecuteInfo)result).getDataCollection();
		
		Object dataValue = null;
		Map value = (Map) this.value;
		
		if((value).containsKey(propertyName)){
			dataValue = (value).get(propertyName);
		}else{
			return;
		}
		
		ViewProperty viewProperty = this.viewData
				.getViewPropertyInfo()
				.getProperty()
				.get(propertyName);
		
		if(dataValue instanceof Collection){
			((Collection)dataValue).clear();
			
			if(dataCollection != null && !dataCollection.isEmpty()){
				Iterator it = dataCollection.iterator();
				
				while(it.hasNext()){
					
					Map<String,Object> dataMap = (Map<String, Object>) it.next();
					//Map<String,Object> dataObjValue = new HashMap<String,Object>();
					
					DataConvert dataConvert = new DataConvert();
					
					dataConvert.convert(value, dataValue, viewProperty, dataMap,this.dataSource);
					/*
					Iterator<String> keyIt = this.viewData.getViewPropertyInfo()
							.getProperty().get(propertyName)
							.getViewData().getViewPropertyInfo().getProperty().keySet().iterator();
					
					DataUtil.convertDataToBaseData(dataMap, dataObjValue, keyIt);

					((Collection)dataValue).add(dataObjValue);*/
				}
			}
				
			
		}/*else{
			
			if(dataCollection.size() > 1)
				return;

			//((Map)value).clear();
			
			Iterator it = dataCollection.iterator();
			
			if(it.hasNext()){
				Map<String,Object> dataMap = (Map<String, Object>) it.next();
				Map<String,Object> dataObjValue = (Map<String, Object>) dataValue;
				Iterator<String> keyIt = dataObjValue.keySet().iterator();
				while(keyIt.hasNext()){
					String key = keyIt.next();
					String lowKey = key.toLowerCase();
					if(dataMap.containsKey(lowKey)){
						((Map)dataValue).put(key,dataMap.get(lowKey));
					}
				}
				//((Map)dataValue).putAll((Map)it.next());
			}
			
		}*/
		
	}
}

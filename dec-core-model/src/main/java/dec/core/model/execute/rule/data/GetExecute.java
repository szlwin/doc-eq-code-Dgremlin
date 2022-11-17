package dec.core.model.execute.rule.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import dec.core.context.config.model.view.ViewProperty;
//import dec.core.common.xml.exception.DataNotDefineException;
//import dec.core.common.xml.model.view.ViewProperty;
//import dec.core.context.data.DataConvert;
//import dec.core.context.data.DataUtil;
//import dec.core.sql.datatype.convert.DataTypeConvert;
//import dec.core.sql.dom.ExecuteInfo;
import dec.core.model.data.DataConvert;
import dec.core.datasource.execute.dom.ExecuteInfo;

public class GetExecute extends AbstractDataExecute{
	/*
	protected boolean executeBySql() throws ExecuteException{
		//Convert convert = new Convert(sql,viewName);
		//String tempSql = convert.convert();
		return executeBysql(sql,paramValue);
	}*/
	
	/*
	protected boolean executeByData() throws ExecuteException{
		return executeByData(paramValue);
	}*/
	
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

	@Override
	protected void doAfter(Object result) {
		//DataTypeConvert.setDataSourceType(dataSource);
		Collection<?> dataCollection = ((ExecuteInfo)result).getDataCollection();
		
		//Object dataValue = null;
		
		
		/*if(this.viewData.getTargetMain().getName().equals(propertyName)){
			dataValue = value;
		}else{
			if((value).containsKey(propertyName)){
				dataValue = (value).get(propertyName);
			}else{
				return;
			}
		}*/

		
		//
		
		if(dataCollection.size() > 1)
			return;

		Iterator<?> it = dataCollection.iterator();
		//((Map)value).clear();
		if(!it.hasNext()){
			return;
		}
		
		//if(this.sql == null || "".equals(sql)){
			
			ViewProperty viewProperty = null;
			//String dataName = null;
			boolean isMain = viewData.getTargetMain().getName().equals(propertyName);
			if(isMain){
				//dataName = viewData.getTargetMain().getName();
				
			}else{
				viewProperty = this.viewData
						.getViewPropertyInfo()
						.getProperty()
					.get(propertyName);
				
				//dataName = viewProperty
				//		.getViewData()
				//		.getTargetMain().getName();
			}

			
			Map<String, Object> dataMap =(Map<String, Object>) it.next();
			//DataUtil.convertDataToBaseData(dataMap, data);
			
			DataConvert dataConvert = new DataConvert();
			
			Map<String,Object> value = (Map<String,Object>) this.value;
			
			if(isMain){
				dataConvert.convert(value, viewData.getTargetMain(),viewData.getViewPropertyInfo().getProperty(), dataMap,this.dataSource);
			}else{
				dataConvert.convert(value, this.propertyValue, viewProperty, dataMap,this.dataSource);
			}
			
			//DataUtil.convertDataToBaseData(dataMap,(Map<String, Object>) propertyValue);
		//}else{
			
		//	Map<String,Object> dataMap = (Map<String, Object>) it.next();
			//Map<String,Object> dataObjValue = (Map<String, Object>) dataValue;
			
		//	DataUtil.convertDataToBaseData(dataMap,(Map<String, Object>) propertyValue);
			/*Iterator<String> keyIt = dataObjValue.keySet().iterator();
			while(keyIt.hasNext()){
				String key = keyIt.next();
				String lowKey = key.toLowerCase();
				if(dataMap.containsKey(lowKey)){
					dataObjValue.put(key,dataMap.get(lowKey));
				}
			}*/
				//((Map)dataValue).putAll((Map)it.next());
		//}
		
	}
}

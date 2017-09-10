package com.orm.api.connection;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import com.orm.common.execute.exception.ExecuteException;
import com.orm.connection.exception.ConectionException;
import com.orm.connection.factory.ConnectionFactory;
import com.orm.context.data.DataUtil;
import com.orm.api.dom.ApiConvertInfo;
import com.orm.api.dom.FieldInfo;
import com.orm.sql.connection.AbstractConnection;
import com.orm.api.dom.ApiQueryInfo;
import com.orm.sql.dom.ConvertParam;
import com.orm.sql.dom.ExecuteInfo;
import com.orm.sql.dom.ExecuteParam;

public abstract class AbstractApiConnection extends AbstractConnection<ExecuteParam,ExecuteInfo>{

	public void connect() throws ConectionException {
		try {
			con = ConnectionFactory.getInstance().getConnection(conName);
		} catch (Exception e) {
			throw new ConectionException(e);
		}
	}



	public ExecuteInfo execute(ExecuteParam param) throws ExecuteException {
		
		Map<String,String> proMap = DataUtil.getConfigInfo().getConnection(conName).getPropertyInfo();
		
		ConvertParam convertParam = new ConvertParam();
		convertParam.setData(param.getValue());
		convertParam.setSql(param.getCmd());
		convertParam.setType(param.getType());
		convertParam.setOrign(param.getIsOrign());
		convertParam.setDataSource(dataSource);
		convertParam.setConParamMap(proMap);
		
		ApiConvertInfo convertInfo = (ApiConvertInfo) sqlConvertContainer.convert(convertParam);
		
		ApiQueryInfo queryInfo = new ApiQueryInfo();
		queryInfo.setSql(convertInfo.getCmd());
		queryInfo.setType(param.getType());
		queryInfo.setDataInfoCollection(convertInfo.getDataInfo());
		queryInfo.setCon(con);
		queryInfo.setKeyType(convertInfo.getKeyType());
		queryInfo.setKeyValue(convertInfo.getKeyValue());
		queryInfo.setConditionInfo(convertInfo.getConditionInfo());
		queryInfo.setFieldInfo(convertInfo.getFieldInfo());
		queryInfo.setKeyId(convertInfo.getKeyId());
		queryInfo.setConParamMap(proMap);
		queryInfo.setDataName(convertInfo.getDataName());
		ExecuteInfo executeInfo = (ExecuteInfo) sqlExecuteContainer.execute(queryInfo);
		

		if("get".equals(param.getType()) || "query".equals(param.getType()))
			convert(executeInfo.getDataCollection(),convertInfo.getFieldInfo(),convertInfo.getKeyId(),convertInfo.getKeyValue());
		return executeInfo;
	}
	
	protected void convert(Collection collection, Collection<FieldInfo> fieldInfoSet,String keyId,Object keyValue){
		Iterator<Map<String,Object>> it = collection.iterator();
		while(it.hasNext()){
			Map<String,Object> dataMap = it.next();
			Iterator<FieldInfo> infoIt =  fieldInfoSet.iterator();
			
			while(infoIt.hasNext()){
				FieldInfo fieldInfo = infoIt.next();
				
				Object value = dataMap.remove(fieldInfo.getColumn());
				dataMap.put(fieldInfo.getProperty(), value);
			}
			
			dataMap.put(keyId, keyValue);
		}
	}

}

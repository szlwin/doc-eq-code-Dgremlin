package com.orm.api.connection.redis;
import redis.clients.jedis.Jedis;


import com.orm.connection.exception.ConectionException;
import com.orm.api.connection.AbstractApiConnection;

public class RedisConnection extends AbstractApiConnection{

	public void commit() throws ConectionException {
		// TODO Auto-generated method stub
		
	}

	public void close() throws ConectionException {
		((Jedis) con).quit();
		
	}

	//public ExecuteInfo execute(ExecuteParam param) throws ExecuteException {
		/*ConvertParam convertParam = new ConvertParam();
		convertParam.setData(param.getValue());
		convertParam.setSql(param.getCmd());
		convertParam.setType(param.getType());
		convertParam.setOrign(param.getIsOrign());
		convertParam.setDataSource(dataSource);
		//convertParam.setDataType(null);
		//convertParam.setTimeOut(0);
		
		ConvertInfo convertInfo = (ConvertInfo) sqlConvertContainer.convert(convertParam);
		
		RedisQueryInfo queryInfo = new RedisQueryInfo();
		queryInfo.setCmd(convertInfo.getSql());
		queryInfo.setType(param.getType());
		queryInfo.setKey(convertInfo.getKeyId());
		queryInfo.setData(convertInfo.getDataInfo());
		//queryInfo.setDataType(convertParam.getDataType());
		//queryInfo.setTimeOut(convertParam.getTimeOut());
		queryInfo.setCon(con);
		
		ExecuteInfo executeInfo = (ExecuteInfo) sqlExecuteContainer.execute(queryInfo);*/
		//ExecuteInfo executeInfo = super.execute(param);
		//if("get".equals(param.getType())){
		//	convert(executeInfo.getDataCollection(),convertInfo.getDataInfo(),executeInfo.getKeyId(),executeInfo.getKeyValue());
		//}
		//return executeInfo;
		//param.g
	//}


	
	public void rollback() throws ConectionException {
		// TODO Auto-generated method stub
		
	}

	public boolean isClosed() throws ConectionException {
		if(con !=null )
			return ((Jedis) con).isConnected();
		return true;
	}

	public void setAutoCommit(boolean isAuto) throws ConectionException {
		// TODO Auto-generated method stub
		
	}


}

package com.orm.api.redis.execute;

import java.util.ArrayList;
import java.util.Collection;

import redis.clients.jedis.Jedis;

import com.orm.api.redis.execute.AbstractRedisExecute;
import com.orm.common.execute.exception.ExecuteException;

public class GetExecute extends AbstractRedisExecute{

	public void execute() throws ExecuteException {
		Jedis jedis = (Jedis) this.queryInfo.getCon();
		
		Object object = null;
		try{
			object = RedisExecuteUtil.execCmd((String)queryInfo.getKeyValue(), jedis, 
					queryInfo.getDataInfoCollection(), queryInfo.getSql());
		}catch(Exception e){
			throw new ExecuteException(e);
		}
		Collection dataCollection = new ArrayList();
		
		dataCollection.add(object);
		
		this.executeInfo.setDataCollection(dataCollection);
		this.executeInfo.setKeyValue(queryInfo.getKeyValue());
		
	}
}

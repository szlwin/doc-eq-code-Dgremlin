package com.orm.api.redis.execute;

import redis.clients.jedis.Jedis;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.api.execute.AbstractApiExecute;

public abstract class AbstractRedisExecute extends AbstractApiExecute<Jedis> implements RedisExecute{

	protected Jedis jedis;
	
	public void setConnection(Jedis con) {
		this.jedis = con;
	}

	/*
	public void execute(ApiQueryInfo e) throws ExecuteException {
		this.jedis = (Jedis) e.getCon();
		this.dataInfoCollection = e.getDataInfoCollection();
		redisQueryInfo = e;
		execute();
	}*/

	public void execute() throws ExecuteException {
		
		try{
			RedisExecuteUtil.execCmd((String)queryInfo.getKeyValue(), jedis, 
					dataInfoCollection, queryInfo.getSql());
		}catch(Exception e){
			throw new ExecuteException(e);
		}

		this.executeInfo.setKeyValue(queryInfo.getKeyValue());
		
	}

}

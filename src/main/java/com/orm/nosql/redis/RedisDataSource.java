package com.orm.nosql.redis;

import org.apache.commons.pool.impl.GenericObjectPool.Config;

import redis.clients.jedis.JedisPool;

public class RedisDataSource extends JedisPool{
	
	//public RedisDataSource(){
		
	//}
	
	public RedisDataSource(Config poolConfig, String host) {
		super(poolConfig, host);
	}

	//public void setHost(){
	//	this.
	//}
}

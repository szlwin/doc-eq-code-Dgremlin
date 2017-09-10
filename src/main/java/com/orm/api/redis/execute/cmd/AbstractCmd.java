package com.orm.api.redis.execute.cmd;

import redis.clients.jedis.Jedis;

public abstract class AbstractCmd implements JedisCmd{

	protected Jedis jedis;
	
	protected Object data;
	
	protected String key;
	
	public void setJedis(Jedis jedis) {
		this.jedis = jedis;
	}

	public void setData(Object data) {
		this.data = data;
		
	}
	
	public void setKey(String key) {
		this.key = key;
	}

}

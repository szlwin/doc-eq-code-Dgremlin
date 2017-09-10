package com.orm.api.redis.execute.cmd;

import redis.clients.jedis.Jedis;

public interface JedisCmd {

	public void setJedis(Jedis jedis);
	
	public Object execute();
	
	public void setData(Object object);
	
	public void setKey(String key);
}

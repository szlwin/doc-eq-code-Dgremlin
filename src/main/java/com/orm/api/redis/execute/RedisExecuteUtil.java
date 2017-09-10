package com.orm.api.redis.execute;

import java.util.HashMap;
import java.util.Map;

import com.orm.api.redis.execute.cmd.DelCmd;
import com.orm.api.redis.execute.cmd.HgetallCmd;
import com.orm.api.redis.execute.cmd.HmgetCmd;
import com.orm.api.redis.execute.cmd.HmsetCmd;
import com.orm.api.redis.execute.cmd.JedisCmd;

import redis.clients.jedis.Jedis;

public class RedisExecuteUtil {

	static Map<String,Class<? extends JedisCmd>> cmdMap 
		= new HashMap<String,Class<? extends JedisCmd>>();
	
	static{
		cmdMap.put("del", DelCmd.class);
		cmdMap.put("hmset", HmsetCmd.class);
		cmdMap.put("hmget", HmgetCmd.class);
		cmdMap.put("hgetall", HgetallCmd.class);
	}
	
	public static Object execCmd(String key,Jedis jedis,Object data,String cmd){
		JedisCmd jedisCmd = null;
		try {
			jedisCmd = cmdMap.get(cmd).newInstance();
		} catch (Exception e) {

		}
		jedisCmd.setData(data);
		jedisCmd.setKey(key);
		jedisCmd.setJedis(jedis);
		return jedisCmd.execute();
	}
}

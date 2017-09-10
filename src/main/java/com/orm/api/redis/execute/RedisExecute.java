package com.orm.api.redis.execute;

import java.util.Collection;

import redis.clients.jedis.Jedis;

import com.orm.sql.dom.DataInfo;
import com.orm.sql.dom.ExecuteInfo;
import com.orm.api.execute.ApiExecute;

public interface RedisExecute extends ApiExecute<Collection<DataInfo>, ExecuteInfo, Jedis>{
	
	
}

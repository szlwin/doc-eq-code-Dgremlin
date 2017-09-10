package com.orm.api.redis.execute.cmd;

public class DelCmd extends AbstractCmd{

	public Object execute() {
		return this.jedis.del(key);
	}

}

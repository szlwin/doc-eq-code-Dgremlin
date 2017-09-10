package com.orm.api.redis.execute.cmd;

public class HgetallCmd extends AbstractCmd{

	public Object execute() {
		return this.jedis.hgetAll(key);
	}
}

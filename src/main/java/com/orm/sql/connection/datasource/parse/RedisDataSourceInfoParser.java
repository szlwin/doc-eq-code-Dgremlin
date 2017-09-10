package com.orm.sql.connection.datasource.parse;

import java.lang.reflect.Method;
import java.util.Iterator;

import org.dom4j.Element;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisDataSourceInfoParser implements DataSourceInfoParser<JedisPool,Element>{

	public JedisPool parser(Element element) throws Exception {
		JedisPoolConfig config = new JedisPoolConfig();
		
		String host = null;
		String port = null;
		Iterator it = element.element("properties").elementIterator("property");
		while(it.hasNext()){
			Element e = (Element) it.next();
			
			String propertyName = e.attributeValue("name");
			String propertyValue = e.attributeValue("value");
			
			if(propertyName.equals("host")){
				host = propertyValue;
				continue;
			}
			
			if(propertyName.equals("port")){
				port = propertyValue;
				continue;
			}
			
			String methodName = getMethodName(propertyName);
			Method method = config.getClass().getMethod(methodName,new Class[]{String.class});
			method.invoke(config, new Object[]{propertyValue});
		}
		JedisPool pool = new JedisPool(config,host,Integer.valueOf(port));
		return pool;
	}

	protected String getMethodName(String name){
		String methodName = "set"+name.substring(0,1).toUpperCase()+name.substring(1);
		return methodName;
	}
}

package com.orm.sql.connection.datasource;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.orm.connection.datasource.DataSource;
import com.orm.connection.exception.ConectionException;

public class JredisDataSource implements DataSource<JedisPool,Jedis>{

	Log log = LogFactory.getLog(JredisDataSource.class);
	
	protected JedisPool jedisPool;
	
	public void setDataSource(JedisPool e) {
		this.jedisPool = e;
	}

	public JedisPool getDataSource() {
		return jedisPool;
	}

	public Jedis getConnection() throws ConectionException {
		return jedisPool.getResource();
	}

	public void init(String type, Map<String, String> property)
			throws ConectionException {
		
		try {
			jedisPool = getDataSorce(property);
		} catch (Exception e) {
			log.error(e);
			throw new ConectionException(e);
		}
	}

	
	private JedisPool getDataSorce(Map<String,String> property) throws Exception {
		
		JedisPoolConfig config = new JedisPoolConfig();
		
		Iterator<String> it = property.keySet().iterator();
		
		String host = null;
		String port = null;
		
		while(it.hasNext()){
			String propertyName = it.next();
			String propertyValue = property.get(propertyName);
			
			if(propertyName.equals("host")){
				host = propertyValue;
				continue;
			}
			
			if(propertyName.equals("port")){
				port = propertyValue;
				continue;
			}
			
			String methodName = getMethodName(propertyName);

			Object value = null;
			Method methodArray[] = config.getClass().getMethods();
			
			for(int i = 0; i < methodArray.length;i++){
				Method method = methodArray[i];
				
				if(method.getName().equals(methodName)){
					Class<?> parameter = method.getParameterTypes()[0];
					
					if(!parameter.getName().equals("java.lang.String")){
						Method parameterMethod = config.getClass().getMethod("valueOf",String.class);
						value = parameterMethod.invoke(propertyValue);
					}else{
						value = propertyValue;
					}
					method.invoke(config, new Object[]{value});
					break;
				}
			}
		}
		
		JedisPool pool = new JedisPool(config,host,Integer.valueOf(port));
		
		return pool;
	}
	
	private String getMethodName(String name){
		String methodName = "set"+name.substring(0,1).toUpperCase()+name.substring(1);
		return methodName;
	}
}

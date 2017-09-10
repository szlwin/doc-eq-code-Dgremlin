package com.orm.common.xml.parse.datasource;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.orm.common.config.Config;
import com.orm.common.xml.exception.XMLParseException;
import com.orm.common.xml.model.config.ConfigInfo;
import com.orm.common.xml.model.config.DataSourceConfigInfo;
import com.orm.common.xml.model.config.datasource.DataSource;
import com.orm.common.xml.parse.ElementParser;
import com.orm.common.xml.util.ConfigManager;
import com.orm.connection.datasource.DataSourceFactory;
import com.orm.context.data.DataUtil;

public class DataSourceParser implements ElementParser<DataSource>{

	Log log = LogFactory.getLog(DataSourceParser.class);
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DataSource parse(Element element) throws XMLParseException {
		
		DataSource dataSource = new DataSource();
		String dataSrcname = element.attributeValue(DataSource.NAME);
		
		Object dataSourceObj = DataUtil.getConfigInfo().get(Config.DATASOURCE, dataSrcname);
		
		if(dataSourceObj != null){
			throw new XMLParseException("The data source:"+dataSrcname+" is existed!");
		}
		
		dataSource.setName(dataSrcname);
		
		String type = element.elementText(DataSource.NAME);
		
		Object dataSourceInfo = DataUtil.getConfigInfo().get(Config.DATASOURCE_CONFIG, type);
		
		if(dataSourceInfo == null){
			throw new XMLParseException("The type:"+type+" is not existed!");
		}
		
		dataSource.setType(type);
		
		
		String factory = element.attributeValue(DataSource.INTERFACE);
		
		if(factory != null && !"".equals(factory)){
			try {
				com.orm.connection.datasource.DataSource ds = getDataSorceByFactory(factory);
				dataSource.setDataSource(ds);
				return dataSource;
			} catch (Exception e) {
				log.error(e);
				throw new XMLParseException("Parse dataSource error!");
			}
		}
		
		String dataSourceClass = element.attributeValue(DataSource.DATA_SOURCE_CLASS);
		
		ConfigInfo configInfo = ConfigManager.getInstance().getConfigInfo();
		
		DataSourceConfigInfo dataSourceConfigInfo 
			= (DataSourceConfigInfo) configInfo.get(Config.DATASOURCE_CONFIG,dataSource.getType());
		
		String dClass = dataSourceConfigInfo.getDataSource();
		
		String sqlType =  element.attributeValue(DataSource.TYPE);
		if((dataSourceClass != null && !"".equals(dataSourceClass))
				|| !sqlType.equals("sql")){
			try {
				com.orm.connection.datasource.DataSource dd 
					= (com.orm.connection.datasource.DataSource) Class.forName(dClass).newInstance();
				dd.init(type, getProperty(element,dataSourceClass));
				dataSource.setDataSource(dd);
				return dataSource;
			}catch (Exception e) {
				log.error(e);
				throw new XMLParseException(e);
			}
		}

		
		/*if((dataSourceClass != null && !"".equals(dataSourceClass))
				|| type.equals("Redis")){
			try {
				Object ds = null;
				
				if(type.equals("Redis")){
					ds = getRedisDataSorceByClass(element);
				}else{
					
					ds = getDataSorceByClass(element,dataSourceClass);
				}
				
				String dClass = dataSourceConfigInfo.getDataSource();
				
				com.orm.connection.datasource.DataSource dd 
					= (com.orm.connection.datasource.DataSource) Class.forName(dClass).newInstance();
				
				dd.setDataSource(ds);
				dataSource.setDataSource(dd);
				return dataSource;
			} catch (Exception e) {
				log.error(e);
				throw new XMLParseException(e);
			}
		}*/

		String driverClass = element.elementText(DataSource.DRIVER_CLASS);
		dataSource.setDriverClass(driverClass);
		
		String url = element.elementText(DataSource.URL);
		dataSource.setUrl(url);
		
		String userName = element.elementText(DataSource.USERNAME);
		dataSource.setUserName(userName);
		
		String passWord = element.elementText(DataSource.PASSWORD);
		dataSource.setPassWord(passWord);
		
		return dataSource;
	}
	
	private Object getRedisDataSorceByClass(Element element) throws Exception {
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
		if(host == null)
			throw new XMLParseException("The host-property is not defined for type:Redis");
		
		if(port == null)
			throw new XMLParseException("The port-property is not defined for type:Redis");
		JedisPool pool = new JedisPool(config,host,Integer.valueOf(port));
		return pool;
	}

	@SuppressWarnings("rawtypes")
	private Object getDataSorceByClass(Element element,
			String name) throws Exception {
		Class<?> classObj = Class.forName(name);
		
		Object object = classObj.newInstance();
		Iterator it = element.element("properties").elementIterator("property");
		while(it.hasNext()){
			Element e = (Element) it.next();
			
			String propertyName = e.attributeValue("name");
			String propertyValue = e.attributeValue("value");
			
			String methodName = getMethodName(propertyName);

			Object value = null;
			Method methodArray[] = classObj.getMethods();
			
			for(int i = 0; i < methodArray.length;i++){
				Method method = methodArray[i];
				
				if(method.getName().equals(methodName)){
					Class<?> parameter = method.getParameterTypes()[0];
					
					if(!parameter.getName().equals("java.lang.String")){
						Method parameterMethod = classObj.getMethod("valueOf",String.class);
						value = parameterMethod.invoke(propertyValue);
					}else{
						value = propertyValue;
					}
					method.invoke(object, new Object[]{value});
					break;
				}
			}
		}
		return object;
	}

	private Map<String,String> getProperty(Element element,String dataSourceClass){
		Map<String,String> property = new HashMap<String,String>();
		Iterator it = element.element("properties").elementIterator("property");
		
		while(it.hasNext()){
			Element e = (Element) it.next();
			
			String propertyName = e.attributeValue("name");
			String propertyValue = e.attributeValue("value");
			
			property.put(propertyName, propertyValue);
		}
		property.put("dataSourceClass", dataSourceClass);
		return property;
	}
	
	private String getMethodName(String name){
		String methodName = "set"+name.substring(0,1).toUpperCase()+name.substring(1);
		return methodName;
	}
	
	@SuppressWarnings("rawtypes")
	private com.orm.connection.datasource.DataSource getDataSorceByFactory(String name) throws Exception{
		Class<?> classObj = Class.forName(name);
		
		Object object = classObj.newInstance();
		if(!(object instanceof DataSourceFactory)){
			log.error("The name is not the type of interface: com.orm.connection.datasource.DataSourceFactory");
			throw new XMLParseException("The name is not the type of interface: com.orm.connection.datasource.DataSourceFactory");
		}
		Method method = classObj.getMethod("getDataSource", new Class[]{});
		
		Object dataSourceObje = method.invoke(object, new Object[]{});
		return (com.orm.connection.datasource.DataSource) dataSourceObje;
	}

	/*private javax.sql.DataSource getDataSorceByClass(Element element,String name) throws Exception{
		Class<?> classObj = Class.forName(name);
		
		element.getElementsByTagName(DataSource.PROPERTIES).item(0).getChildNodes();
		Object object = classObj.newInstance();
		
		
		Method method = classObj.getMethod("getDataSource", new Class[]{});
		
		Object dataSourceObje = method.invoke(object, new Object[]{});
		return (javax.sql.DataSource) dataSourceObje;
	}*/
}

package com.orm.sql.connection.datasource;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.orm.connection.exception.ConectionException;

public abstract class AbstractSqlDataSource implements SqlDataSource {

	Log log = LogFactory.getLog(AbstractSqlDataSource.class);
	
	protected javax.sql.DataSource dataSource;
	
	public void setDataSource(DataSource e) {
		this.dataSource = e;
		
	}

	public void init(String type,Map<String,String> property) throws ConectionException{
		
		String dataSourceClass = property.get("dataSourceClass");
		if((dataSourceClass != null && !"".equals(dataSourceClass))){
			try {
				dataSource = getDataSorceByClass(property,dataSourceClass);
			} catch (Exception e) {
				log.error(e);
				throw new ConectionException(e);
			}
		}
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}
	
	public Connection getConnection() throws ConectionException {
		Connection con = null;
		try {
			con = dataSource.getConnection();
		} catch (SQLException e) {
			throw new ConectionException(e);
		}
		return con;
	}

	private DataSource getDataSorceByClass(Map<String,String> property,
			String name) throws Exception {
		Class<?> classObj = Class.forName(name);
		
		Object object = classObj.newInstance();
		Iterator<String> it = property.keySet().iterator();
		
		while(it.hasNext()){
			String propertyName = it.next();
			String propertyValue = property.get(propertyName);
			
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
		return (DataSource) object;
	}
	
	private String getMethodName(String name){
		String methodName = "set"+name.substring(0,1).toUpperCase()+name.substring(1);
		return methodName;
	}
}

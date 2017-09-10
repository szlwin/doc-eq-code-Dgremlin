package com.orm.sql.connection.datasource.parse;

import java.lang.reflect.Method;
import java.util.Iterator;

import javax.sql.DataSource;

import org.dom4j.Element;

public class SqlDataSoureInfoParser implements DataSourceInfoParser<DataSource,Element>{

	public DataSource parser(Element element) throws Exception {
		String dataSourceClass = element.attributeValue(com.orm.common.xml.model.config.datasource.DataSource.DATA_SOURCE_CLASS);
		Class<?> classObj = Class.forName(dataSourceClass);
		
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
					
					System.out.println(parameter.getName());
					
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

	protected String getMethodName(String name){
		String methodName = "set"+name.substring(0,1).toUpperCase()+name.substring(1);
		return methodName;
	}
}

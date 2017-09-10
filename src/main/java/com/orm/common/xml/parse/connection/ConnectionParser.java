package com.orm.common.xml.parse.connection;

import java.util.Iterator;
import java.util.Map;

import org.dom4j.Element;

import com.orm.common.config.Config;
import com.orm.common.config.DataSourceConfig;
import com.orm.common.xml.exception.XMLParseException;
import com.orm.common.xml.model.config.ConnectionInfo;
import com.orm.common.xml.model.config.connection.Connection;
import com.orm.common.xml.model.config.datasource.DataSource;
import com.orm.common.xml.parse.ElementParser;
import com.orm.context.data.DataUtil;

public class ConnectionParser implements ElementParser<Connection>{


	@SuppressWarnings("rawtypes")
	public Connection parse(Element element) throws XMLParseException{
		Connection connection = new Connection();
		
		String conName = element.attributeValue(Connection.NAME);
		Object connectionObj = DataUtil.getConfigInfo().get(Config.CONNECTION, conName);
		
		if(connectionObj != null){
			throw new XMLParseException("The con:"+conName+" is existed!");
		}
		
		connection.setName(element.attributeValue(Connection.NAME));
		
		Iterator nodeList = element.element("data-source-info").elementIterator(Connection.DATA_SOURCE);
		while(nodeList.hasNext()){
			
			String name = ((Element)nodeList.next()).attributeValue(Connection.DATA_SOURCE_REF);
			DataSource dataSource = getDataSource(name);
			
			if(dataSource == null)
				throw new XMLParseException("The dataSource name: "+name+" is not existed!");
			
			dataSource.setConName(connection.getName());
			connection.getDataSourceInfo().addDataSource(dataSource);
			
			connection.setConnectionInfo(
					(ConnectionInfo) DataUtil.getConfigInfo().get(Config.CONNECTION_CONFIG, dataSource.getType()));
			
		}
		Element elementPro = element.element("property-info");
		
		if(elementPro != null){
			Iterator propertyList = element.element("property-info").elementIterator(Connection.PROPERTY);
			
			Map<String,String> propertyInfo = connection.getPropertyInfo();
			while(propertyList.hasNext()){
				Element elementProperty = (Element) propertyList.next();
				String name = elementProperty.attributeValue("name");
				
				String value = elementProperty.attributeValue("value");
				
				propertyInfo.put(name, value);
			}
		}

		return connection;

	}
	
	private DataSource getDataSource(String name)
	{
		return DataSourceConfig.getInstance().get(name);
		
	}

}

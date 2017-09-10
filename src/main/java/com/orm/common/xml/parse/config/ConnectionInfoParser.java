package com.orm.common.xml.parse.config;

import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.orm.common.config.Config;
import com.orm.common.xml.exception.XMLParseException;
import com.orm.common.xml.model.config.ConfigInfo;
import com.orm.common.xml.model.config.ConnectionInfo;
import com.orm.common.xml.model.config.DataSourceConfigInfo;
import com.orm.common.xml.parse.FileParser;
import com.orm.common.xml.parse.connection.config.ConnectionConfigInfoParser;
import com.orm.common.xml.parse.datasource.config.DataSourceConfigInfoParser;
import com.orm.common.xml.util.ConfigManager;

public class ConnectionInfoParser implements FileParser<ConfigInfo>{

	Log log = LogFactory.getLog(ConnectionInfoParser.class);
	
	public ConfigInfo parse(String filePath) throws XMLParseException {
		Document doc = null;

		ConfigInfo configInfo = new ConfigInfo();
		ConfigManager.getInstance().setConfigInfo(configInfo);
		
		log.info("------SZLOrm init Connection Start------");
		
		log.info("------load the "+filePath+" Start------");
		try {
			
			InputStream fileStream 											
				= Thread.currentThread().getContextClassLoader()
				.loadClass("DoNotDeleteMe")
				.getResourceAsStream(filePath);
			//System.out.println(Thread.currentThread().getContextClassLoader().loadClass("DoNotDeleteMe").getResource(filePath).getPath());
			
			SAXReader saxReader = new SAXReader();
			doc = saxReader.read(fileStream); 
			
			parserDataSource(configInfo,doc);
			
			parserConnection(configInfo,doc);
		} catch (Exception e) {
			log.error(e);
			throw new XMLParseException(e);
		}
		
		log.info("------load the "+filePath+" end------");
		
		log.info("------SZLOrm init Connection end------");
		return configInfo;
	}
	
	@SuppressWarnings("rawtypes")
	private void parserDataSource(ConfigInfo configInfo,Document doc) throws XMLParseException{
		Element element = doc.getRootElement().element(ConfigInfo.DATASOURCE_CONFIG);
		Iterator dataSourceList =  element.elementIterator("orm-dataSource");
		
		while(dataSourceList.hasNext())
		{
			DataSourceConfigInfoParser dataSourceParser = new DataSourceConfigInfoParser();
			DataSourceConfigInfo dataSource = dataSourceParser.parse((Element)dataSourceList.next());
			
			configInfo.add(Config.DATASOURCE_CONFIG, dataSource);
			log.info("Load the DataSourceConfig:"+dataSource.getName()+" success!");
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void parserConnection(ConfigInfo configInfo,Document doc) throws XMLParseException{
		Element element = doc.getRootElement().element(ConfigInfo.CONNECTION_CONFIG);
		
		Iterator connectionInfoList =  element.elementIterator("orm-connection");
		
		while(connectionInfoList.hasNext())
		{
			ConnectionConfigInfoParser connectionInfoParser = new ConnectionConfigInfoParser();
			ConnectionInfo connectionInfo = connectionInfoParser.parse((Element)connectionInfoList.next());
			
			configInfo.add(Config.CONNECTION_CONFIG, connectionInfo);
			log.info("Load the ConnectionConfig:"+connectionInfo.getName()+" success!");
		}
		
	}
	
	
	

}

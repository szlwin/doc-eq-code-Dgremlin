package dec.context.parse.xml.parse.config;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;


import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dec.context.parse.xml.exception.XMLParseException;
import dec.context.parse.xml.parse.FileParser;
import dec.context.parse.xml.parse.connection.ConnectionParser;
import dec.context.parse.xml.parse.datasource.DataSourceParser;
import dec.core.context.config.manager.ConfigManager;
import dec.core.context.config.model.config.ConfigInfo;
import dec.core.context.config.model.connection.Connection;
import dec.core.context.config.model.datasource.DataSource;

public class ConfigFileParser implements FileParser<ConfigInfo>{

	private final static Logger log = LoggerFactory.getLogger(ConfigFileParser.class);
	
	public ConfigInfo parse(String filePath) throws XMLParseException{
		
		log.info("------Dec init Start------");
		
		ConfigInfo configInfo = ConfigManager.getInstance().getConfigInfo();
		//ConfigManager.getInstance().setConfigInfo(configInfo);
		try {
			
			
			SAXReader saxReader = new SAXReader();
			Document doc = null;

			
			if(filePath.startsWith("classpath:")){
				filePath = filePath.substring("classpath:".length());
				
				InputStream fileStream 											
					= ConfigFileParser.class
					.getClassLoader()
					.getResourceAsStream(filePath);
				 
				 doc = saxReader.read(fileStream); 
				 
			}else{
				
				doc = saxReader.read(new File(filePath)); 
			}
			
			log.info("Load the "+filePath+" Start");
			
			parseDataSource(configInfo,doc);
			
			parseConnection(configInfo,doc);

			log.info("Load the "+filePath+" End");
			
			log.info("Load the other config file Start!");
			for(int i = 0; i < CommonParser.TYPE_SIZE;i++){
				CommonParser commonParser = new CommonParser(i);
				commonParser.parseFile(configInfo, doc);
			}
			log.info("Load the other config file End!");
			
			//parseDataFile(configInfo,doc);
			
			//parseRelationFile(configInfo,doc);
			
			//parseViewFile(configInfo,doc);
			
			//parseRuleFile(configInfo,doc);
		} catch (Exception e) {
			log.info("------Dec init error------",e);
			throw new XMLParseException(e);
		}
		log.info("------Dec init End------");
		return configInfo;
	}
	
	@SuppressWarnings("rawtypes")
	private void parseConnection(ConfigInfo configInfo,Document doc) throws XMLParseException
	{
		log.info("Load the Connection Start!");
		
		Element element = doc.getRootElement().element(ConfigInfo.CONNECTION_INFO);
		
		configInfo.setDefaultConnection(element.attributeValue(
				ConfigInfo.DEFAULT_CONNECTION));
		
		Iterator it =  element.elementIterator("orm-connection");
		
		while(it.hasNext()){
			Element e = (Element) it.next();
			ConnectionParser connectionParser = new ConnectionParser();
			Connection con = connectionParser.parse((Element)e);
			
			configInfo.addConnection(con);
			log.info("Load the Connection:"+con.getName()+" success!");
			
		}
		
		log.info("Load the Connection End!");
	}

	@SuppressWarnings("rawtypes")
	private void parseDataSource(ConfigInfo configInfo,Document doc) throws XMLParseException
	{
		log.info("Load the DataSource Start!");
		Element dataSrcElement = doc.getRootElement().element(ConfigInfo.DATASOURCE_INFO);
		
		
		configInfo.setDefaultDataSource(dataSrcElement.attributeValue(
				ConfigInfo.DEFAULT_DATASOURCE));
		
		Iterator dataSourceList = dataSrcElement.elementIterator("orm-datasource");
		
		while(dataSourceList.hasNext())
		{
			DataSourceParser dataSourceParser = new DataSourceParser();
			DataSource dataSource = dataSourceParser.parse((Element) dataSourceList.next());
			
			configInfo.addDataSource(dataSource);
			log.info("Load the DataSource:"+dataSource.getName()+" success!");
		}
		
		log.info("Load the DataSource End!");
	}
	
	/*
	private void parseDataFile(ConfigInfo configInfo,Document doc) throws XMLParseException
	{
		NodeList list = doc.getElementsByTagName(ConfigInfo.DATA_FILE_INFO);
		Element element = (Element)list.item(0);
		
		//��ȡfileԪ��
		NodeList fileList = element.getElementsByTagName("orm-file");
		
		for(int i = 0; i < fileList.getLength();i++)
		{
			Element fileElement = (Element)fileList.item(i);
			String filePath = fileElement.getAttribute("path");
			DataFileParse dataFileParse = new DataFileParse();
			
			List<Data> dataList = dataFileParse.parse(filePath);
			
			configInfo.addDataList(dataList);
		}
	}
	
	
	private void parseRelationFile(ConfigInfo configInfo,Document doc) throws XMLParseException
	{
		NodeList list = doc.getElementsByTagName(ConfigInfo.RELATION_FILE_INFO);
		Element element = (Element)list.item(0);
		
		//��ȡfileԪ��
		NodeList fileList = element.getElementsByTagName("orm-file");
		
		for(int i = 0; i < fileList.getLength();i++)
		{
			Element fileElement = (Element)fileList.item(i);
			String filePath = fileElement.getAttribute("path");
			
			RelationFileParser relationFileParse = new RelationFileParser();
			List<Relation> relationList = relationFileParse.parse(filePath);
			
			configInfo.addRelationList(relationList);
		}
	}
	
	
	private void parseViewFile(ConfigInfo configInfo,Document doc) throws XMLParseException
	{
		NodeList list = doc.getElementsByTagName(ConfigInfo.VIEW_FILE_INFO);
		Element element = (Element)list.item(0);
		
		//��ȡfileԪ��
		NodeList fileList = element.getElementsByTagName("orm-file");
		
		for(int i = 0; i < fileList.getLength();i++)
		{
			Element fileElement = (Element)fileList.item(i);
			String filePath = fileElement.getAttribute("path");
			
			ViewFileParser viewnFileParse = new ViewFileParser();
			List<ViewData> viewDataList = viewnFileParse.parse(filePath);
			
			configInfo.addViewDataList(viewDataList);
		}
	}
	
	
	private void parseRuleFile(ConfigInfo configInfo,Document doc) throws XMLParseException
	{
		NodeList list = doc.getElementsByTagName(ConfigInfo.RULE_FILE_INFO);
		Element element = (Element)list.item(0);
		
		//��ȡfileԪ��
		NodeList fileList = element.getElementsByTagName("orm-file");
		
		for(int i = 0; i < fileList.getLength();i++)
		{
			Element fileElement = (Element)fileList.item(i);
			String filePath = fileElement.getAttribute("path");
			
			RuleFileParser ruleFileParse = new RuleFileParser();
			List<RuleViewInfo> ruleDataList = ruleFileParse.parse(filePath);
			
			configInfo.addRuleViewInfoList(ruleDataList);
		}
	}*/
}

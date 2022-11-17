package dec.context.parse.xml.parse.config;

import java.util.Iterator;
import java.util.List;


import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dec.context.parse.xml.exception.XMLParseException;
import dec.context.parse.xml.parse.AbstractFileParser;
import dec.context.parse.xml.parse.data.DataFileParse;
import dec.context.parse.xml.parse.relation.RelationFileParser;
import dec.context.parse.xml.parse.rule.RuleFileParser;
import dec.context.parse.xml.parse.service.ServiceFileParser;
import dec.context.parse.xml.parse.view.ViewFileParser;
import dec.core.context.config.model.config.ConfigInfo;
import dec.core.context.config.model.config.data.ConfigBaseData;

public class CommonParser {

	private final static Logger log = LoggerFactory.getLogger(CommonParser.class);
	
	public static int TYPE_SIZE = 5;
	
	public static int START_SIZE = 2;
	
	private static String nodeArray[] = new String[TYPE_SIZE];
	
	//private static AbstractFileParser parserArray[] = new AbstractFileParser[TYPE_SIZE];
	
	static{
		//nodeArray[0] = ConfigInfo.DATASOURCE_INFO;
		//nodeArray[1] = ConfigInfo.CONNECTION_INFO;
		nodeArray[0] = ConfigInfo.DATA_FILE_INFO;
		nodeArray[1] = ConfigInfo.RELATION_FILE_INFO;
		nodeArray[2] = ConfigInfo.VIEW_FILE_INFO;
		nodeArray[3] = ConfigInfo.RULE_FILE_INFO;
		nodeArray[4] = ConfigInfo.SERVICE_FILE_INFO;
	}
	

	private int type;
	
	private AbstractFileParser fileParser;
	
	public CommonParser(int type){
		this.type = type;
		this.fileParser = getFileParser();
	}
	
	public void parseFile(ConfigInfo configInfo,Document doc) throws XMLParseException
	{
		//System.out.println(nodeArray[type]);
		Element element = doc.getRootElement().element(nodeArray[type]);
		
		if(element == null)
			return;
		
		Iterator<?> it = element.elementIterator("orm-file");
		
		while(it.hasNext()){
			Element fileElement = (Element) it.next();
			
			String filePath = fileElement.attributeValue("path");
			
			log.info("Load the filePath:"+filePath+" Start!");
			
			List<ConfigBaseData> dataList = fileParser.parse(filePath);
			
			configInfo.addList(type+START_SIZE,dataList);
			
			log.info("Load the filePath:"+filePath+" End!");
		}
	}
	
	private AbstractFileParser getFileParser(){
		switch(type){
			case 0:
				return new DataFileParse();
			case 1:
				return new RelationFileParser();
			case 2:
				return new ViewFileParser();
			case 3:
				return new RuleFileParser();
			case 4:
				return new ServiceFileParser();
		}
		
		return null;
	}
}

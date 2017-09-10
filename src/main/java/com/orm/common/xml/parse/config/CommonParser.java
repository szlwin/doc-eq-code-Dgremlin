package com.orm.common.xml.parse.config;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import com.orm.common.xml.exception.XMLParseException;
import com.orm.common.xml.model.config.ConfigBaseData;
import com.orm.common.xml.model.config.ConfigInfo;
import com.orm.common.xml.parse.AbstractFileParser;
import com.orm.common.xml.parse.data.DataFileParse;
import com.orm.common.xml.parse.relation.RelationFileParser;
import com.orm.common.xml.parse.rule.RuleFileParser;
import com.orm.common.xml.parse.service.ServiceFileParser;
import com.orm.common.xml.parse.view.ViewFileParser;

public class CommonParser {

	Log log = LogFactory.getLog(CommonParser.class);
	
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

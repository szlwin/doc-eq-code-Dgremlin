package com.orm.common.xml.parse;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.orm.common.xml.exception.XMLParseException;
import com.orm.common.xml.model.config.ConfigBaseData;

public abstract class AbstractFileParser implements FileParser<List<ConfigBaseData>>{

	Log log = LogFactory.getLog(AbstractFileParser.class);
	
	protected AbstarctElementsParser elementsParser;
	
	protected String nodeName;
	
	public List<ConfigBaseData> parse(String filePath) throws XMLParseException {
		
		elementsParser = getParser();
		
		nodeName = getNodeNme();
		
		List<ConfigBaseData> list = null;
		try{
			if(filePath.endsWith(".xml"))
				list = parseByFile(filePath);
			else
				list = parseByDirectory(filePath);
		}catch(Exception e)
		{
			throw new XMLParseException(e);
		}

		return list;
	}

	@SuppressWarnings("rawtypes")
	protected List<ConfigBaseData> parseByFile(String filePath) throws Exception
	{
		log.info("Load the file:"+filePath+" Start!");
		List<ConfigBaseData> list = new ArrayList<ConfigBaseData>();
		
		SAXReader saxReader = new SAXReader();
		Document doc = saxReader.read(new File(filePath)); 
	
		Iterator it = doc.getRootElement().elementIterator(nodeName);
		
		while(it.hasNext()){
			ConfigBaseData data = parseElements((Element)it.next());
			
			list.add(data);
		}
		log.info("Load the file:"+filePath+" End!");
		return list;
		
	}
	
	protected List<ConfigBaseData> parseByDirectory(String filePath) throws Exception
	{
		List<ConfigBaseData> dataList = new ArrayList<ConfigBaseData>();
		
		File file =  new File(filePath);
		if(file.isDirectory())
		{
			File fileArray[] = file.listFiles();
			
			for(int i = 0; i < fileArray.length; i++)
			{
				String tempFilePath = fileArray[i].getCanonicalPath();
				if(tempFilePath.endsWith(".xml"))
				{
					List<ConfigBaseData> list = parseByFile(tempFilePath);
					dataList.addAll(list);
				}
			}
			
		}else
		{
			throw new XMLParseException("The file Directory: "+filePath+" is error!");
		}
		return dataList;
		
	}
	
	protected ConfigBaseData parseElements(Element element) throws XMLParseException
	{
		return elementsParser.parse(element);
	}
	
	protected abstract AbstarctElementsParser getParser();
	
	protected abstract String getNodeNme();
}

package com.orm.common.xml.parse.datasource.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import com.orm.common.xml.exception.XMLParseException;
import com.orm.common.xml.model.config.DataSourceConfigInfo;
import com.orm.common.xml.parse.ElementParser;

public class DataSourceConfigInfoParser implements ElementParser<DataSourceConfigInfo>{

	Log log = LogFactory.getLog(DataSourceConfigInfoParser.class);
	
	public DataSourceConfigInfo parse(Element element) throws XMLParseException {
		
		DataSourceConfigInfo dataSource = new DataSourceConfigInfo();
		
		dataSource.setName(element.attributeValue(DataSourceConfigInfo.NAME));
		
		dataSource.setType(element.attributeValue(DataSourceConfigInfo.TYPE));
		
		String dataSourceClass = element.elementText(DataSourceConfigInfo.DATASOURCE);
		dataSource.setDataSource(dataSourceClass);
		
		return dataSource;
	}
	

}

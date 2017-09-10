package com.orm.common.xml.util;

import com.orm.common.xml.exception.XMLParseException;
import com.orm.common.xml.parse.config.ConfigFileParser;
import com.orm.common.xml.parse.config.ConnectionInfoParser;
public class ConfigUtil {
	
	public static void parseConfigInfo(String filePath) throws XMLParseException
	{
		ConfigFileParser configFileParser = new ConfigFileParser();
		
		configFileParser.parse(filePath);
		
	}
	
	public static void parseConnectionInfo(String filePath) throws XMLParseException{
		ConnectionInfoParser connectionInfoParser = new ConnectionInfoParser();
		
		connectionInfoParser.parse(filePath);
	}
	

}

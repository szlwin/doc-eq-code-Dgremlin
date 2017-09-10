package com.orm.common.xml;

import com.orm.common.xml.exception.XMLParseException;
import com.orm.common.xml.util.ConfigUtil;

public class Config {

	private String filePath;
	
	public Config(String filePath)
	{
		this.filePath = filePath;
	}
	
	public void initContext() throws XMLParseException
	{
		initConnectionContext();
		
		ConfigUtil.parseConfigInfo(filePath);
		//ConfigInfo configInfo = ConfigUtil.parseConfigInfo(filePath);
		
		//ConfigManager.getInstance().setConfigInfo(configInfo);
	}
	
	
	private void initConnectionContext() throws XMLParseException{
		ConfigUtil.parseConnectionInfo("orm-con-config.xml");
	}
}

package dec.demo.config;


import dec.context.parse.xml.exception.XMLParseException;
import dec.core.starter.common.ConfigUtil;

public class ConfigInit {


	public static void main(String args[]) throws XMLParseException{
		init();
	}
	
	public static void init() throws XMLParseException{
		ConfigUtil.parseConnectionInfo("classpath:model/orm-con-config.xml");
		
		
		ConfigUtil.parseConfigInfo("classpath:model/orm-config.xml");
		
	}
	
	
	
}

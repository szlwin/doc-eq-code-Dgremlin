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
import dec.context.parse.xml.parse.connection.config.ConnectionConfigInfoParser;
import dec.context.parse.xml.parse.datasource.config.DataSourceConfigInfoParser;
import dec.core.context.config.manager.ConfigManager;
import dec.core.context.config.model.config.Config;
import dec.core.context.config.model.config.ConfigInfo;
import dec.core.context.config.model.connection.ConnectionInfo;
import dec.core.context.config.model.datasource.DataSourceConfigInfo;

public class ConnectionInfoParser implements FileParser<ConfigInfo>{

	private final static Logger log = LoggerFactory.getLogger(ConnectionInfoParser.class);
	
	public ConfigInfo parse(String filePath) throws XMLParseException {
		ConfigInfo candidate = new ConfigInfo();
		ConfigInfo parsed = parseInto(candidate, filePath);
		// 兼容原有启动方式：只有整个文件解析成功后才替换当前配置。
		ConfigManager.getInstance().setConfigInfo(parsed);
		return parsed;
	}

	/** 把连接类型解析到候选配置，不提前影响正在运行的业务配置。 */
	public ConfigInfo parseInto(final ConfigInfo candidate, final String filePath)
			throws XMLParseException {
		try {
			return ConfigManager.getInstance().withConfigInfo(
					candidate,
					new ConfigManager.ConfigOperation<ConfigInfo>() {
						@Override
						public ConfigInfo execute() throws Exception {
							return parseCurrent(candidate, filePath);
						}
					});
		} catch (XMLParseException e) {
			throw e;
		} catch (Exception e) {
			throw new XMLParseException(e);
		}
	}

	private ConfigInfo parseCurrent(ConfigInfo configInfo, String filePath)
			throws XMLParseException {
		Document doc = null;
		
		log.info("------Dec init Connection Start------");
		
		log.info("------load the "+filePath+" Start------");
		
		SAXReader saxReader = new SAXReader();
		
		try {

			if(filePath.startsWith("classpath:")){
				filePath = filePath.substring("classpath:".length());
				
				InputStream fileStream 											
					= ConnectionInfoParser.class
					.getClassLoader()
					.getResourceAsStream(filePath);
				 
				 doc = saxReader.read(fileStream); 
				 
			}else{
				
				doc = saxReader.read(new File(filePath)); 
			}
			//System.out.println(Thread.currentThread().getContextClassLoader().loadClass("DoNotDeleteMe").getResource(filePath).getPath());
			
			
			parserDataSource(configInfo,doc);
			
			parserConnection(configInfo,doc);
		} catch (Exception e) {
			log.error("Parse file error", e);
			throw new XMLParseException(e);
		}
		
		log.info("------load the "+filePath+" end------");
		
		log.info("------Dec init Connection end------");
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

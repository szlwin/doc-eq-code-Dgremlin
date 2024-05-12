package dec.context.parse.xml.parse.connection.config;


import org.dom4j.Element;

import dec.context.parse.xml.exception.XMLParseException;
import dec.context.parse.xml.parse.ElementParser;
import dec.core.context.config.model.connection.ConnectionInfo;
import dec.core.context.config.model.datasource.DataSourceConfigInfo;
/*
import com.orm.common.convert.container.ConvertContainer;
import com.orm.common.execute.container.ExecuteContainer;
import com.orm.common.xml.exception.XMLParseException;
import com.orm.common.xml.model.config.ConnectionInfo;
import com.orm.common.xml.model.config.DataSourceConfigInfo;
import com.orm.common.xml.parse.ElementParser;
*/
public class ConnectionConfigInfoParser implements ElementParser<ConnectionInfo>{
	
	public ConnectionInfo parse(Element element) throws XMLParseException {
		
		ConnectionInfo connectionInfo = new ConnectionInfo();
		
		connectionInfo.setName(element.attributeValue(DataSourceConfigInfo.NAME));
		
		connectionInfo.setType(element.attributeValue(DataSourceConfigInfo.TYPE));
		
		//String connectionClass = element.elementText(ConnectionInfo.CONNECTION);
		//connectionInfo.setConnectionClass(connectionClass);
		
		//String convertClass = element.elementText(ConnectionInfo.CONVERT);
		//connectionInfo.setConvertClass(convertClass);
		
		
		//String executeClass = element.elementText(ConnectionInfo.EXECUTER);
		//connectionInfo.setExecuteClass(executeClass);
		
		//String dataConvertClass = element.elementText(ConnectionInfo.DATA_CONVERT);
		//connectionInfo.setDataConvertClass(dataConvertClass);
		
		/**以下逻辑暂时屏蔽，移动到加载项目中**/
		/*ConvertContainer convertContainer = null;
		ExecuteContainer executeContainer = null;
		com.orm.sql.datatype.convert.ConvertContainer dataConvertContainer = null;
		try {
			connectionInfo.setConClass(Class.forName(connectionInfo.getConnectionClass()));
			
			convertContainer = (ConvertContainer) Class.forName(connectionInfo.getConvertClass()).newInstance();
			
			executeContainer = (ExecuteContainer) Class.forName(connectionInfo.getExecuteClass()).newInstance();
			
			dataConvertContainer = (com.orm.sql.datatype.convert.ConvertContainer)Class.forName(connectionInfo.getDataConvertClass()).newInstance();
		} catch (Exception e) {
			log.error("Parse error",e);
			throw new XMLParseException(e);
		}
		
		convertContainer.init();
		connectionInfo.setConvertContainer(convertContainer);
		
		
		executeContainer.init();
		
		connectionInfo.setExecuteContainer(executeContainer);
		
		dataConvertContainer.init();
		connectionInfo.setDataConvertContainer(dataConvertContainer);*/
		
		
		return connectionInfo;
	}
	

}

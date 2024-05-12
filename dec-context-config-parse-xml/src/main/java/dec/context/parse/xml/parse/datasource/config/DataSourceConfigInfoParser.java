package dec.context.parse.xml.parse.datasource.config;


import org.dom4j.Element;

import dec.context.parse.xml.exception.XMLParseException;
import dec.context.parse.xml.parse.ElementParser;
import dec.core.context.config.model.datasource.DataSourceConfigInfo;

/*import com.orm.common.xml.exception.XMLParseException;
import com.orm.common.xml.model.config.DataSourceConfigInfo;
import com.orm.common.xml.parse.ElementParser;
*/
public class DataSourceConfigInfoParser implements ElementParser<DataSourceConfigInfo>{
	
	public DataSourceConfigInfo parse(Element element) throws XMLParseException {
		
		DataSourceConfigInfo dataSource = new DataSourceConfigInfo();
		
		dataSource.setName(element.attributeValue(DataSourceConfigInfo.NAME));
		
		dataSource.setType(element.attributeValue(DataSourceConfigInfo.TYPE));
		
		String dataSourceClass = element.elementText(DataSourceConfigInfo.DATASOURCE);
		dataSource.setDataSource(dataSourceClass);
		
		return dataSource;
	}
	

}

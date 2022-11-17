package dec.external.datasource.sql.mysql.convert.container.factory;

import dec.core.datasource.convert.container.ConvertContainer;
import dec.core.datasource.convert.container.factory.ConvertContainerFacory;
import dec.external.datasource.sql.dom.ConvertInfo;
import dec.external.datasource.sql.dom.ConvertParam;
import dec.external.datasource.sql.mysql.convert.container.MySQLConvertContainer;

public class MySQLConvertContainerFactory implements ConvertContainerFacory<ConvertParam, ConvertInfo>{

	private MySQLConvertContainer mySQLConvert = new MySQLConvertContainer();
	
	@Override
	public ConvertContainer<ConvertParam, ConvertInfo> getConvertContainer() {
		return mySQLConvert;
	}

}

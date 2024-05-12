package dec.external.datasource.sql.mysql.datatype.convert.factory;

import dec.core.datasource.datatype.convert.DataConvertContainer;
import dec.core.datasource.datatype.convert.factory.DataConvertContainerFacory;
import dec.external.datasource.sql.mysql.datatype.convert.MySQLDataTypeConvert;

public class MySQLDataConvertContainerFactory implements DataConvertContainerFacory{

	private MySQLDataTypeConvert dataTypeConvert = new MySQLDataTypeConvert();

	@Override
	public DataConvertContainer getDataConvertContainer() {
		return dataTypeConvert;
	}
	


}

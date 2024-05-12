package dec.external.datasource.sql.mysql.datatype.convert;

import dec.core.datasource.datatype.convert.DataConvert;

//import com.orm.sql.datatype.convert.DataConvert;

public class SqlTimeToDate implements DataConvert<java.sql.Time,java.util.Date>{

	public java.util.Date convert(java.sql.Time data) {

		java.util.Date date = new java.util.Date(data.getTime());
		return date;
	}

}

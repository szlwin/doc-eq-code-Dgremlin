package dec.external.datasource.sql.mysql.datatype.convert;

import dec.core.datasource.datatype.convert.DataConvert;

//import com.orm.sql.datatype.convert.DataConvert;

public class SqlDateToDate implements DataConvert<java.sql.Date,java.util.Date>{

	public java.util.Date convert(java.sql.Date data) {
		
		java.util.Date date = new java.util.Date(data.getTime());

		return date;
	}

}

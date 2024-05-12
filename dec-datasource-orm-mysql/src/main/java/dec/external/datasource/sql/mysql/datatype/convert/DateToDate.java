package dec.external.datasource.sql.mysql.datatype.convert;

import dec.core.datasource.datatype.convert.DataConvert;

//import com.orm.sql.datatype.convert.DataConvert;

public class DateToDate implements DataConvert<java.util.Date,java.util.Date>{

	public java.util.Date convert(java.util.Date data) {
		return data;
	}

}

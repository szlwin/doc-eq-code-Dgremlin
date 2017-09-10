package com.orm.sql.datatype.convert.oracle;

import com.orm.sql.datatype.convert.DataConvert;

public class SqlDateToDate implements DataConvert<oracle.sql.DATE,java.util.Date>{

	public java.util.Date convert(oracle.sql.DATE data) {

		java.util.Date date = new java.util.Date(data.timestampValue().getTime());
		return date;
	}

}

package com.orm.sql.datatype.convert.oracle;

import com.orm.sql.datatype.convert.DataConvert;

public class DateToSqlTimestamp implements DataConvert<java.util.Date,java.sql.Timestamp>{

	public java.sql.Timestamp convert(java.util.Date data) {

		java.sql.Timestamp sqldate = new java.sql.Timestamp(data.getTime());
		return sqldate;
	}

}

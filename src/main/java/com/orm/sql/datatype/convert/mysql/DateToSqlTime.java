package com.orm.sql.datatype.convert.mysql;

import com.orm.sql.datatype.convert.DataConvert;

public class DateToSqlTime implements DataConvert<java.util.Date,java.sql.Time>{

	public java.sql.Time convert(java.util.Date data) {

		java.sql.Time sqldate = new java.sql.Time(data.getTime());
		return sqldate;
	}

}

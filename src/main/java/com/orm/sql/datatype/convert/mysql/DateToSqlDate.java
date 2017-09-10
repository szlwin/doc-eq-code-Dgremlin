package com.orm.sql.datatype.convert.mysql;

import com.orm.sql.datatype.convert.DataConvert;

public class DateToSqlDate implements DataConvert<java.util.Date,java.sql.Date>{

	public java.sql.Date convert(java.util.Date data) {

		java.sql.Date sqldate = new java.sql.Date(data.getTime());
		return sqldate;
	}

}

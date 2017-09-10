package com.orm.sql.datatype.convert.mysql;

import com.orm.sql.datatype.convert.DataConvert;

public class SqlTimestampToDate implements DataConvert<java.sql.Timestamp,java.util.Date>{

	public java.util.Date convert(java.sql.Timestamp data) {

		java.util.Date date = new java.util.Date(data.getTime());
		return date;
	}

}

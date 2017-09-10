package com.orm.sql.datatype.convert.oracle;

import java.sql.SQLException;

import com.orm.sql.datatype.convert.DataConvert;

public class SqlTimestampToDate implements DataConvert<oracle.sql.TIMESTAMP,java.util.Date>{

	public java.util.Date convert(oracle.sql.TIMESTAMP data) {

		java.util.Date date = null;
		try {
			date = new java.util.Date(data.timestampValue().getTime());
		} catch (SQLException e) {

		}
		return date;
	}

}

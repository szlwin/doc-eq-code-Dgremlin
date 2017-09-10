package com.orm.sql.datatype.convert.hbase;

import com.orm.sql.datatype.convert.DataConvert;

public class DateToLong implements DataConvert<java.util.Date,Long>{

	public Long convert(java.util.Date data) {
		return data.getTime();
	}

}

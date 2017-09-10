package com.orm.sql.datatype.convert.hbase;

import java.util.Date;

import com.orm.sql.datatype.convert.DataConvert;

public class LongToDate implements DataConvert<Long,java.util.Date>{

	public Date convert(Long data) {
		return new Date(data);
	}

}

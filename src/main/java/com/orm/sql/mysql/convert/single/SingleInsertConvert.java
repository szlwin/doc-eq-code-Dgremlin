package com.orm.sql.mysql.convert.single;

import com.orm.context.data.BaseData;
import com.orm.sql.convert.single.AbstractSingleInsertConvert;
import com.orm.sql.util.Util;

public class SingleInsertConvert  extends AbstractSingleInsertConvert{
	
	public String convert(BaseData baseData) {
		
		StringBuffer sqlBuffer = new StringBuffer("insert into ");
		
		String tableName = Util.getTableInfo(baseData.getName(), dataSourceName)
					.getName();
		
		sqlBuffer.append(tableName);
		
		sqlBuffer.append(" ");
		
		sqlBuffer.append("(");
		
		StringBuffer columnBuffer = new StringBuffer();
		
		StringBuffer dataBuffer = new StringBuffer();
		
		convertToSql(baseData,columnBuffer,dataBuffer);
		
		sqlBuffer.append(columnBuffer.toString());
		
		sqlBuffer.append(")");
		
		sqlBuffer.append(" values ( ");
		sqlBuffer.append(dataBuffer.toString());
		sqlBuffer.append(" )");
		
		return sqlBuffer.toString();
	}

	@Override
	protected String convert() {
		return convert((BaseData) this.convertParam.getData());
	}

}

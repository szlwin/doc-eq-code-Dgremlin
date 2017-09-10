package com.orm.sql.hbase.convert.single;

import com.orm.common.xml.model.data.DataTable;
import com.orm.context.data.BaseData;
import com.orm.sql.convert.single.AbstractSingleInsertConvert;
import com.orm.sql.util.Util;

public class SingleUpsertConvert  extends AbstractSingleInsertConvert{
	
	public String convert(BaseData baseData) {
		
		StringBuffer sqlBuffer = new StringBuffer("UPSERT INTO ");
		
		DataTable dataTable = Util.getTableInfo(baseData.getName(), dataSourceName);
		
		keyType = dataTable.getKeyType();
		
		String tableName = dataTable.getName();
		
		sqlBuffer.append(tableName);
		
		//sqlBuffer.append(" ");
		
		sqlBuffer.append("(");
		
		StringBuffer columnBuffer = new StringBuffer();
		
		StringBuffer dataBuffer = new StringBuffer();
		
		convertToSql(baseData,columnBuffer,dataBuffer);
		
		sqlBuffer.append(columnBuffer.toString());
		
		sqlBuffer.append(")");
		
		sqlBuffer.append(" VALUES (");
		sqlBuffer.append(dataBuffer.toString());
		sqlBuffer.append(")");
		
		return sqlBuffer.toString();
	}

	@Override
	protected String convert() {
		return convert((BaseData) this.convertParam.getData());
	}

}

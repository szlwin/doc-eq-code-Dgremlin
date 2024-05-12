package dec.external.datasource.sql.mysql.convert.single;

import dec.core.context.data.BaseData;

import dec.external.datasource.sql.convert.single.AbstractSingleInsertConvert;
import dec.external.datasource.sql.utils.Util;

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

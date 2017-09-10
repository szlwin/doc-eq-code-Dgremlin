package com.orm.sql.oracle.convert.single;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.orm.common.xml.model.data.DataTable;
import com.orm.common.xml.util.Constanst;
import com.orm.connection.exception.ConectionException;
import com.orm.connection.factory.DataConnectionFactory;
import com.orm.context.data.BaseData;
import com.orm.sql.connection.DataBaseConnection;
import com.orm.sql.convert.single.AbstractSingleInsertConvert;
import com.orm.sql.query.SelectQuery;
import com.orm.sql.util.Util;

public class SingleInsertConvert extends AbstractSingleInsertConvert{

	private Log log = LogFactory.getLog(SingleInsertConvert.class);

	public String convert(BaseData baseData) {
		
		StringBuffer sqlBuffer = new StringBuffer("insert into ");
		
		DataTable dataTable = Util.getTableInfo(baseData.getName(), dataSourceName);
		
		String tableName = dataTable.getName();
		
		sqlBuffer.append(tableName);
		
		sqlBuffer.append(" ");
		
		sqlBuffer.append("(");
		
		StringBuffer columnBuffer = new StringBuffer();
		
		StringBuffer dataBuffer = new StringBuffer();
		
		keyType = dataTable.getKeyType();
		if(keyType != null && keyType.equals(Constanst.KEY_TYPE_SEQ))
		{
			try {
				setKeyValue(baseData,dataTable.getSeq(),dataSourceName);
			} catch (Exception e) {
				log.error("Get the key error",e);
			}
		}
		
		convertToSql(baseData,columnBuffer,dataBuffer);
		
		sqlBuffer.append(columnBuffer.toString());
		
		sqlBuffer.append(")");
		
		sqlBuffer.append(" values ( ");
		sqlBuffer.append(dataBuffer.toString());
		sqlBuffer.append(" )");
		
		return sqlBuffer.toString();
	}
	
	private void setKeyValue(BaseData baseData,String seq,String dataSourceName) throws ConectionException, SQLException{
		//String seq = Util.getSeqName(baseData.getName(), dataSourceName);
		
		String sql = "select "+seq+".nextval from dual";
		
		String conName = Util.getConNameByDataSource(dataSourceName);
		DataBaseConnection connection = (DataBaseConnection) DataConnectionFactory.getInstance().getConnection(conName);
		
		connection.connect();

		Connection con = connection.getConnection();
		SelectQuery selectQuery = new SelectQuery(con,sql,null);
		
		selectQuery.executeSQL();
		
		ResultSet rs = selectQuery.getResultSet();
		if(rs.next()){
			String key = baseData.getData().getTableInfo()
					.getTable(dataSourceName).getPropertyKey();//Util.getIdKey(baseData, dataSourceName);
			keyValue = rs.getObject(1);
			baseData.setValue(key, keyValue);
		}
		connection.close();
		
	}
	
	protected String convert() {
		return convert((BaseData) this.convertParam.getData());
	}
}

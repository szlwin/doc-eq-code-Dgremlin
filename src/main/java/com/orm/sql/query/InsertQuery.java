package com.orm.sql.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.orm.common.xml.util.Constanst;
import com.orm.sql.dom.DataInfo;
import com.orm.sql.dom.UpdateInfo;
import com.orm.sql.util.SQLUtil;

public class InsertQuery extends AbstractQuery<UpdateInfo> {

	Log log = LogFactory.getLog(InsertQuery.class);
	
	protected UpdateInfo updateInfo;
	
	private String keyType;
	
	public InsertQuery(Connection con, String sql, Collection<DataInfo> param) {
		super(con, sql, param);
		
	}
	
	public void executeSQL() throws SQLException  {
		ResultSet rs = null;
		try{
			
			preStatement = createStatement();
			
			SQLUtil.convert(preStatement, paramCollection);
			
			if(log.isDebugEnabled()){
				log.debug("Execute the SQL: "+sqlString+" start!");
			}

			int updateCount = preStatement.executeUpdate();
			
			updateInfo = new UpdateInfo();
			
			if(keyType == null || keyType.equals(Constanst.KEY_TYPE_AUTO)){
				rs = preStatement.getGeneratedKeys();
				
				if(!rs.next())
					return;
				
				Object keyValue = rs.getObject(1);
				updateInfo.setKey(keyValue);
			}

			updateInfo.setCount(updateCount);
			
			if(log.isDebugEnabled()){
				log.debug("Row count: "+updateCount);
				log.debug("Execute the SQL: "+sqlString+" end!");
			}
		}catch(SQLException e){
			log.error("Execute the SQL: "+sqlString+" Error!",e);
			throw e;
		}finally{
			try {
				if(rs != null)
					rs.close();
				
				if(preStatement != null)
					preStatement.close();
				
			} catch (SQLException e) {
				throw e;
			}
		}
	}
	
	public UpdateInfo getResult() {
		return updateInfo;
	}

	public void close() throws SQLException {
		preStatement.close();
	}

	
	protected PreparedStatement createStatement() throws SQLException{
		PreparedStatement preStatement = null;
		if(keyType==null || keyType.equals(Constanst.KEY_TYPE_AUTO) ){
			preStatement =  connection.prepareStatement(sqlString,Statement.RETURN_GENERATED_KEYS);
		}else{
			preStatement =  connection.prepareStatement(sqlString);
		}

		return preStatement;
	}

	public String getKeyType() {
		return keyType;
	}

	public void setKeyType(String keyType) {
		this.keyType = keyType;
	}
	
	
}

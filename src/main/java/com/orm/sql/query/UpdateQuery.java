package com.orm.sql.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.orm.sql.dom.DataInfo;
import com.orm.sql.dom.UpdateInfo;
import com.orm.sql.util.SQLUtil;

public class UpdateQuery extends AbstractQuery<UpdateInfo> {

	Log log = LogFactory.getLog(UpdateQuery.class);
	
	protected PreparedStatement preStatement;
	
	protected UpdateInfo updateInfo;
	
	public UpdateQuery(Connection con, String sql, Collection<DataInfo> param) {
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

	protected int execute() throws SQLException{
		return preStatement.executeUpdate();
	}
	
	protected PreparedStatement createStatement() throws SQLException{
		PreparedStatement preStatement =  connection.prepareStatement(sqlString);

		return preStatement;
	}
}

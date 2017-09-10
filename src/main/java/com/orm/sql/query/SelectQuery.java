package com.orm.sql.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.orm.common.xml.exception.DataNotDefineException;
import com.orm.context.data.BaseData;
import com.orm.context.data.DataUtil;
import com.orm.sql.dom.DataInfo;
import com.orm.sql.dom.UpdateInfo;
import com.orm.sql.util.SQLUtil;
import com.orm.sql.util.Util;

public class SelectQuery extends AbstractQuery<UpdateInfo>{

	Log log = LogFactory.getLog(SelectQuery.class);
	
	protected ResultSet rs;
	
	public SelectQuery(Connection con, String sql, Collection<DataInfo> param) {
		super(con, sql, param);
	}

	public UpdateInfo getResult() {
		return null;
	}

	protected PreparedStatement createStatement() throws SQLException{
		PreparedStatement preStatement =  connection.prepareStatement(sqlString);
		//PreparedStatement preStatement =  connection.prepareStatement(sqlString,
		//		ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
		//preStatement.setFetchSize(60);
		return preStatement;
	}
	
	public void loadData(Collection<BaseData> dataCollection,String name) throws SQLException{
		try{
			while(rs.next()){
				BaseData data = DataUtil.createBaseData(name);
				Util.convertRsToBaseData(rs, data);
				dataCollection.add(data);
			}
		}catch(DataNotDefineException e){
			log.error("The data: "+name+" is not exist!",e);
		}

	}
	
	public void loadData(Collection<Map<String,Object>> dataCollection) throws SQLException{
		while(rs.next()){
			Map<String,Object> data = new HashMap<String,Object>(15);
			Util.convertRsToMap(rs, data);
			dataCollection.add(data);
		}
	}

	public void executeSQL() throws SQLException {
		try{
			preStatement = createStatement();
			
			SQLUtil.convert(preStatement, paramCollection);
			
			if(log.isDebugEnabled()){
				log.debug("Execute the SQL: "+sqlString+" start!");
			}
			
			rs = preStatement.executeQuery();
			
			if(log.isDebugEnabled()){
				log.debug("Execute the SQL: "+sqlString+" end!");
			}
		}catch(SQLException e){
			log.error("Execute the SQL: "+sqlString+" error!",e);
			if(rs != null)
				rs.close();
			if(preStatement != null){
				preStatement.close();
			}
			throw e;
		}
		
	}
	
	public void close() throws SQLException{
		try{
			rs.close();
			preStatement.close();
		}catch(SQLException e){
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

	public ResultSet getResultSet() {
		return rs;
	}
}

package com.orm.sql.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.orm.sql.dom.DataInfo;
import com.orm.sql.dom.SQLDataInfo;
import com.orm.sql.util.SQLUtil;

public class BatchQuery extends AbstractQuery<int[]> {

	protected List<SQLDataInfo> sqlDataList;
	
	protected PreparedStatement preStatement;
	
	private int updateCount[];
	
	public BatchQuery(Connection con) {
		super(con);
		sqlDataList = new ArrayList<SQLDataInfo>(30);
	}

	public void executeSQL() throws SQLException {
		for(int i = 0 ;i < sqlDataList.size(); i++){
			SQLDataInfo sqlDataInfo = sqlDataList.get(i);
			this.addBatch(sqlDataInfo);
		}
		
		updateCount = preStatement.executeBatch();
	}

	public int[] getResult() {
		return updateCount;
	}

	public void cancel() throws SQLException {
		if(preStatement != null){
			preStatement.clearBatch();
			preStatement.clearParameters();
			preStatement.cancel();
		}
	}
	
	public void close() throws SQLException {
		if(preStatement != null)
			preStatement.close();
	}

	public void addBatch(String sql){
		addBatch(sql,null);
	}
	
	public void addBatch(String sql,List<DataInfo> param){
		
		SQLDataInfo sqlDataInfo = new SQLDataInfo();
		
		sqlDataInfo.setSql(sql);
		
		sqlDataInfo.setDataInfoList(param);
		
		sqlDataList.add(sqlDataInfo);
	}
	
	protected void addBatch(SQLDataInfo sqlDataInfo) throws SQLException{
		
		preStatement.addBatch(sqlDataInfo.getSql());
		
		List<DataInfo> dataInfoList = sqlDataInfo.getDataInfoList();
		
		if(dataInfoList != null 
				&& !dataInfoList.isEmpty()){
			
			SQLUtil.convert(preStatement, dataInfoList);
		}
		preStatement.addBatch();
	}

	@Override
	protected PreparedStatement createStatement() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	
}

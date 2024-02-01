package dec.external.datasource.sql.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import dec.core.datasource.dom.DataInfo;
import dec.external.datasource.sql.dom.SQLDataInfo;
import dec.external.datasource.sql.utils.SQLUtil;
import javolution.util.FastTable;

public class BatchQuery extends AbstractQuery<int[]> {

	protected List<SQLDataInfo> sqlDataList;
	
	protected PreparedStatement preStatement;
	
	private int updateCount[];
	
	public BatchQuery(Connection con) {
		super(con);
		sqlDataList = new FastTable<SQLDataInfo>();
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
			
			SQLUtil.convert(preStatement, dataInfoList, false);
		}
		preStatement.addBatch();
	}

	@Override
	protected PreparedStatement createStatement() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	
}

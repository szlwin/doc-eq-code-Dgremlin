package com.orm.sql.connection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Map;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.connection.exception.ConectionException;
import com.orm.connection.factory.ConnectionFactory;
import com.orm.context.data.DataUtil;
import com.orm.sql.dom.ConvertInfo;
import com.orm.sql.dom.ConvertParam;
import com.orm.sql.dom.ExecuteInfo;
import com.orm.sql.dom.ExecuteParam;
import com.orm.sql.dom.QueryInfo;

public abstract class AbstractCmdConnection extends AbstractConnection<ExecuteParam,ExecuteInfo> implements SqlDBConnection<ExecuteParam,ExecuteInfo>{
	
	public void connect() throws ConectionException {
		try {
			con = ConnectionFactory.getInstance().getConnection(conName);
			this.setAutoCommit(false);
		} catch (Exception e) {
			throw new ConectionException(e);
		} 
	}
	
	public void commit() throws ConectionException {
		try{
			if(check())
				((Connection) con).commit();
		}catch(SQLException e){
			throw new ConectionException(e);
		}finally{
			if(con != null){
				try {
					((Connection) con).close();
				} catch (SQLException e) {

				}
			}
		}
	}

	public void close() throws ConectionException {
		try {
			if(check())
				((Connection) con).close();
		} catch (SQLException e) {
			throw new ConectionException(e);
		}
	}

	public void rollback() throws ConectionException {
		try {
			if(check())
				((Connection) con).rollback();
		} catch (SQLException e) {
			throw new ConectionException(e);
		}
	}
	
	public Connection getConnection(){
		return (Connection) con;
	}
	
	private boolean check() throws SQLException{
		return con != null  && !((Connection) con).isClosed();
	}
	
	public void setAutoCommit(boolean isAuto) throws ConectionException{
		try {
			if(check())
				((Connection) con).setAutoCommit(isAuto);
		} catch (SQLException e) {
			throw new ConectionException(e);
		}
	}

	public Savepoint getSavepoint(String name) throws SQLException{
		return ((Connection) con).setSavepoint(name);
	}
	
	public Savepoint getSavepoint() throws SQLException{
		return ((Connection) con).setSavepoint();
	}
	
	public void rollback(Savepoint savepoint) throws SQLException{
		if(check())
			((Connection) con).rollback(savepoint);
	}
	
	public void releaseSavepoint(Savepoint savepoint)throws SQLException{
		if(check())
			((Connection) con).releaseSavepoint(savepoint);
	}
	
	public boolean isClosed() throws ConectionException{
		
		try {
			return ((Connection) con).isClosed();
		} catch (SQLException e) {
			throw new ConectionException(e);
		}
	}

	public ExecuteInfo execute(ExecuteParam param) throws ExecuteException {
		Map<String,String> proMap = DataUtil.getConfigInfo().getConnection(conName).getPropertyInfo();
		
		ConvertParam convertParam = new ConvertParam();
		convertParam.setData(param.getValue());
		convertParam.setSql(param.getCmd());
		convertParam.setType(param.getType());
		convertParam.setOrign(param.getIsOrign());
		convertParam.setDataSource(dataSource);
		convertParam.setConParamMap(proMap);
		
		ConvertInfo convertInfo = (ConvertInfo) sqlConvertContainer.convert(convertParam);
		
		QueryInfo queryInfo = new QueryInfo();
		queryInfo.setSql(convertInfo.getCmd());
		queryInfo.setType(param.getType());
		queryInfo.setDataInfoCollection(convertInfo.getDataInfo());
		queryInfo.setCon(con);
		queryInfo.setKeyType(convertInfo.getKeyType());
		queryInfo.setKeyValue(convertInfo.getKeyValue());
		
		
		queryInfo.setConParamMap(proMap);
		
		ExecuteInfo executeInfo = (ExecuteInfo) sqlExecuteContainer.execute(queryInfo);
		
		return executeInfo ;
		
	}
	
	//protected boolean isNeedConvert() {
	//	return false;
	//}
}

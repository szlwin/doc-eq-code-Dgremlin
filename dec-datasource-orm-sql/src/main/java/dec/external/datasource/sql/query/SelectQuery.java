package dec.external.datasource.sql.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dec.core.context.config.exception.DataNotDefineException;
import dec.core.context.config.utils.DataUtil;
import dec.core.context.data.BaseData;
import dec.core.datasource.dom.DataInfo;
import dec.external.datasource.sql.dom.UpdateInfo;
import dec.external.datasource.sql.utils.SQLUtil;
import dec.external.datasource.sql.utils.Util;
import javolution.util.FastMap;

//import com.orm.common.xml.exception.DataNotDefineException;
//import com.orm.context.data.BaseData;
//import com.orm.context.data.DataUtil;
//import com.orm.sql.dom.DataInfo;
//import com.orm.sql.dom.UpdateInfo;
//import com.orm.sql.util.SQLUtil;
//import com.orm.sql.util.Util;

public class SelectQuery extends AbstractQuery<UpdateInfo>{

	private final static Logger log = LoggerFactory.getLogger(SelectQuery.class);
	
	protected ResultSet rs;
	
	public SelectQuery(Connection con, String sql, Collection<DataInfo> param) {
		super(con, sql, param);
	}

	public UpdateInfo getResult() {
		return null;
	}

	protected PreparedStatement createStatement() throws SQLException{
		PreparedStatement preStatement =  connection.prepareStatement(sqlString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				//pprepareStatement(sqlString,ResultSet.CONCUR_READ_ONLY ,ResultSet.CONCUR_READ_ONLY);
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
			log.error("The data: {} is not exist!", name, e);
		}

	}
	
	public void loadData(Collection<Map<String,Object>> dataCollection) throws SQLException{
		while(rs.next()){
			Map<String,Object> data = new FastMap<String,Object>();
			SQLUtil.convertRsToMap(rs, data);
			dataCollection.add(data);
		}
	}

	public void executeSQL() throws SQLException {
		try{
			preStatement = createStatement();
			
			String paramString = SQLUtil.convert(preStatement, paramCollection, log.isDebugEnabled());
			
			if(log.isDebugEnabled()){
				log.debug("Execute the SQL: {}", sqlString);
				
				if(paramString != null){
					log.debug("SQL Paramer==> {}", paramString);
				}
			}
			
			rs = preStatement.executeQuery();
			
		}catch(SQLException e){
			log.error("Execute the SQL eror, sql: {}", sqlString,e);
			
			/*if(preStatement != null){
				try{
					preStatement.close();
				}catch(SQLException ex){
					ex.printStackTrace();
				}
				
			}
			
			if(rs != null){
				try{
					rs.close();
				}catch(SQLException ex){
					ex.printStackTrace();
				}
			}*/
			
			throw e;
		}
		
	}
	
	public void close() throws SQLException{
		
		try {
			if(rs != null)
				rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try {
			
			if(preStatement != null)
				preStatement.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		/*try{
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
		}*/
	}

	public ResultSet getResultSet() {
		return rs;
	}
}
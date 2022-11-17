package dec.external.datasource.sql.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dec.core.context.config.model.config.ConfigConstanst;
import dec.core.datasource.dom.DataInfo;
import dec.external.datasource.sql.dom.UpdateInfo;
import dec.external.datasource.sql.utils.SQLUtil;

//import com.orm.common.xml.util.Constanst;
//import com.orm.sql.dom.DataInfo;
//import com.orm.sql.dom.UpdateInfo;
//import com.orm.sql.util.SQLUtil;

public class InsertQuery extends AbstractQuery<UpdateInfo> {

	private final static Logger log = LoggerFactory.getLogger(InsertQuery.class);
	
	protected UpdateInfo updateInfo;
	
	private String keyType;
	
	public InsertQuery(Connection con, String sql, Collection<DataInfo> param) {
		super(con, sql, param);
		
	}
	
	public void executeSQL() throws SQLException  {
		ResultSet rs = null;
		try{
			
			preStatement = createStatement();
			
			String paramString = SQLUtil.convert(preStatement, paramCollection,log.isDebugEnabled());
			
			if(log.isDebugEnabled()){
				
				log.debug("Execute the SQL: {}",sqlString);
				
				if(paramString != null){
					log.debug("SQL Paramer==> {}", paramString);
				}
			}

			int updateCount = preStatement.executeUpdate();
			
			updateInfo = new UpdateInfo();
			
			if(keyType == null || keyType.equals(ConfigConstanst.KEY_TYPE_AUTO)){
				rs = preStatement.getGeneratedKeys();
				
				if(!rs.next())
					return;
				
				Object keyValue = rs.getObject(1);
				updateInfo.setKey(keyValue);
			}

			updateInfo.setCount(updateCount);
			
			//if(log.isDebugEnabled()){
			//	log.debug("Row count: "+updateCount);
				//log.debug("Execute the SQL: "+sqlString+" end!");
			//}
		}catch(SQLException e){
			log.error("Execute the SQL: "+sqlString+" Error!",e);
			throw e;
		}finally{
			try {
				if(rs != null)
					rs.close();
				
			} catch (SQLException e) {
				e.printStackTrace();;
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
		if(keyType==null || keyType.equals(ConfigConstanst.KEY_TYPE_AUTO) ){
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

package com.orm.sql.execute;

import java.sql.SQLException;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.common.xml.util.Constanst;
import com.orm.sql.dom.UpdateInfo;
import com.orm.sql.query.InsertQuery;

public class InsertExecute extends AbstractSQLExecute{

	public void execute() throws ExecuteException {
		//String keyType = null;
		
		InsertQuery query = new InsertQuery(con,this.cmd,this.dataInfoCollection);
		
		query.setKeyType(keyType);
		
		try {
			query.executeSQL();
		} catch (SQLException e) {
			throw new ExecuteException(e);
		}finally{
			try {
				query.close();
			} catch (SQLException e) {
			}
		}
		
		UpdateInfo updateIfno = query.getResult();
		
		if(keyType == null || keyType.equals(Constanst.KEY_TYPE_AUTO)){
			this.executeInfo.setKeyValue(updateIfno.getKey());
		}else{
			this.executeInfo.setKeyValue(this.keyValue);
		}
		
	}

}

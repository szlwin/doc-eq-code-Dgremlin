package com.orm.sql.execute;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.sql.query.SelectQuery;

public class GetExecute extends AbstractSQLExecute{

	public void execute() throws ExecuteException {
		SelectQuery query = new SelectQuery(con,this.cmd,this.dataInfoCollection);
		
		Collection dataCollection = new ArrayList();
	
		try {
			query.executeSQL();
			query.loadData(dataCollection);
		} catch (SQLException e) {
			throw new ExecuteException(e);
		}finally{
			try {
				query.close();
			} catch (SQLException e) {
			}
		}
		if(dataCollection.size()>1){
			throw new ExecuteException("This get the more than one data.");
		}
		this.executeInfo.setDataCollection(dataCollection);
		
	}

}

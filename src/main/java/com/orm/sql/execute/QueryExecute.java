package com.orm.sql.execute;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.sql.query.SelectQuery;

public class QueryExecute extends AbstractSQLExecute{
	Log log = LogFactory.getLog(QueryExecute.class);
	
	public void execute() throws ExecuteException {
		if(log.isDebugEnabled()){
			log.debug("Query data by the SQL: "+this.cmd+" start!");
		}
		
		if(log.isDebugEnabled()){
			log.debug("Convert the SQL result: "+this.cmd);
		}

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
		
		this.executeInfo.setDataCollection(dataCollection);
		if(log.isDebugEnabled()){
			log.debug("Query data end!");
		}
		
		
		
	}

}

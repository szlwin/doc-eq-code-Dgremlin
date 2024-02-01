package dec.external.datasource.sql.execute;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dec.core.datasource.execute.exception.ExecuteException;
import dec.external.datasource.sql.collections.list.SimpleList;
import dec.external.datasource.sql.query.SelectQuery;
import javolution.util.FastTable;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

//import com.orm.common.execute.exception.ExecuteException;
//import com.orm.sql.query.SelectQuery;

public class QueryExecute extends AbstractSQLExecute{
	private final static Logger log = LoggerFactory.getLogger(QueryExecute.class);
	
	public void execute() throws ExecuteException {
		//if(log.isDebugEnabled()){
		//	log.debug("Query data by the SQL: "+this.cmd+" start!");
		//}
		
		//if(log.isDebugEnabled()){
		//	log.debug("Convert the SQL result: "+this.cmd);
		//}

		SelectQuery query = new SelectQuery(con,this.cmd,this.dataInfoCollection);
		
		Collection<Map<String, Object>> dataCollection = new FastTable<Map<String, Object>>();
		
		try {
			query.executeSQL();
			query.loadData(dataCollection);
			
			if(log.isDebugEnabled()){
				log.debug("Total: "+dataCollection.size());
			}
		} catch (SQLException e) {
			throw new ExecuteException(e);
		}finally{
			try {
				query.close();
			} catch (SQLException e) {
				log.error("Close connection error", e);
			}
		}
		
		this.executeInfo.setDataCollection(dataCollection);
	}

}

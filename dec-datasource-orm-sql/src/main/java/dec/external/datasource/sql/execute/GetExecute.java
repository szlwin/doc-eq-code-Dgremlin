package dec.external.datasource.sql.execute;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dec.core.datasource.execute.exception.ExecuteException;
import dec.external.datasource.sql.query.SelectQuery;
import javolution.util.FastTable;

public class GetExecute extends AbstractSQLExecute{

	private final static Logger log = LoggerFactory.getLogger(GetExecute.class);
	
	public void execute() throws ExecuteException {
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
		if(dataCollection.size()>1){
			throw new ExecuteException("This get the more than one data.");
		}
		this.executeInfo.setDataCollection(dataCollection);
		
	}

}

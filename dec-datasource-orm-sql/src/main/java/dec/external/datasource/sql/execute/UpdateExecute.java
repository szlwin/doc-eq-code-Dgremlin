package dec.external.datasource.sql.execute;

import java.sql.SQLException;

import dec.core.datasource.execute.exception.ExecuteException;
import dec.external.datasource.sql.query.UpdateQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.orm.common.execute.exception.ExecuteException;
//import com.orm.sql.query.UpdateQuery;

public class UpdateExecute extends AbstractSQLExecute{

	private final static Logger log = LoggerFactory.getLogger(QueryExecute.class);

	public void execute() throws ExecuteException {
		UpdateQuery query = new UpdateQuery(con,cmd,this.dataInfoCollection);
		
		try {
			query.executeSQL();
		} catch (SQLException e) {
			throw new ExecuteException(e);
		}finally{
			try {
				query.close();
			} catch (SQLException e) {
				log.error("Close connection error", e);
			}
		}
	}

}

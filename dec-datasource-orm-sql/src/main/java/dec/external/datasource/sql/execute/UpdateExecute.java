package dec.external.datasource.sql.execute;

import java.sql.SQLException;

import dec.core.datasource.execute.exception.ExecuteException;
import dec.external.datasource.sql.query.UpdateQuery;

//import com.orm.common.execute.exception.ExecuteException;
//import com.orm.sql.query.UpdateQuery;

public class UpdateExecute extends AbstractSQLExecute{

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
				e.printStackTrace();
			}
		}
	}

}

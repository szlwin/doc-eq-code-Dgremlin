package dec.external.datasource.sql.query;

import java.sql.SQLException;

public interface Query<E> {

	void executeSQL() throws SQLException;
	
	E getResult();
	
	void close() throws SQLException;
}

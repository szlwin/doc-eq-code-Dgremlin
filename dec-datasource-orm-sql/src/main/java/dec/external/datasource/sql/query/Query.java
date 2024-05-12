package dec.external.datasource.sql.query;

import java.sql.SQLException;

public interface Query<E> {

	void executeSQL() throws SQLException;
	
	public E getResult();
	
	public void close() throws SQLException;
}

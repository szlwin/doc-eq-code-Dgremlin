package dec.external.datasource.sql.query;

import java.sql.SQLException;

public interface Query<E> {

	public void executeSQL() throws SQLException;
	
	public E getResult();
	
	public void close() throws SQLException;
}

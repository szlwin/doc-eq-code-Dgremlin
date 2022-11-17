package dec.core.datasource.connection.factory;

import dec.core.datasource.connection.DataConnection;

public interface DBConectionFacory<K,V> {

	public DataConnection<K,V> getDataConnection();
}

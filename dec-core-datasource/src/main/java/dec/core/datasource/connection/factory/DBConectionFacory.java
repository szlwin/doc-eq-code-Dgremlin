package dec.core.datasource.connection.factory;

import dec.core.datasource.connection.DataConnection;

public interface DBConectionFacory<K,V> {

	DataConnection<K,V> getDataConnection();
}

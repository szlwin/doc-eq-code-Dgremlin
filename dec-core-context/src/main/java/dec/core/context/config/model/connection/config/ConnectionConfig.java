package dec.core.context.config.model.connection.config;

import java.util.Map;

import dec.core.context.config.model.connection.Connection;
import javolution.util.FastMap;

public class ConnectionConfig extends AbstractConfig {

	
	private static final ConnectionConfig connectionManager =  new ConnectionConfig();
	
	private Map<String,Connection> connecionMap = new FastMap<String,Connection>();
	
	private String defaultName;
	
	private ConnectionConfig()
	{
		
	}

	public static ConnectionConfig getInstance()
	{
		return connectionManager;
	}
	
	public String getDefaultName() {
		return defaultName;
	}

	public void setDefaultName(String defaultName) {
		this.defaultName = defaultName;
	}

	public void add(Connection connection)
	{
		connecionMap.put(connection.getName(),connection);
	}
	
	public Connection get(String name)
	{
		if(name == null){
			return getConnection();
		}
		return connecionMap.get(name);
	}
	
	public Connection getConnection()
	{
		return connecionMap.get(defaultName);
	}

	public <E> void add(E connection) {
		add((Connection)connection);
	}
	
}

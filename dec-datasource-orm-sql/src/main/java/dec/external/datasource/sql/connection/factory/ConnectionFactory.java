package dec.external.datasource.sql.connection.factory;

import java.sql.SQLException;

import dec.core.context.config.model.datasource.DataSource;
import dec.core.context.config.utils.ConfigContextUtil;

import dec.core.datasource.connection.exception.ConectionException;


public class ConnectionFactory{

	private static final ConnectionFactory connectionFactory 
		= new ConnectionFactory();
	
	public static ConnectionFactory getInstance()
	{
		return connectionFactory;
	}
	
	public Object getConnection() throws ClassNotFoundException, SQLException, ConectionException {
		String conName = ConfigContextUtil.getDefaultCon();
		return getConnection(conName);
	}

	public Object getConnection(String conName) throws ClassNotFoundException, SQLException, ConectionException {
		
		if(conName == null){
			conName = ConfigContextUtil.getDefaultCon();
		}
		
		DataSource<?> dataSourceInfo = ConfigContextUtil.getDataSourceByCon(conName);
		if(dataSourceInfo == null){
			throw new ConectionException("The con:"+conName+" is error!");
		}
		
		dec.core.datasource.connection.datasource.DataSource<?,?> ds = (dec.core.datasource.connection.datasource.DataSource<?,?>) dataSourceInfo.getDataSource();
		if(ds != null){
			//DataConnection con =  (Connection) ds.getConnection();
			//con.setAutoCommit(false);
			
			return ds.getConnection();
		}
		
		//SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		//System.out.println("get con start:"+format.format(new Date()));
		
		//Class.forName(dataSourceInfo.getDriverClass());
		//Connection con = DriverManager.getConnection(dataSourceInfo.getUrl(), dataSourceInfo.getUserName(), dataSourceInfo.getPassWord());
		//con.setAutoCommit(false);
		//con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		
		/*MysqlConnectionPoolDataSource mysqlDataSource = new MysqlConnectionPoolDataSource();
		mysqlDataSource.setUrl("jdbc:mysql://localhost/orm-test?maxActive=10&amp;useUnicode=true&amp;characterEncoding=utf8");
		mysqlDataSource.setUser("root");
		mysqlDataSource.setPassword("1234");
		Connection con = mysqlDataSource.getConnection();*/
		//System.out.println("get con end:"+format.format(new Date()));
		return null;
	}
}

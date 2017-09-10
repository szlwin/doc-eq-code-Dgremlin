package com.orm.connection.factory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.orm.common.xml.model.config.datasource.DataSource;
import com.orm.connection.exception.ConectionException;
import com.orm.sql.util.Util;

public class ConnectionFactory implements ORMConnectionFactory{

	private static final ConnectionFactory connectionFactory 
		= new ConnectionFactory();
	
	public static ConnectionFactory getInstance()
	{
		return connectionFactory;
	}
	
	public Object getConnection() throws ClassNotFoundException, SQLException, ConectionException {
		String conName = Util.getDefaultCon();
		return getConnection(conName);
	}

	public Object getConnection(String conName) throws ClassNotFoundException, SQLException, ConectionException {
		
		if(conName == null){
			conName = Util.getDefaultCon();
		}
		
		DataSource dataSourceInfo = Util.getDataSourceByCon(conName);
		if(dataSourceInfo == null){
			throw new ConectionException("The con:"+conName+" is error!");
		}
		
		com.orm.connection.datasource.DataSource ds = dataSourceInfo.getDataSource();
		if(ds != null){
			//DataConnection con =  (Connection) ds.getConnection();
			//con.setAutoCommit(false);
			
			return ds.getConnection();
		}
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		//System.out.println("get con start:"+format.format(new Date()));
		
		Class.forName(dataSourceInfo.getDriverClass());
		Connection con = DriverManager.getConnection(dataSourceInfo.getUrl(), dataSourceInfo.getUserName(), dataSourceInfo.getPassWord());
		con.setAutoCommit(false);
		//con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		
		/*MysqlConnectionPoolDataSource mysqlDataSource = new MysqlConnectionPoolDataSource();
		mysqlDataSource.setUrl("jdbc:mysql://localhost/orm-test?maxActive=10&amp;useUnicode=true&amp;characterEncoding=utf8");
		mysqlDataSource.setUser("root");
		mysqlDataSource.setPassword("1234");
		Connection con = mysqlDataSource.getConnection();*/
		//System.out.println("get con end:"+format.format(new Date()));
		return con;
	}
}

package test.common;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.orm.context.data.BaseData;
import com.orm.context.data.DataUtil;
import com.orm.context.data.ModelData;
import com.orm.model.container.ModelLoader;
import com.orm.model.container.manager.ContainerManager;
import com.orm.model.execute.tran.TransactionContainer;


public class TestXA {

	/**
	 * @param args
	 * void
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		MysqlConnectionPoolDataSource mysqlDataSource = new MysqlConnectionPoolDataSource();
		mysqlDataSource.setUrl("jdbc:mysql://localhost/orm-test?maxActive=10&amp;useUnicode=true&amp;characterEncoding=utf8");
		mysqlDataSource.setUser("root");
		mysqlDataSource.setPassword("1234");
		Connection con = mysqlDataSource.getConnection();
		con.setAutoCommit(false);
		PreparedStatement statement = mysqlDataSource.getConnection().prepareStatement("insert into orderinfo (o_id,o_userId) values (5,2)");
		ConThread conThread1 = new ConThread(con,false,null);
		ConThread conThread2 = new ConThread(con,true,null);
		conThread1.start();
		conThread1.join();
		conThread2.start();
		conThread2.join();
		//con.commit();
		//testXA1();
		//testXA1();
	}
	
	public static void testXA1(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Connection con = null;
		
		//con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		MysqlConnectionPoolDataSource mysqlDataSource = new MysqlConnectionPoolDataSource();
		mysqlDataSource.setUrl("jdbc:mysql://127.0.0.1:3306/orm-test?maxActive=10&useUnicode=true&amp;characterEncoding=utf8");
		mysqlDataSource.setUser("root");
		mysqlDataSource.setPassword("1234");

		//mysqlDataSource.setRelaxAutoCommit(property)
		Statement statement;
		try {
			con = mysqlDataSource.getConnection();
			con.setAutoCommit(false);
			con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = con.createStatement();
			statement.executeUpdate("insert into orderinfo (o_id,o_userId) values (5,2)");
			//con.commit();
			

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		Connection con1 = null;
		
		//con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		//MysqlConnectionPoolDataSource mysqlDataSource1 = new MysqlConnectionPoolDataSource();
		//mysqlDataSource1.setUrl("jdbc:mysql://localhost/orm-test?useUnicode=true&amp;characterEncoding=utf8");
		//mysqlDataSource1.setUser("root");
		//mysqlDataSource1.setPassword("1234");
		Statement statement1;
		try {
			con1 = mysqlDataSource.getConnection();
			con1.setAutoCommit(false);
			con1.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement1 = con1.createStatement();
			statement1.executeUpdate("insert into orderinfo (o_id,o_userId) values (5,2)");
			//con.commit();
			

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void testXA(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Connection con = null;
		
		//con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		
		Statement statement;
		try {
			con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/orm-test?useUnicode=true&amp;characterEncoding=utf8","root", "1234");
			con.setAutoCommit(false);
			statement = con.createStatement();
			statement.executeUpdate("insert into orderinfo (o_id,o_userId) values (5,2)");
			//con.commit();
			

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Connection con1 =null; 
		Statement statement1;
		try {
			con1 = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/orm-test?useUnicode=true&amp;characterEncoding=utf8","root", "1234");
			statement1 = con1.createStatement();
			statement1.executeUpdate("insert into orderinfo (o_id,o_userId) values (5,2)");
			//statement.close();
			//con.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

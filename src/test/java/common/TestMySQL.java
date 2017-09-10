package test.common;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;


public class TestMySQL {

	/**
	 * @param args
	 * void
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException {
		
		testThread();
	}
	
	public static void testNoThread() throws SQLException{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		System.out.println(format.format(new Date()));
		
		MysqlConnectionPoolDataSource mysqlDataSource = new MysqlConnectionPoolDataSource();
		mysqlDataSource.setUrl("jdbc:mysql://localhost/orm-test?maxActive=10&amp;useUnicode=true&amp;characterEncoding=utf8");
		mysqlDataSource.setUser("root");
		mysqlDataSource.setPassword("1234");
		Connection con = mysqlDataSource.getConnection();
		con.setAutoCommit(false);
		
		for(int i = 0 ; i < 100;i++){
			Statement sta = con.createStatement();
			sta.executeUpdate("insert into userInfo (u_name,u_password) values ('test','test')");
			sta.close();
		}
		con.commit();
		con.close();
		
		System.out.println(format.format(new Date()));
	}
	
	public static void testThread() throws SQLException{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		System.out.println(format.format(new Date()));
		
		MysqlConnectionPoolDataSource mysqlDataSource = new MysqlConnectionPoolDataSource();
		mysqlDataSource.setUrl("jdbc:mysql://localhost/orm-test?maxActive=10&amp;useUnicode=true&amp;characterEncoding=utf8");
		mysqlDataSource.setUser("root");
		mysqlDataSource.setPassword("1234");
		final Connection con = mysqlDataSource.getConnection();
		con.setAutoCommit(false);
		
		for(int k = 0;k <5; k++){
			Thread t = new Thread(){
				public void run(){
					for(int i = 0 ; i < 20;i++){
						Statement sta = null;
						try {
							sta = con.createStatement();
							sta.executeUpdate("insert into userInfo (u_name,u_password) values ('test','test')");
							sta.close();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}finally{
							finish(con);
							if(sta!=null)
								try {
									sta.close();
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
						}

					}
				}
			};
			t.start();
		}


	}

	private static int count =0;
	
	private static Object lock = new Object();
	private static void finish(Connection con){
		synchronized(lock){
			count++;
			if(count != 100){
				return;
			}
		}

		try {
			con.commit();
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		System.out.println(format.format(new Date()));
	}
}

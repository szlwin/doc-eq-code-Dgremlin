package test.common;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class ConThread extends Thread{
	//insert into orderinfo (o_userId) values (2)
	private Connection con;
	private boolean isCommit;
	private Statement statement;
	public ConThread(Connection connection,boolean isCommit,Statement statement) {
		this.con = connection;
		this.isCommit = isCommit;
		this.statement = statement;
	}

	public void run(){

		//mysqlDataSource.setRelaxAutoCommit(property)
		//Statement statement;
		try {
			//con.setAutoCommit(false);
			con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			if(statement == null)
				statement = con.prepareStatement("update userInfo set u_name ='t1' where u_id = 140");
			((PreparedStatement)statement).executeUpdate();
			statement.close();
			if(isCommit)
				con.commit();
			

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			
		}
	}
}

package test.common;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class TestBatch {

	/**
	 * @param args
	 * void
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		test();
	}
	
	public static void test() throws SQLException, ClassNotFoundException{
		Class.forName("com.mysql.jdbc.Driver");
		Connection con1 = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/orm-test?useUnicode=true&amp;characterEncoding=utf8","root", "1234");
		PreparedStatement preparedStatement = con1.prepareStatement("insert into orderinfo (o_userId) values (?)");
		preparedStatement.setObject(1, 4);
		preparedStatement.addBatch();
		
		preparedStatement.clearParameters();
		preparedStatement.setObject(1, 5);
		preparedStatement.addBatch();
		
		preparedStatement.executeBatch();
		
		preparedStatement.clearParameters();
		preparedStatement.setObject(1, 6);
		preparedStatement.addBatch();
		
		preparedStatement.executeBatch();
		preparedStatement.close();
		con1.close();
	}
}

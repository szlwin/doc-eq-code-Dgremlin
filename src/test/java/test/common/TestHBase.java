package test.common;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.common.xml.Config;
import com.orm.common.xml.exception.DataNotDefineException;
import com.orm.common.xml.exception.XMLParseException;
import com.orm.connection.exception.ConectionException;
import com.orm.context.data.DataUtil;
import com.orm.context.data.ModelData;
import com.orm.model.container.ModelContainer;
import com.orm.model.container.ModelLoader;
import com.orm.model.container.ResultInfo;
import com.orm.model.container.manager.ContainerManager;
import com.orm.model.execute.rule.exception.ExecuteRuleException;
import com.orm.session.SimpleSession;
import com.orm.session.factory.SessionFactory;


public class TestHBase {
	static Log log = LogFactory.getLog(TestHBase.class);
	/**
	 * @param args
	 * void
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		init();
		//dropTable();
		//createTable();
		//insertUTest();
		//insertUser();
		//getUser("test0004");
		//getUser("test0001");
		//getUTest();
		//insertUTTest();
		//queryUTest();
		//deleteUTest();
		count();
	}

	
	public static void init() throws XMLParseException{
		Config config = new Config("config/orm-config.xml");
		config.initContext();
	}
	
	public static void getUTest() throws Exception{
		String sql = "select u_id as uid,u_status as status,u_name as name,u_atime as activeTime  from USER_INFO where u_id = ?";
		Class.forName("com.salesforce.phoenix.jdbc.PhoenixDriver");
	    Connection conn = DriverManager.getConnection("jdbc:phoenix:hadoop.demo.bi.bestv.com.cn:2181:/hbase","admin","admin");
	    
	    PreparedStatement sta = conn.prepareStatement(sql);
	    
	    sta.setString(1, "test0004");
	    
	    ResultSet rs = sta.executeQuery(sql);
	    
	    while(rs.next()){
	    	System.out.print(rs.getObject("uid")+",");
	    	System.out.print(rs.getObject("status")+",");
	    	System.out.print(rs.getObject("name")+",");
	    	Date date = new Date();
	    	date.setTime((Long) rs.getObject("activeTime"));
	    			
	    	System.out.print(date);
	    	System.out.println("");
	    }
	    rs.close();
	    sta.close();
	    conn.close();
	}
	
	public static void queryUTest() throws Exception{
		Class.forName("com.salesforce.phoenix.jdbc.PhoenixDriver");
	    Connection conn = DriverManager.getConnection("jdbc:phoenix:hadoop.demo.bi.bestv.com.cn:2181:/hbase","admin","admin");
	    
		String sql = "select * from USER_INFO";

		Statement sta = conn.prepareStatement(sql);

	    ResultSet rs = sta.executeQuery(sql);
	    
	    while(rs.next()){
	    	System.out.print(rs.getObject("u_id")+",");
	    	System.out.print(rs.getObject("u_name")+",");
	    	System.out.print(rs.getObject("u_atime")+",");
	    	System.out.print(rs.getObject("u_status"));
	    	System.out.println("");
	    }
	    rs.close();
	    sta.close();
	    conn.close();
	}
	
	public static void insertUTTest() throws Exception{
		Class.forName("com.salesforce.phoenix.jdbc.PhoenixDriver");
	    Connection conn = DriverManager.getConnection("jdbc:phoenix:hadoop.demo.bi.bestv.com.cn:2181:/hbase","admin","admin");
	    
		String sql = "UPSERT INTO userInfo VALUES (?,?,?,?)";

	    PreparedStatement sta = conn.prepareStatement(sql);
	    sta.setString(1, "test123456789012345678901234567890123457");
	    sta.setString(2, "test123456789012345678901234567890123456");
	    sta.setDate(3, new java.sql.Date(new Date().getTime()));
	    sta.setInt(4, 1);
	    sta.execute(sql);
	    conn.commit();
	}
	
	public static void insertUTest() throws Exception{
		Class.forName("com.salesforce.phoenix.jdbc.PhoenixDriver");
	    Connection conn = DriverManager.getConnection("jdbc:phoenix:hadoop.demo.bi.bestv.com.cn:2181:/hbase","admin","admin");
	    
		String sql = "UPSERT INTO userInfo VALUES ('test123456789012345678901234567890123456','test')";

	    Statement sta = conn.createStatement();
	    sta.execute(sql);
	}
	
	public static void deleteUTest() throws Exception{
		Class.forName("com.salesforce.phoenix.jdbc.PhoenixDriver");
	    Connection conn = DriverManager.getConnection("jdbc:phoenix:hadoop.demo.bi.bestv.com.cn:2181:/hbase","admin","admin");
	    
		String sql = "delete from USER_INFO where u_id = ? ";
	    PreparedStatement sta = conn.prepareStatement(sql);
	    
	    sta.setString(1, "test0001");
	    
	    sta.execute(sql);
	    conn.commit();
	}
	
	public static void insertUser() throws XMLParseException, DataNotDefineException, ExecuteRuleException{
		ModelData user = DataUtil.createViewData("UserInfo");
		user.setValue("uid", "test0004");
		user.setValue("name","test11");
		
		Date date = new Date();
		
		date.setYear(2013);
		date.setMonth(6);
		date.setDate(25);
		date.setHours(10);;
		date.setMinutes(18);
		date.setSeconds(18);
		
		user.setValue("activeTime",date);
		user.setValue("status",1);
		
		ModelContainer container = ContainerManager.getCurrentModelContainer();
		ModelLoader loader = new ModelLoader();
		loader.load("save-User", user,"con4");
		container.load(loader);
		
		container.execute();
	}
	
	public static void getUser(String id) throws XMLParseException, DataNotDefineException, ExecuteRuleException{
		ModelData user = DataUtil.createViewData("UserInfo");
		user.setValue("uid", id);
		//user.setValue("name", "test11");
		//user.setValue("name","test123456789012345678901234567890123450");
		//user.setValue("activeTime",new java.sql.Date(new Date().getTime()));
		//user.setValue("status",1);
		
		ModelContainer container = ContainerManager.getCurrentModelContainer();
		ModelLoader loader = new ModelLoader();
		loader.load("get-User", user,"con4");
		container.load(loader);
		
		container.execute();
		
		//ResultInfo resultInfo = container.getResult();
    	/*Date date = new Date();
    	date.setTime((Long) user.getValue("activeTime"));
		
		System.out.println(date);*/
		System.out.println(user.getAllValues());
	}
	
	public static void createTable() throws ConectionException, ExecuteException{
		SimpleSession session = SessionFactory.getSimpleSession("con4");
		
		String sql = "CREATE TABLE IF NOT EXISTS USER_INFO ( " +
				"u_id VARCHAR(30) not null  PRIMARY KEY," +
				"u_name VARCHAR(30) not null," +
				"u_atime UNSIGNED_LONG," +
				"u_status UNSIGNED_INT " +
				")";

		session.begian();
		session.executeOrignUpdate(sql);
		session.commit();
		session.close();
	}
	
	public static void dropTable() throws ConectionException, ExecuteException{
		SimpleSession session = SessionFactory.getSimpleSession("con4");
		
		
		String sql = "DROP TABLE USER_INFO";

		session.begian();
		session.executeOrignUpdate(sql);
		session.commit();
	}
	
	public static void count() throws Exception{
		Class.forName("com.salesforce.phoenix.jdbc.PhoenixDriver");
	    Connection conn = DriverManager.getConnection("jdbc:phoenix:hadoop.demo.bi.bestv.com.cn:2181:/hbase","admin","admin");
	    
		String sql = "select PRODUCT_CODE as code,count(1) as cu from cbims_order group by PRODUCT_CODE ";
	    PreparedStatement sta = conn.prepareStatement(sql);
	    
	    //sta.setString(1, "test0001");
	    log.info("-----start-----");
	    ResultSet rs = sta.executeQuery(sql);
	   //while(rs.next())
	   // System.out.println(rs.getObject("code")+":"+(rs.getObject("cu")));
	    //conn.commit();
	   log.info("-----end-----");
	   
	    log.info("-----start1-----");
	    ResultSet rs1 = sta.executeQuery(sql);
	   //while(rs1.next())
	   // System.out.println(rs1.getObject("code")+":"+(rs1.getObject("cu")));
	    //conn.commit();
	   log.info("-----end1-----");
	}
}

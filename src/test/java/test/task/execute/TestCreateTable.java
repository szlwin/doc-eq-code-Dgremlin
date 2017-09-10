package test.task.execute;

import com.orm.common.execute.exception.ExecuteException;
import com.orm.common.xml.Config;
import com.orm.common.xml.exception.XMLParseException;
import com.orm.connection.exception.ConectionException;
import com.orm.session.SimpleSession;
import com.orm.session.factory.SessionFactory;

public class TestCreateTable {

	/**
	 * @param args
	 * void
	 * @throws XMLParseException 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws XMLParseException, Exception {
		init();
		//createOrderTable();
		createBillTable();
	}

	public static void init() throws XMLParseException{
		Config config = new Config("config/orm-config.xml");
		config.initContext();
	}
	
	
	public static void createOrderTable() throws ConectionException, ExecuteException{
		SimpleSession session = SessionFactory.getSimpleSession("con4");

		String sql = "CREATE TABLE IF NOT EXISTS CBIMS_ORDER(" +
				"EXTERNAL_ID     VARCHAR(128) not null  PRIMARY KEY," +
				"TRANSACTION_ID  VARCHAR(64)," +
				"USER_ID         VARCHAR(36)," +
				"PRODUCT_CODE    VARCHAR(64)," +
				"ACCOUNT_NO      VARCHAR(64)," +
				"PRODUCT_NAME    VARCHAR(128)," +
				"CHARGE_TYPE          INTEGER," +
				"PAY_TYPE             INTEGER," +
				"VALID_TIME           UNSIGNED_LONG," +
				"EXPIRE_TIME          UNSIGNED_LONG," +
				"CAN_UNSUBSCRIBE_TIME UNSIGNED_LONG," +
				"SUBSCRIBE_TIME       UNSIGNED_LONG," +
				"UNSUBSCRIBE_TIME     UNSIGNED_LONG," +
				"AUTO_SUB             INTEGER," +
				"STATUS               INTEGER," +
				"FEE                  INTEGER," +
				"UPDATE_TIME          TIMESTAMP," +
				"STB_ID               VARCHAR(128)," +
				"IP_ADDRESS           VARCHAR(64)," +
				"STATION_CODE         VARCHAR(64)," +
				"CENTER_PRODUCT_CODE  VARCHAR(128)," +
				"MAKE_BILL            INTEGER," +
				"RECORD_TIME          UNSIGNED_LONG," +
				"COLLECT_ID           INTEGER," +
				"USER_FAKED           INTEGER," +
				"CHANNEL_CODE         VARCHAR(64))";
		
		session.begian();
		session.executeOrignUpdate(sql);
		session.commit();
		session.close();
	}
	
	private static void  createBillTable() throws ConectionException, ExecuteException{
		String sql ="create table CBIMS_BILL("+
		  "ID                  VARCHAR(256) not null  PRIMARY KEY,"+
		  "BOOK_ID             UNSIGNED_INT,"+
		  "ACCOUNT_ID          UNSIGNED_INT,"+
		  "SUBSCRIBER_ID       UNSIGNED_INT,"+
		  "USER_ID             VARCHAR(64),"+
		  "ACCOUNT_SUBJECT     VARCHAR(64),"+
		  "AMOUNT              UNSIGNED_INT,"+
		  "RECORD_TIME         UNSIGNED_LONG,"+
		  "ORDER_TYPE          UNSIGNED_INT,"+
		  "ORDER_ID            UNSIGNED_INT,"+
		  "PRODUCT_CODE        VARCHAR(64),"+
		  "PRODUCT_NAME        VARCHAR(64),"+
		  "CONTENT_CODE        VARCHAR(128),"+
		  "CONTENT_NAME        VARCHAR(128),"+
		  "CENTER_PRODUCT_CODE VARCHAR(64),"+
		  "PRODUCT_PRICE       UNSIGNED_INT,"+
		  "DISCOUNT_PRICE      UNSIGNED_INT,"+
		  "OP_PRICE            UNSIGNED_INT,"+
		  "REDUCE_AMOUNT       UNSIGNED_INT,"+
		  "REDUCE_ID           UNSIGNED_INT,"+
		  "TYPE                UNSIGNED_INT,"+
		  "ORDER_CYCLE         UNSIGNED_INT,"+
		  "REAL_RECORD_TIME    UNSIGNED_LONG,"+
		  "STATION_CODE        VARCHAR(255),"+
		  "CREATE_TIME         UNSIGNED_LONG,"+
		  "CHANNEL_CODE        VARCHAR(64),"+
		  "RENEW_ORDER_DATE    VARCHAR(128))";

		SimpleSession session = SessionFactory.getSimpleSession("con4");
		
		session.begian();
		session.executeOrignUpdate(sql);
		session.commit();
		session.close();
	}
}

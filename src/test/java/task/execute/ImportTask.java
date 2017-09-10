package test.task.execute;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.orm.common.xml.Config;
import com.orm.common.xml.exception.XMLParseException;
import com.orm.context.data.BaseData;
import com.orm.context.data.DataUtil;
import com.orm.session.SimpleSession;
import com.orm.session.factory.SessionFactory;
import com.orm.sql.datatype.convert.DataTypeConvert;
import com.orm.sql.query.SelectQuery;

public class ImportTask {

	/**
	 * @param args
	 * void
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		init();
		
		//importOrder("2009-12-31","2010-1-31");
		//importData("2008-07-28","2009-12-31");
		//importData("2011-05-10","2011-12-31");
		countTest();
	}
	
	public static void importData(String startTime,String endTime) throws Exception{
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate = sdf.parse(startTime);
		
		Date endDate = sdf.parse(endTime);
		
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);
		
		
		Calendar endCal = Calendar.getInstance();
		endCal.setTime(endDate);
		
		while(startCal.compareTo(endCal) < 0){
			Date date = startCal.getTime();
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			
			String paramStartTime = dateFormat.format(date);
			
			startCal.add(Calendar.DAY_OF_YEAR, 1);
			
			String paramEndTime = dateFormat.format(startCal.getTime());
			System.out.println(paramStartTime+":"+paramEndTime);
			//importOrder(paramStartTime,paramEndTime);
			importBill(paramStartTime,paramEndTime);
		}
	}
	
	public static void init() throws XMLParseException{
		Config config = new Config("config/orm-config.xml");
		config.initContext();
		System.out.println();
	}
	
	public static void importBill(String startTime,String endTime) throws Exception{
		//DataTypeConvert.setDataSourceType("data5");
		String sql = "select * from CBIMS_BILL where RECORD_TIME >= to_date( #STARTTIME ,'yyyy-mm-dd') and RECORD_TIME < to_date( #ENDTIME ,'yyyy-mm-dd') and STATION_CODE = 'guangdong'";
		
		Map<String,Object> paramMap = new HashMap<String,Object>();
		paramMap.put("STARTTIME", startTime);
		paramMap.put("ENDTIME", endTime);
		
		SimpleSession session = SessionFactory.getSimpleSession("con3");
		session.begian();
		Collection<Map<String,Object>> col  = session.query(sql, paramMap);
		session.close();
		
		Iterator<Map<String,Object>> it = col.iterator();
		SimpleSession hbaseSession = SessionFactory.getSimpleSession("con4");
		
		int count = 0;
		
		while(it.hasNext()){
			Map<String,Object> value = it.next();
			BaseData baseData = DataUtil.createBaseData("iptyOrderBill");
			
			DataUtil.convertDataToBaseData(value, baseData);

			baseData.setValue("ID", baseData.getValue("ORDER_ID")+"_"+((oracle.sql.TIMESTAMP)baseData.getValue("RECORD_TIME")).timestampValue().getTime()+baseData.getValue("STATION_CODE"));
			Object rDate = baseData.getValue("REAL_RECORD_TIME");
			if(rDate != null){
				baseData.setValue("REAL_RECORD_TIME", new Date(((oracle.sql.TIMESTAMP)rDate).timestampValue().getTime()));
			}
			baseData.setValue("PRODUCT_NAME", "PRODUCT_CODE");
			
			baseData.setValue("RECORD_TIME", new Date(((oracle.sql.TIMESTAMP)baseData.getValue("RECORD_TIME")).timestampValue().getTime()));
			baseData.setValue("CREATE_TIME", new Date(((oracle.sql.TIMESTAMP)baseData.getValue("CREATE_TIME")).timestampValue().getTime()));
			hbaseSession.begian();
			hbaseSession.save(baseData);
			hbaseSession.commit();
			count++;
		}
		
		hbaseSession.close();
		
		System.out.println(count);
	}
	
	public static void countTest() throws Exception{
		String sql = "select count(*),PRODUCT_CODE from CBIMS_ORDER where STATION_CODE = 'guangdong' group by PRODUCT_CODE";
		
		Class.forName("com.salesforce.phoenix.jdbc.PhoenixDriver");
		Connection con = DriverManager.getConnection("jdbc:phoenix:hadoop.demo.bi.bestv.com.cn:2181:/hbase");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		System.out.println(sdf.format(new Date()));
		
		SelectQuery selectQuery = new SelectQuery(con,sql,null);
		
		selectQuery.executeSQL();
		
		List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
		selectQuery.loadData(dataList);
		System.out.println(sdf.format(new Date()));
		System.out.println(dataList);
		
	}
	
	public static void count() throws Exception{
		
		//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		
		//String paramStartTime = dateFormat.format(new Date());
		//String sql = "select count(*) from CBIMS_ORDER where STATION_CODE = 'guangdong'";
		String sql = "select count(*),PRODUCT_CODE from CBIMS_ORDER where STATION_CODE = 'guangdong' group by PRODUCT_CODE";
		SimpleSession session = SessionFactory.getSimpleSession("con3");
		session.begian();
		session.query(sql, null);
		Collection<Map<String,Object>> col  = session.query(sql, null);
		session.close();
		
		System.out.println(col);
	}
	
	public static void importOrder(String startTime,String endTime) throws Exception{
		
		String sql = "select * from CBIMS_ORDER where SUBSCRIBE_TIME >= to_date( #STARTTIME ,'yyyy-mm-dd') and SUBSCRIBE_TIME <= to_date( #ENDTIME ,'yyyy-mm-dd') and STATION_CODE = 'guangdong'";
		
		Map<String,Object> paramMap = new HashMap<String,Object>();
		paramMap.put("STARTTIME", startTime);
		paramMap.put("ENDTIME", endTime);
		
		SimpleSession session = SessionFactory.getSimpleSession("con3");
		session.begian();
		Collection<Map<String,Object>> col  = session.query(sql, paramMap);
		session.close();
		
		Iterator<Map<String,Object>> it = col.iterator();
		SimpleSession hbaseSession = SessionFactory.getSimpleSession("con4");
		
		int count = 0;
		
		while(it.hasNext()){
			Map<String,Object> value = it.next();
			BaseData baseData = DataUtil.createBaseData("iptyOrder");
			
			DataUtil.convertDataToBaseData(value, baseData);

			baseData.setValue("EXTERNAL_ID", baseData.getValue("EXTERNAL_ID")+"_"+baseData.getValue("STATION_CODE"));
			hbaseSession.begian();
			hbaseSession.save(baseData);
			hbaseSession.commit();
			count++;
		}
		
		hbaseSession.close();
		
		System.out.println(count);
	}

}

package test.task.execute;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
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
import com.orm.connection.exception.ConectionException;
import com.orm.context.data.BaseData;
import com.orm.context.data.DataUtil;
import com.orm.session.SimpleSession;
import com.orm.session.factory.SessionFactory;
import com.orm.sql.datatype.convert.DataTypeConvert;
import com.orm.sql.query.SelectQuery;

public class ImportOTTUserRecordTask {
	private static String sql = "insert into CBIMS_SUBSCRIBER_RECORD(ID,USER_ID,USER_TYPE,EPG_GROUP,STB_ID," +
			"AREA_CODE,UPDATE_TIME,TERMINAL_TYPE,PLATFORM,TERMINAL_MODEL,STATUS," +
			"CREATE_TIME,ACTIVATED_TIME,SUSPENDED_TIME,CANCELLED_TIME,CHANNEL_CODE,PARTNER_CODE,USER_GROUP,STATION_CODE) values " +
			"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	public static ColInfo[] getInfo(){
		ColInfo colInfoArr[] = new ColInfo[18];
		             
		String str = "USER_ID,USER_TYPE,EPG_GROUP,,STB_ID,,,,,,,AREA_CODE,,,,UPDATE_TIME,,TERMINAL_TYPE,PLATFORM,TERMINAL_MODEL,STATUS,CREATE_TIME,ACTIVATED_TIME,SUSPENDED_TIME,CANCELLED_TIME,CHANNEL_CODE,PARTNER_CODE,,,,,,,,,,,,,,,,,USER_GROUP,,,,,,,,";
		String strArr[] = str.split(",");
		int j = 0;
		for(int i = 0; i < strArr.length;i++){
			if(!strArr[i].equals("")){
				
				ColInfo colInfo = new ColInfo();
				colInfo.setIndex(i);
				colInfo.setName(strArr[i]);
				colInfoArr[j] = colInfo;
				j++;
				//System.out.println(strArr[i]+":"+i);
			}
			
		}
		colInfoArr[colInfoArr.length-2].setIndex(44);
		return colInfoArr;
	}
	
	public static void readfile(String filePath) throws Exception{
		
	
		
		BufferedReader file 
			= new BufferedReader(new InputStreamReader(
				new FileInputStream(filePath),"GBK"));
		file.readLine();
		//file.readLine();
		//int count = 0;
		int count = 1001;
		for(int i = 0 ; i <1000000;i++){
			String line = file.readLine();
			if(line == null){
				break;
			}
			//System.out.println(line);
			ColInfo[] colInfoArr = getInfo();
			String lineArr[] = line.split(",");
			//System.out.println(lineArr[44]);
			for(int j = 0; j < colInfoArr.length-1;j++){
				colInfoArr[j].setValue(lineArr[colInfoArr[j].getIndex()].replaceAll("\"", ""));
				String strvalue = (String) colInfoArr[j].getValue();
				
				if(strvalue == null || "".equals(strvalue)){
					colInfoArr[j].setValue(null);
				}else{
					if(colInfoArr[j].getName().equals("TERMINAL_TYPE") 
							|| colInfoArr[j].getName().equals("USER_TYPE")
							|| colInfoArr[j].getName().equals("STATUS")){
						if(!colInfoArr[j].getValue().equals("")){
							colInfoArr[j].setValue(Integer.parseInt((String) colInfoArr[j].getValue()));
						}
						
					}else if(colInfoArr[j].getName().endsWith("_TIME")) {
						String value = (String) colInfoArr[j].getValue();
						if(value != null && !"".equals(value)){
							if(value.length() < 11){
								SimpleDateFormat fromat = new SimpleDateFormat("yyyy/MM/dd");
								Date date = fromat.parse(value);
								//oracle.sql.TIMESTAMP time = new oracle.sql.TIMESTAMP();
								//time.timestampValue().setTime(date.getTime());
								colInfoArr[j].setValue(new java.sql.Timestamp(date.getTime()));
							}else{
								SimpleDateFormat fromat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
								Date date = fromat.parse(value);
								//oracle.sql.TIMESTAMP time = new oracle.sql.TIMESTAMP();
								//time.timestampValue().setTime(date.getTime());
								colInfoArr[j].setValue(new java.sql.Timestamp(date.getTime()));
							}
						}

					}
				}

				
				
				//System.out.print(colInfoArr[j].index+":"+colInfoArr[j].getValue()+",");
				
			}
			//System.out.println();
			colInfoArr[17] = new ColInfo();
			colInfoArr[17].setName("STATION_CODE");
			colInfoArr[17].setValue("ZHG_OTT_BESTV_0001");
			Statement stt = con.createStatement();
			ResultSet rs = stt.executeQuery("select CBIMS_SUBSCRIBER_RECORD_SEQ.nextval from dual");
			rs.next();
			stm.setObject(1, rs.getObject(1));
			rs.close();
			stt.close();
			//count++;
			for(int k = 2 ; k <= 19;k++){
				stm.setObject(k, colInfoArr[k-2].getValue());
			}
			stm.execute();
			//con.commit();
			//System.out.println();
		}
		con.close();

	}
	/**
	 * @param args
	 * void
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		init();
		//readfile("E:/ott用户11.csv");
		readfile("E:/OTT用户.csv");
		//init();
		
		//importOrder("2009-12-31","2010-1-31");
		//importData("2008-07-28","2009-12-31");
		//importData("2011-05-10","2011-12-31");

	}
	static PreparedStatement stm;
	static Connection con;
	public static void init() throws Exception{
		Class.forName("oracle.jdbc.driver.OracleDriver");
		con = DriverManager.getConnection("jdbc:oracle:thin:@10.50.107.71:1521:cbims", "cbims", "cbims");
		con.setAutoCommit(true);
		stm = con.prepareStatement(sql);
	}


}

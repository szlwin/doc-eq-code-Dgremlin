package test.task.execute;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

public class Test {

	/**
	 * @param args
	 * void
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ParseException, IOException {
		//put();
		get();


	}

	public static void get() throws IOException{
		Configuration conf = HBaseConfiguration.create();
		
		conf.set("hbase.zookeeper.quorum", "hadoop.demo.bi.bestv.com.cn");
		conf.set("zookeeper.znode.parent", "/hbase");
		conf.set("hbase.zookeeper.property.clientPort", "2181");
		
		HTable table = new HTable(conf, "USER_INFO");
		Get get = new Get(Bytes.toBytes("test0001"));
		
		get.addColumn(Bytes.toBytes("_0"), Bytes.toBytes("U_STATUS"));
		get.addColumn(Bytes.toBytes("_0"), Bytes.toBytes("U_ATIME"));
		Result rs = table.get(get);
		
		byte[] val = rs.getValue(Bytes.toBytes("_0"),
				Bytes.toBytes("U_STATUS"));
		
		byte[] time = rs.getValue(Bytes.toBytes("_0"),
				Bytes.toBytes("U_ATIME"));
		
		System.out.println("Value: " + Bytes.toInt(val));
		
		System.out.println("Value: " + new Date(Bytes.toLong(time)));
	}
	
	public static void delete() throws IOException{
		Configuration conf = HBaseConfiguration.create();
		
		conf.set("hbase.zookeeper.quorum", "hadoop.demo.bi.bestv.com.cn");
		conf.set("zookeeper.znode.parent", "/hbase");
		conf.set("hbase.zookeeper.property.clientPort", "2181");
		
		HTable table = new HTable(conf, "USER_INFO");
		Delete delete = new Delete(Bytes.toBytes("test0004"));
		
		table.delete(delete);
	}
	
	public static void put() throws IOException{
		Configuration conf = HBaseConfiguration.create();
		
		conf.set("hbase.zookeeper.quorum", "hadoop.demo.bi.bestv.com.cn");
		conf.set("zookeeper.znode.parent", "/hbase");
		conf.set("hbase.zookeeper.property.clientPort", "2181");
		
		HTable table = new HTable(conf, "USER_INFO");
		Put put = new Put(Bytes.toBytes("test0001"));
		
		put.add(Bytes.toBytes("_0"), Bytes.toBytes("U_STATUS"),
				Bytes.toBytes(2));
		Date date = new Date();
		
		date.setYear(2013);
		date.setMonth(6);
		date.setDate(21);
		date.setHours(10);;
		date.setMinutes(18);
		date.setSeconds(18);
		put.add(Bytes.toBytes("_0"), Bytes.toBytes("U_ATIME"),
				Bytes.toBytes(date.getTime()));
		table.put(put);
	}
	
	public static void getOrder() throws IOException{
		Configuration conf = HBaseConfiguration.create();
		
		conf.set("hbase.zookeeper.quorum", "hadoop.demo.bi.bestv.com.cn");
		conf.set("zookeeper.znode.parent", "/hbase");
		conf.set("hbase.zookeeper.property.clientPort", "2181");
		
		HTable table = new HTable(conf, "CBIMS_ORDER");
		Get get = new Get(Bytes.toBytes("02000028253201007211720312166_guangdong"));
		
		get.addColumn(Bytes.toBytes("_0"), Bytes.toBytes("STATUS"));
		
		Result rs = table.get(get);
		
		byte[] val = rs.getValue(Bytes.toBytes("_0"),
				Bytes.toBytes("STATUS"));
		System.out.println("Value: " + Bytes.toInt(val));
		System.out.println("Value: " + Bytes.toStringBinary(val));
	}
}

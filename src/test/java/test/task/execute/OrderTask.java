package test.task.execute;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.IdentityTableMapper;
import org.apache.hadoop.hbase.mapreduce.TableMapper;

import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class OrderTask {
	private static Log log = LogFactory.getLog(OrderTask.class);
	/**
	 * @param args
	 * void
	 */
	public static void main(String[] args) {
		
		String table = args[0];
		
		Scan scan = createScan(args[1],args[2]);
		
		Configuration conf = HBaseConfiguration.create();
		
		//conf.set("hbase.zookeeper.quorum", "hadoop.demo");
		//conf.set("hbase.zookeeper.property.clientPort", "2181");
		//conf.set("zookeeper.znode.parent", "/hbase");
		conf.set("mapred.output.key.class", "org.apache.hadoop.io.Text");
		System.out.println("start");
		Job job = null;
		try {
			job = new Job(conf, "Analyze data in " + table);
			job.setJarByClass(OrderTask.class);

			TableMapReduceUtil.initTableMapperJob(table, scan, AnalyzeMapper.class,
					Text.class, IntWritable.class, job);
			
			job.setReducerClass(AnalyzeReducer.class);
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(IntWritable.class);
			job.setNumReduceTasks(0);
			FileOutputFormat.setOutputPath(job, new Path("/tmp/test"));
			job.waitForCompletion(true);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	private static Scan createScan(String date,String stationCode){
		Scan scan = new Scan();
		scan.addFamily(Bytes.toBytes("_0"));
		scan.addColumn(Bytes.toBytes("_0"), Bytes.toBytes("U_NAME"));
		scan.addColumn(Bytes.toBytes("_0"), Bytes.toBytes("U_ATIME"));
		scan.addColumn(Bytes.toBytes("_0"), Bytes.toBytes("U_STATUS"));
		
		//FilterList filterList = new FilterList();
		
		//SingleColumnValueFilter staticonCodeFilter = new SingleColumnValueFilter(Bytes.toBytes("_0"),    
        //        Bytes.toBytes("STATION_CODE"),    
        //        CompareOp.EQUAL,Bytes.toBytes(stationCode));
		
		//scan.setFilter(staticonCodeFilter);
		
		
		//SingleColumnValueFilter dateFilter = new SingleColumnValueFilter(Bytes.toBytes("_0"),    
        //        Bytes.toBytes("SUBSCRIBE_TIME"),    
        //        CompareOp.EQUAL,Bytes.t.(stationCode));
		
		return scan;
	}
	
	private static class AnalyzeMapper extends TableMapper<Text, IntWritable> {
		private IntWritable ONE = new IntWritable(1);
		private JSONParser parser = new JSONParser();
		
		
		public void map(ImmutableBytesWritable row, Result columns, Context context){
			//context.getCounter(Counters.ROWS).increment(1);
			//row.
			//System.out.println("tttttttttttt");
			//fileOutputStream.(columns.list().size());
			String value = null;
			PrintStream fileOutputStream = null;
			try {
				fileOutputStream = new PrintStream("/test/t.txt");
				fileOutputStream.println(columns.list().size());
				
			} catch (Exception e1) {
				fileOutputStream.println(e1.getMessage());

			}
			
			try {
				fileOutputStream.println("KEY:"+Bytes.toStringBinary(columns.getRow()));
				fileOutputStream.println("NAME:"+Bytes.toStringBinary(columns.getValue(Bytes.toBytes("_0"), Bytes.toBytes("U_NAME"))));
				//fileOutputStream.println("STATUS:"+Bytes.toLong(columns.getValue(Bytes.toBytes("_0"), Bytes.toBytes("U_STATUS"))));
				fileOutputStream.println("STATUS:"+Bytes.toStringBinary(columns.getValue(Bytes.toBytes("_0"), Bytes.toBytes("U_STATUS"))));
				fileOutputStream.println("STATUS:"+Bytes.toInt(columns.getValue(Bytes.toBytes("_0"), Bytes.toBytes("U_STATUS"))));
				
				byte byteArray[] = columns.getValue(Bytes.toBytes("_0"), Bytes.toBytes("U_STATUS"));
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
				DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
				
				fileOutputStream.println("STATUS:"+dataInputStream.readInt());
				/*for (KeyValue kv : columns.list()) {
					//context.getCounter(Counters.COLS).increment(1);
					String key = kv.getKeyString();
					
					value = Bytes.toStringBinary(kv.getValue());
					
					fileOutputStream.println("key:"+key);
					
					fileOutputStream.println("value:"+value);
					fileOutputStream.flush();
					
					String rowValue = Bytes.toStringBinary(kv.getRow());
					
					fileOutputStream.println("rowValue:"+rowValue);
					
					///JSONObject json = (JSONObject) parser.parse(value);
					
					//Object aTime = json.get("U_ATIME");
					//fileOutputStream.println("aTime:"+aTime);
			
					//fileOutputStream.println("aTime long:"+Bytes.toLong((byte[]) aTime));

					context.write(new Text(value), ONE);
					//context.write(new Text(String.valueOf(Bytes.toLong((byte[]) aTime))), ONE);
					//context.getCounter(Counters.VALID).increment(1);
				}*/
		   } catch (Exception e) {
				fileOutputStream.println(e);
				fileOutputStream.println(e.getMessage());
				System.err.println("Row: " + Bytes.toStringBinary(row.get()) +
						", JSON: " + value);
				//context.getCounter(Counters.ERROR).increment(1);
			}finally{
				/*try {
					fileOutputStream.close();
				} catch (IOException e) {
					
				}*/
			}
		}
	}
	
	private static class AnalyzeReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		@Override
		protected void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			int count = 0;
			for (IntWritable one : values) count++;
				context.write(key, new IntWritable(count));
		}
	}
}

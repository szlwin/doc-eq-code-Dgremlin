package test.data.redis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



import redis.clients.jedis.Jedis;

public class ReadData {

	private static Log log = LogFactory.getLog(ReadData.class);
	
	private Jedis jedis;
	
	public static void main(String args[]){
		ReadData writeData = new ReadData();
		writeData.connect("10.50.107.71", 6379);
		
		log.info("-----start-----");
		/*for(int i = 0; i < 5000;i++){
			String account = createAccout(i);
			//System.out.println(account);
			writeData.readUser(account);
		}*/
		writeData.readUser("utest");
		//String account = createAccout(10000000);
		//System.out.println(account);
		//writeData.writeUser(account, account+"_name", account+"_address");
		
		log.info("-----end-----");
		writeData.close();
	}
	
	
	public static String createAccout(int i){
		String index = String.valueOf(i);
		int length = index.length();
		String account = "u";
		for(i = length;i < 8; i++){
			account = account+"0";
		}
		account = account + index;
		return account;
	}
	
	public void read(String key,String... fields){
		List<String> strList = jedis.hmget(key, fields);


		System.out.println(strList);
		
	}
	
	public void connect(String ip,int port){
		jedis = new Jedis(ip,port);
	}
	
	public void readUser(String account){
		read(account,"name","account");
	}
	
	public void close(){
		jedis.quit();
	}
}

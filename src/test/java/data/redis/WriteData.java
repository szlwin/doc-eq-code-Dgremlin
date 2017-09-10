package test.data.redis;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import redis.clients.jedis.Jedis;

public class WriteData {

	private static Log log = LogFactory.getLog(WriteData.class);
	
	private Jedis jedis;
	
	public static void main(String args[]){
		WriteData writeData = new WriteData();
		writeData.connect("10.50.107.71", 10000);
		
		log.info("-----start-----");
		for(int i = 0; i < 5000;i++){
			String account = createAccout(i);
			//System.out.println(account);
			writeData.writeUser(account, account+"_name", account+"_address");
			System.out.println(account);
		}
		
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
	
	public void write(String key,Map<String,String> map){
		String res = jedis.hmset(key, map);
		log.info(res);
	}
	
	public void connect(String ip,int port){
		jedis = new Jedis(ip,port);
	}
	
	public void writeUser(String account,String name,String address){
		Map<String,String> dataMap = new HashMap<String,String>();
		dataMap.put("account", account);
		dataMap.put("name", name);
		dataMap.put("address", address);
		write(account,dataMap);
	}
	
	public void close(){
		jedis.quit();
	}
}

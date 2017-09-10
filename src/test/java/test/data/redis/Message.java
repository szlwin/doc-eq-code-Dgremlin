package test.data.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class Message {

	private Jedis jedis;
	
	private JedisPubSub jedisPubSub;
	
	public static void main(String args[]){
		Message message = new Message();
		message.connect("10.50.107.71", 6379);
		message.subscribe("mychannle");
	}
	public void subscribe(String... channels){

		jedis.subscribe(jedisPubSub, channels);
		
	}
	
	public void connect(String ip,int port){
		jedis = new Jedis(ip,port);
		jedisPubSub =  new  DefaultPubSub();
	}
}

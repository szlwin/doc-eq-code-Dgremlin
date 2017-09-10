package test.data.redis;

import redis.clients.jedis.JedisPubSub;

public class DefaultPubSub extends JedisPubSub{

	@Override
	public void onMessage(String arg0, String arg1) {
		System.out.println("onMessage");
		System.out.println(arg0);
		System.out.println(arg1);
	}

	@Override
	public void onPMessage(String arg0, String arg1, String arg2) {
		System.out.println(arg0);
		System.out.println(arg1);
		System.out.println(arg2);
	}

	@Override
	public void onPSubscribe(String arg0, int arg1) {
		System.out.println(arg0);
		System.out.println(arg1);
		
	}

	@Override
	public void onPUnsubscribe(String arg0, int arg1) {
		System.out.println(arg0);
		System.out.println(arg1);
		
	}

	@Override
	public void onSubscribe(String arg0, int arg1) {
		System.out.println(arg0);
		System.out.println(arg1);
		
	}

	@Override
	public void onUnsubscribe(String arg0, int arg1) {
		System.out.println(arg0);
		System.out.println(arg1);
		
	}

}

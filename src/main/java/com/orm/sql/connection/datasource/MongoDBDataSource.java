package com.orm.sql.connection.datasource;

import java.util.Map;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.orm.connection.datasource.DataSource;
import com.orm.connection.exception.ConectionException;
import com.orm.sql.connection.pool.MongoDBPool;

public class MongoDBDataSource implements DataSource<MongoDBPool,DB>{

	private MongoDBPool mongoDBPool;
	
	public void setDataSource(MongoDBPool e) {
		mongoDBPool = e;
	}

	public MongoDBPool getDataSource() {
		return mongoDBPool;
	}

	public DB getConnection() throws ConectionException {
		try{
			MongoClient mongoClient =  new MongoClient(mongoDBPool.getHost(),mongoDBPool.getPort());
			return mongoClient.getDB(mongoDBPool.getDb());
		}catch(Exception e){
			throw new ConectionException(e);
		}
	}

	public void init(String type, Map<String, String> property)
			throws ConectionException {
		mongoDBPool = new  MongoDBPool();
		mongoDBPool.setHost(property.get("host"));
		mongoDBPool.setPort(Integer.valueOf(property.get("port")));
		mongoDBPool.setDb(property.get("db"));
	}

}

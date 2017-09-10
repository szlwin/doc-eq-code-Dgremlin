package com.orm.api.connection.mongodb;

import com.mongodb.Mongo;
import com.orm.connection.exception.ConectionException;
import com.orm.api.connection.AbstractApiConnection;

public class MongoDBConnection extends AbstractApiConnection{
	
	protected boolean isClosed = false;

	public void commit() throws ConectionException {
		// TODO Auto-generated method stub
		
	}

	public void close() throws ConectionException {
		((Mongo) con).close();
		isClosed = true;
	}
	
	public void rollback() throws ConectionException {
		// TODO Auto-generated method stub
		
	}

	public boolean isClosed() throws ConectionException {
		return isClosed;
	}

	public void setAutoCommit(boolean isAuto) throws ConectionException {
		// TODO Auto-generated method stub
		
	}

}

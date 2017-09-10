package com.orm.connection.factory;

import java.sql.Connection;

public interface ORMConnectionFactory {

	public Object getConnection() throws Exception;
}

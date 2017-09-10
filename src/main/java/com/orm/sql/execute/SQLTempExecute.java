package com.orm.sql.execute;

import java.sql.SQLException;

public interface SQLTempExecute<E> {

	int EXECUTE_TYPE_INSERT = 1;
	
	int EXECUTE_TYPE_UPDATE = 2;
	
	int EXECUTE_TYPE_DELETE = 3;
	
	int EXECUTE_TYPE_SELECT = 4;
	
	int EXECUTE_TYPE_GET = 5;
	
	int EXECUTE_TYPE_QUERY = 6;
	
	public void execute(E e) throws SQLException;
}

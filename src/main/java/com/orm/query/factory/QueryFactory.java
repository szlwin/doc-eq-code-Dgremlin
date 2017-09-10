package com.orm.query.factory;

import com.orm.query.OrignSQLQuery;
import com.orm.query.SimpleDataQuery;
import com.orm.query.SimpleSQLQuery;

public class QueryFactory {

	public static SimpleDataQuery createSimpleDataQuery(){
		return new SimpleDataQuery();
	}
	
	public static SimpleDataQuery createSimpleDataQuery(String connectionName){
		return new SimpleDataQuery(connectionName);
	}
	
	public static SimpleSQLQuery createSimpleSQLQuery(){
		return new SimpleSQLQuery();
	}
	
	public static SimpleSQLQuery createSimpleSQLQuery(String connectionName){
		return new SimpleSQLQuery(connectionName);
	}
	
	
	public static OrignSQLQuery createOrignSQLQuery(){
		return new OrignSQLQuery();
	}
	
	public static OrignSQLQuery createOrignSQLQuery(String connectionName){
		return new OrignSQLQuery(connectionName);
	}
}

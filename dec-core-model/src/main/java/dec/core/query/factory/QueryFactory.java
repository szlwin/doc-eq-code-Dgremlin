package dec.core.query.factory;

import dec.core.query.OrignSQLQuery;
import dec.core.query.SimpleDataQuery;
import dec.core.query.SimpleSQLQuery;

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
